/*******************************************************************************
 * Copyright (C) 2017-2018 Bibliothèque nationale de Luxembourg (BnL)
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
package lu.bnl.io;

import java.io.IOException;

import lu.bnl.domain.model.PrimoDocument;

public interface Archiver {

	/**
	 * Opens and prepares the archive file.
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException;
	
	/**
	 * Write a PrimoDocument to the archive as an XML file.
	 * 
	 * @param primoDocument The PrimoDocument to save to the archive.
	 * @throws IOException
	 */
	public void write(PrimoDocument primoDocument, ArchiverFilenameStrategy archiverFilenameStrategy) throws IOException;
	
	/**
	 * Closes all archive files.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
	
}
