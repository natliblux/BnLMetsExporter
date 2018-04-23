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
package lu.bnl.domain.constants;

public class CliConstant {

	// Main Commands
	//================================================================================
	
	public static final String CMD_L_HELP 		= "help";
	public static final String CMD_S_HELP 		= "h";
	
	public static final String CMD_L_EXPORT 	= "export";
	public static final String CMD_S_EXPORT 	= "e";
	
	public static final String CMD_L_CLEAN		= "clean";
	public static final String CMD_S_CLEAN		= "c";
	
	public static final String CMD_L_TESTCONFIG	= "testconfig";
	public static final String CMD_S_TESTCONFIG	= "tc";
	
	// Options - EXPORT
	// ================================================================================
	
	public static final String CMD_EXPORT_OPTION_PRIMO		= "primo";
	
	public static final String CMD_EXPORT_OPTION_SOLR		= "solr";
	
	public static final String OPTION_L_PIDS		= "pids";
	
	public static final String OPTION_L_DIR			= "dir";
	
	public static final String OPTION_L_OUTPUT 		= "out";

	public static final String OPTION_L_OVERWRITE	= "overwrite";
	
	public static final String OPTION_L_PARALLEL	= "parallel";
	
	public static final String OPTION_L_CONFIG		= "config";
	
	/** Option to use SolrCloud mode.
	 */
	public static final String OPTION_L_CLOUD 		= "cloud";


	private CliConstant() {}
	
}
