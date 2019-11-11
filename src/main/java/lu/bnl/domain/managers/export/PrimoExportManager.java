/*******************************************************************************
 * Copyright (C) 2017-2019 Bibliothèque nationale de Luxembourg (BnL)
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
package lu.bnl.domain.managers.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.configuration.ExportConfig;
import lu.bnl.domain.managers.StatisticsManager;
import lu.bnl.domain.model.Alto;
import lu.bnl.domain.model.DivSection;
import lu.bnl.domain.model.DocList;
import lu.bnl.io.ArchiverFilenameStrategy;
import lu.bnl.reader.MetsAltoReaderManager;
import lu.bnl.reader.MetsGetter;
import lu.bnl.xml.MetsXMLParserHandler;

public class PrimoExportManager extends ExportManager {

	private static final Logger logger = LoggerFactory.getLogger(PrimoExportManager.class);
	
	// Export Parameters and State
	//================================================================================
	
	private MetsGetter metsGetter;
	
	private String outputFile;
	
	private String dir;
	
	private boolean parallel;
	
	//================================================================================
	//================================================================================
	
	public PrimoExportManager(String outputFile, String dir, boolean parallel) {
		this.outputFile = outputFile;
		this.dir = dir;
		this.parallel = parallel;
	}
	
	@Override
	public void run(MetsGetter metsGetter) {
		
		this.metsGetter = metsGetter;
		
		console("Starting Primo Export.");
		
		this.startGlobal = this.getExecutionTimeTracker().start();
		this.count = metsGetter.getMetsData().size();
		
		StatisticsManager.getInstance().countFiles(count);
		
		logger.info(String.format("%d Mets Document IDs have been found.", this.count));
		
		if (this.parallel) {
			console("Mode: Parallel");
			this.exportParallel(this.outputFile);
		} else {
			console("Mode: Sequential");
			this.export(metsGetter.getMetsData(), this.outputFile);
		}
		
		this.printStats();
	}
	
	private void export(List<String> metsData, String output) {
		ExportConfig ec = AppConfigurationManager.getInstance().getExportConfig();
		List<String> supportedMetsTypes = ec.exportDublinCore;
		
		ArchiverFilenameStrategy strategy = ArchiverFilenameStrategy.getPreferedStrategyForMetsGetter(this.metsGetter);
		console("ArchiverFilenameStrategy " + strategy);
		
		DublinCoreExporter dublinCoreExporter = new DublinCoreExporter(output, strategy);
		dublinCoreExporter.open();
		
		for (String metsAddress : metsData) {

			String documentID 	= this.metsGetter.getUniqueName(metsAddress); // Note: did must be processed differently for local
			String path 		= this.metsGetter.getMetsLocation(metsAddress);

			String dir = this.dir;
			
			// If the location of the METS is defined by the metsGetter then use it, otherwise use the argument dir
			// LocalMetsGetterImpl found a METS at path/to/1-mets.xml and so the relative alto paths are path/to/
			// RemoteMetsGetterImpl gets METS from server and so does not have a local repository. There the dir parameter is
			// used to find alto files directly.
			if (path != null) {
				dir = path;
			}
			
			//System.out.println("THE DOCUMENT ID IS : " + documentID); // DEBUG
			//System.out.println("THE METSADDRESS IS : " + metsAddress); // DEBUG
			//System.out.println("THE METSLOCATION IS : " + path); // DEBUG
			
			String content = metsGetter.getMetsContent(metsAddress, path);
			
			MetsXMLParserHandler metsHandler = MetsAltoReaderManager.parseMets(documentID, content, dir, false, supportedMetsTypes);
			
			if (metsHandler.isSupported()) {
			
				DocList docList = this.handleMetsResult(metsHandler, documentID, false);
	
				// Create Dublin Core Documents
				for (DivSection article : metsHandler.getArticles().values()) {

					// DEBUG
					//System.out.println("*************");
					//System.out.println(article.getTextInLineFormat());

					// DocList is automatically changed.
					if ( dublinCoreExporter.save(article, documentID, metsHandler, docList) ) {
						// SUCCESS
					} else {
						// FAIL
						StatisticsManager.getInstance().countSkipArticles(1);
					}
				}
				
				StatisticsManager.getInstance().countArticles( docList.primoarticles.size() );
				StatisticsManager.getInstance().countPages( docList.pages );
			} else {
				logger.info(String.format("METS with Document ID %s is not supported.", documentID));
				StatisticsManager.getInstance().countCustom("Not Supported", 1);
			}
			
			StatisticsManager.getInstance().countCustom("Processed", 1);

			getExecutionTimeTracker().stopETA(startGlobal, "[X] ", StatisticsManager.getInstance().getCustomValue("Processed"), count);
		}
		
		dublinCoreExporter.close();
	}
	
	private DocList handleMetsResult(MetsXMLParserHandler handler, String documentID, boolean useTokenizer) {
		DocList docList = new DocList();

		try {
			// Index des mots contenant les coordonnees par page/alto
			Map<String, DivSection> articles = handler.getArticles();
			List<Alto> pages = handler.getPages();

			Map<String, List<String>> articleAltoBlockMap = handler.getArticleAltoBlockMap();
			
			logger.info("Pages : " + pages.size());
			logger.info("Articles : " + articles.size());
			logger.info("ArticleAltoBlockMap : " + articleAltoBlockMap.size());
			
			docList.pages += pages.size();
			
			for (Alto page : pages) {
				MetsAltoReaderManager.parseAlto(page, articleAltoBlockMap, articles, useTokenizer);
			}
			
		} catch (Exception e) {
			logger.error("Mets " + documentID + " failed to be parsed", e);
		}

		return docList;

	}
	
	/** Runs the export in parallel by splitting the list of Document IDs
	 *  on multiple threads. Each thread will output its own tar.gz
	 *  file. Each thread calls the standard export function.
	 * 
	 * @param output
	 */
	private void exportParallel(String output) {
		// Remove extension if present to simplify the creation of filenames.
		if (output.endsWith(".tar.gz")) {
			output = output.replaceFirst("\\.tar\\.gz$", "");
		}
		
		// Get the optimal number of cores
		// Do not use more cores than we have Document IDs (overkill)
		int cores = this.getReasonableNumberOfAvailableCores( metsGetter.getMetsData().size() );
		
		// Partition list of Document IDs for each core
		List<List<String>> partitions = this.getPartitions(metsGetter.getMetsData(), cores);
		
		console("Number of partitions: " + partitions.size());
		
		// Create Threads
		List<Thread> threads = new ArrayList<>();
		
		for (int i = 0; i < partitions.size(); i++) {
			String partName = String.format("%s_%d.tar.gz", output, i);
			File partNameFile = new File(partName);
			if (partNameFile.exists()) {
				partNameFile.delete();
			}
			
			console(String.format("Starting Thread %d (Output will be in: %s)", i, partName));
			
			List<String> partition = partitions.get(i);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					export(partition, partName);
				}
			});
			
			threads.add(t);
			
			t.start();
		}
		
		// Follow and wait for finishing
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				logger.error("Thread InterruptedException.", e);
				e.printStackTrace();
			}
		}
	}
	
}
