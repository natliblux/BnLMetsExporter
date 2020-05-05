/*******************************************************************************
 * Copyright (C) 2017-2020 Bibliothèque nationale de Luxembourg (BnL)
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
package lu.bnl.domain.model;

import lu.bnl.reader.MetsGetter;

public class Config {

	private MetsGetter metsGetter;
	
	private String documentIDsFile;
	
	private boolean randomly;
	
	private String dir;
	
	private String outputFile;
	
	private String local;
	
	private boolean exportPrimo;
	
	private boolean exportSolr;
	
	private boolean parallel;
	
	public Config(MetsGetter metsGetter, String dir) {
		this.metsGetter = metsGetter;
		this.dir = dir;
	}
	
	public Config(String documentIDsFile, String dir, boolean randomly, String outputFile) {
		this.documentIDsFile = documentIDsFile;
		this.dir = dir;
		this.randomly = randomly;
		this.outputFile = outputFile;
	}
	
	public MetsGetter getMetsGetter() {
		return metsGetter;
	}

	public String getDocumentIDsFile() {
		return documentIDsFile;
	}

	public boolean isRandomly() {
		return randomly;
	}

	public String getDir() {
		return dir;
	}

	public String getLocal() {
		return local;
	}
	
	// Getters / Setters for Export Mode
	
	public boolean isExportPrimo() {
		return exportPrimo;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setExportPrimo(boolean exportPrimo) {
		this.exportPrimo = exportPrimo;
	}

	public boolean isExportSolr() {
		return exportSolr;
	}

	public void setExportSolr(boolean exportSolr) {
		this.exportSolr = exportSolr;
	}

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}


}
