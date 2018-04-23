/*******************************************************************************
 * Copyright (C) 2017-2018 Bibliotèque nationale de Luxembourg (BnL)
 *
 * This file is part of BnLMetsExporter.
 *
 * BnLMetsExporter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BnLMetsExporter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BnLMetsExporter.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lu.bnl;

import java.io.File;
import java.util.Comparator;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.client.ParserClient;
import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.domain.constants.CliConstant;
import lu.bnl.domain.managers.export.PrimoExportManager;
import lu.bnl.domain.managers.export.SolrExportManager;
import lu.bnl.domain.managers.solr.SolrCleanManager;
import lu.bnl.domain.model.Config;
import lu.bnl.files.FileUtil;
import lu.bnl.reader.LocalMetsGetterImpl;
import lu.bnl.reader.MetsGetter;
import lu.bnl.reader.RemoteMetsGetterImpl;

public class BnLMetsExporter extends ParserClient {

	private static final Logger logger = LoggerFactory.getLogger(BnLMetsExporter.class);
	
	private Options options;
	
	private static String OUTPUT_EXPORT_FILE = "import-for-primo.tar.gz";  // Default Value
	
	private static String DEFAULT_EXPOR_CONFIG = "config/export.yml";  // Default Value
	
	private boolean USE_CLOUD = false;  // Default Value

	public BnLMetsExporter() {
		
	}

	public BnLMetsExporter(boolean USE_CLOUD) {
		this.USE_CLOUD = USE_CLOUD;
	}

	public static void main(String[] args) throws ParseException {
		
		BnLMetsExporter.printLicenseHelp();
		
		BnLMetsExporter exporterApp = new BnLMetsExporter();
		exporterApp.parseArguments(args);
		
	}
	
	public static void printLicenseHelp() {
		System.out.println("This is free software, see COPYING and AUTHORS files for details given with this program.");	
	}

	@SuppressWarnings("static-access")
	void parseArguments(String[] args) throws ParseException {
				
		options = new Options();

		OptionGroup groupCommands = new OptionGroup();
		
		//================================================================================
		// Global
		//================================================================================

		addOptionalOption(options, CliConstant.OPTION_L_CLOUD, 
				String.format("Flag to enable SolrCloud Mode. Default: %b", this.USE_CLOUD));
		
		//================================================================================
		// HELP
		//================================================================================
		
		groupCommands.addOption(OptionBuilder
				.hasArg(false)
				.isRequired(false)
				.withDescription("Show the help of this program.")
				.withLongOpt(CliConstant.CMD_L_HELP)
				.create(CliConstant.CMD_S_HELP));
		
		//================================================================================
		// TEST CONFIG
		//================================================================================
		
		groupCommands.addOption(OptionBuilder
				.hasArg(false)
				.isRequired(false)
				.withDescription("Test and prints the config.")
				.withLongOpt(CliConstant.CMD_L_TESTCONFIG)
				.create(CliConstant.CMD_S_TESTCONFIG));
		
		//================================================================================
		// Generate / Export
		//================================================================================
		
		groupCommands.addOption(OptionBuilder
				.hasOptionalArg()
				.isRequired(false)
				.withDescription("Export documents, given as a list of PIDS, for Primo or Solr. (-export <TARGET>, <TARGET can be 'primo', 'solr'>)")
				.withLongOpt(CliConstant.CMD_L_EXPORT)
				.create(CliConstant.CMD_S_EXPORT));
		
		addOptionalArgOption(options, CliConstant.OPTION_L_PIDS,
				String.format("Input file for pids for remote download of METS files."));
		
		addOptionalArgOption(options, CliConstant.OPTION_L_DIR,
				"The base directory.");
		
		addOptionalArgOption(options, CliConstant.OPTION_L_OUTPUT, 
				String.format("Output dir/file (default: %s)", OUTPUT_EXPORT_FILE));
		
		addOptionalArgOption(options, CliConstant.OPTION_L_CONFIG,
				String.format("Path to the export YAML configuration file. (default: %s)", DEFAULT_EXPOR_CONFIG));
		
		addOptionalOption(options, CliConstant.OPTION_L_OVERWRITE, 
				"Flag to override the output file if it already exists.");
		
		addOptionalOption(options, CliConstant.OPTION_L_PARALLEL, 
				"Flag to compute PIDs in parallel. This will output multiple TARGZs files.");
		
		//================================================================================
		// Clean
		//================================================================================
		
		groupCommands.addOption(OptionBuilder
				.isRequired(false)
				.withDescription("Clean the Solr index (-clean)")
				.withLongOpt(CliConstant.CMD_L_CLEAN)
				.create(CliConstant.CMD_S_CLEAN));
		
		//================================================================================
		//================================================================================
		
		options.addOptionGroup(groupCommands);

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);
		
		if (args == null || args.length == 0) {
			this.printHelp(options);
			return;
		}
		
		console("BnLMetsExporter = " + AppVersion.VERSION);
		console("Solr = " + AppVersion.SOLR_VERSION);

		this.USE_CLOUD = cmd.hasOption(CliConstant.OPTION_L_CLOUD);

		console("Cloud Mode: " + this.USE_CLOUD);
		
		this.processCommandLine(cmd);
	}
	
	/**
	 * Processes the command line and checking all options.
	 * 
	 * @param cmd
	 */
	private void processCommandLine(CommandLine cmd) {

		if (cmd.hasOption(CliConstant.CMD_L_HELP)) {
			
			this.printHelp(this.options);
			
		} else if (cmd.hasOption(CliConstant.CMD_L_EXPORT)) {
			
			this.perform_export(cmd);
		
		} else if (cmd.hasOption(CliConstant.CMD_L_CLEAN)) {
			
			this.perform_clean(cmd);
			
		} else if (cmd.hasOption(CliConstant.CMD_L_TESTCONFIG)) {
			
			this.perform_testConfig(cmd);
			
		} else {
			
			console("Unknown command");
			
			this.printHelp(this.options);
		}
	}

	private void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(new Comparator<Option>() {
			@Override
			public int compare(Option o1, Option o2) {
				return 0;
			}
		});
		
		String[] commandList = {
				CliConstant.CMD_L_HELP,
				CliConstant.CMD_L_EXPORT,
				CliConstant.CMD_L_CLEAN,
				CliConstant.CMD_L_TESTCONFIG
		};
		
		String commands = StringUtils.join(commandList, ", "); 
		String header = String.format("BnLMetsExporter %s / Solr %s \nAvailable commands:\n%s", 
				AppVersion.VERSION, AppVersion.SOLR_VERSION, commands);
		
		String footer = null;
		
		formatter.printHelp("java -jar BnLMetsExporter", header, options, footer, false);
	}
	
	//================================================================================
	// Command Line Functions
	//================================================================================
	
	private void loadConfig(CommandLine cmd) {
		// Load the config file for the export
		String inputConfig = cmd.getOptionValue(CliConstant.OPTION_L_CONFIG, DEFAULT_EXPOR_CONFIG);
		console("Loading Config: " + inputConfig);
		
		AppConfigurationManager.getInstance().loadYmlExportConfig(inputConfig);
	}
	
	// Used by: PrimoExport
	private void perform_export(CommandLine cmd) {
		String exportMethod = cmd.getOptionValue(CliConstant.CMD_L_EXPORT, "");
		
		// Try to recognize commands
		boolean exportPrimo = exportMethod.equalsIgnoreCase(CliConstant.CMD_EXPORT_OPTION_PRIMO);
		boolean exportSolr 	= exportMethod.equalsIgnoreCase(CliConstant.CMD_EXPORT_OPTION_SOLR);
		
		if ( !exportPrimo && !exportSolr ) {
			console("[ABORT] No export target as been set. use -export primo or -export solr");
			return;
		}
		
		console("Exporting for:");
		if (exportPrimo) {
			console(" - Primo");
		}
		if (exportSolr) {
			console(" - Solr");
		}
		
		String dir	= cmd.getOptionValue(CliConstant.OPTION_L_DIR);
		String pids	= cmd.getOptionValue(CliConstant.OPTION_L_PIDS);
		
		// Convenience for readable if-else
		Boolean hasDir = dir != null;
		Boolean hasPids = pids != null;
		
		// Init for later use
		MetsGetter metsGetter = null;
		String metsGetterInputData = null;
		
		if (hasPids && hasDir) { // Note: hasOption somehow does not work, since there is a default value...
			console("Using Remote Mets Getter.");
			metsGetter = new RemoteMetsGetterImpl();
			
			metsGetterInputData = pids;
			
			// Check if the PIDS file is valid
			if (FileUtil.checkFile(pids) == null) {
				console("Abording! Reason: You must provide a valid PIDS file.");
				return;
			}
			
		} else if ( hasDir ) {
			console("Using Local Mets Getter.");
			metsGetter = new LocalMetsGetterImpl();
			
			metsGetterInputData = dir;
			
		} else {
			console("Abording! Reason: You must provide a -dir for finding METS files. The option -pids is optional for local usage.");
			return;
		}

		// Check if the DIR is valid
		if (FileUtil.checkDir(dir) == null) {
			console("Abording! Reason: You must provide a valid dir.");
			return;
		}
		
		String outputExportFile	= cmd.getOptionValue(CliConstant.OPTION_L_OUTPUT, 	"" + OUTPUT_EXPORT_FILE);
		
		Boolean override 		= cmd.hasOption(CliConstant.OPTION_L_OVERWRITE);
		Boolean parallel 		= cmd.hasOption(CliConstant.OPTION_L_PARALLEL);
		
		
		
		// SPECIFIC PRIMO CHECKS
		if (exportPrimo) {
			outputExportFile = FileUtil.enforceFile(outputExportFile, OUTPUT_EXPORT_FILE);
			File outputFile = new File(outputExportFile);
			
			if ( outputFile.exists() && !override ) {
				console(String.format("[ABORT] A file named '%s' already exists. Use a different name or use -overwrite to overwrite the file(s).", outputExportFile));
				
				return;
			}/* else {
				// Check PID input file
				File file = FileUtil.checkFile(INPUT_PIDS);
				if (file == null) {
					console("Input file for pids not found : " + INPUT_PIDS);
					return;
				} else {
					console("File(s) will be exported into " + outputExportFile + " ...");
				}
			}*/
		}
		
		// SPECIFIC SOLR CHECKS
		if (exportSolr) {
			// TODO: Add any specific solr checks if necessary.
			
		}
		
		// Load the config file for the export
		this.loadConfig(cmd);
		
		// Find all Mets
		metsGetter.findAllMets(metsGetterInputData);
		
		Config config = new Config(metsGetter, dir);
		config.setOutputFile(outputExportFile);
		config.setExportPrimo(exportPrimo);
		config.setExportSolr(exportSolr);
		config.setParallel(parallel);

		// Run Export for Primo OR for Solr (Not both)
		if ( config.isExportPrimo() ) {
			
			exportForPrimo(config);
		
		} else if ( config.isExportSolr() ) {
			
			exportForSolr(config);
			
		}
		
	}
	
	private void perform_clean(CommandLine cmd) {
		
		this.loadConfig(cmd);
		
		Scanner input = new Scanner(System.in);
		
		try {
			// Ask confirmation
			System.out.println("Are you sure to clean all indices? (y/n) ");
			String userInput = input.next();
			
			if (userInput.equalsIgnoreCase("y")) {
				
				// Wait 10 sec before really cleaning
				int countdown = 10;
				console(String.format("Cleaning will be done in %d seconds.", countdown));
				while (countdown > 0) {
					countdown--;
					console(String.format("%d", countdown));
					Thread.sleep(1000);
				}
				
				// Clean
				console("Launching Cleaning");
				SolrCleanManager solrCleanManager = new SolrCleanManager(this.USE_CLOUD);
				solrCleanManager.run();
				
			} else {
				console("Cleaning is canceled");
			}
			
		} catch (Exception e) {
			System.out.println("Cleaning has been stopped or failed!");
			e.printStackTrace();
		} finally {
			input.close();
		}
	}
	
	private void perform_testConfig(CommandLine cmd) {
		
		this.loadConfig(cmd);
		
		console("Done.");
		
	}
	
	//================================================================================
	// Global
	//================================================================================
	

	@SuppressWarnings("static-access")
	private void addOptionalArgOption(Options options, String name, String desc) {
		Option option = OptionBuilder
				.withArgName(name)
				.hasOptionalArg()
				.isRequired(false)
				.withDescription(desc)
				.create(name);
		
		options.addOption(option);
	}
	
	@SuppressWarnings("static-access")
	private void addOptionalOption(Options options, String name, String desc) {
		Option option = OptionBuilder
				.isRequired(false)
				.withDescription(desc)
				.create(name);
		
		options.addOption(option);
	}
	
	/** Export all PIDs for Primo using PrimoExporterManager.
	 * 
	 * @param pidContents
	 * @param config
	 */
	private void exportForPrimo(Config config) {
		PrimoExportManager primoExportManager = new PrimoExportManager( config.getOutputFile(), config.getDir(), config.isParallel());
		primoExportManager.run(config.getMetsGetter());
	}
	
	/** Export all PIDs to Solr using SolrExporterManager.
	 * 
	 * @param pidContents
	 * @param config
	 */
	private void exportForSolr(Config config) {
		SolrExportManager solrExportManager = new SolrExportManager(config.getDir(), this.USE_CLOUD, config.isParallel());
		solrExportManager.run(config.getMetsGetter());
	}
	
}
