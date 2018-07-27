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
package lu.bnl.domain.managers.export;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.domain.model.ArticleDocumentBuilder;
import lu.bnl.domain.model.DivSection;
import lu.bnl.domain.model.DocList;
import lu.bnl.domain.model.DublinCoreDocument;
import lu.bnl.domain.model.PrimoDocument;
import lu.bnl.io.Archiver;
import lu.bnl.io.ArchiverFilenameStrategy;
import lu.bnl.io.TarGzArchiver;
import lu.bnl.xml.MetsXMLParserHandler;

public class DublinCoreExporter implements Exporter {

	private static final Logger logger = LoggerFactory.getLogger(DublinCoreExporter.class);
	
	private Archiver archiver;

	private String outputFile;
	
	private ArchiverFilenameStrategy archiverFilenameStrategy;
	
	public DublinCoreExporter(String outputFile, ArchiverFilenameStrategy archiverFilenameStrategy) {
		this.outputFile = outputFile;
		this.archiverFilenameStrategy = archiverFilenameStrategy;
	}
	
	/** Creates a new archive with the name given in the constructor.
	 * 
	 *  @return True is archive successfully opened, false otherwise.
	 */
	@Override
	public boolean open() {
		boolean success = false;
		
		// Remove file if it is existing
		File f = new File(outputFile);
		if (f.exists()) {
			f.delete();
		}
		
		try {
			archiver = new TarGzArchiver( this.outputFile );
			archiver.open();
			success = true;
		} catch (IOException e) {
			logger.error("Archiver error while opening.", e);
			
			// Make sure it is null, in case the method is called again after an error
			archiver = null; 
		}
		
		return success;
	}
	
	/** Save the divSection as a Dublin Core Document.
	 * 
	 *  @return True if the divSection has been successfully saved, false otherwise.
	 */
	@Override
	public boolean save(DivSection divSection, String pid, MetsXMLParserHandler metsHandler, DocList docList) {
		boolean success = false;
		
		// getText to skip illustrations without description.
		// !divSection.getBlocks().isEmpty() does not work
		if (  !divSection.getText().isEmpty() ) {
			PrimoDocument doc = null;
			try {
				doc = createPrimoArticle(pid, divSection, metsHandler);
				
				docList.primoarticles.add(doc);
				
				logger.info(String.format("Created PrimoDocument %s, %s, %s", pid, doc.getDmdid(), doc.getId()));
				logger.debug(String.format("Add to archive. PID = %s", pid));
				
				this.archiver.write(doc, archiverFilenameStrategy);
				
				success = true;
				
			} catch (IOException e) {
				logger.error(String.format("Archiver failed to save PrimoDocument %s, %s, %s", pid, doc.getDmdid(), doc.getId()), e);
			} catch (Exception e) {
				// For debug purpose of MARC code
				e.printStackTrace();
			}
			
			
		} else {
			logger.info(String.format("Skipped PrimoDocument %s, %s, %s", pid, divSection.getDmdid(), divSection.getId()));
		}
		
		return success;
	}
	
	/** Closes the archive file.
	 * 
	 *  @return True is archive successfully closed, false otherwise.
	 */
	@Override
	public boolean close() {
		boolean success = false;
		
		try {
			if (archiver != null) {
				archiver.close();
				success = true;
			}
		} catch (IOException e) {
			logger.error("Archiver error while closing.", e);
		}
		
		return success;
	}
	
	private PrimoDocument createPrimoArticle(String pid, DivSection article, MetsXMLParserHandler handler) {
		
		ArticleDocumentBuilder builder = ExportManager.getArticleDocumentBuilder(pid, article, handler);
		
		DublinCoreDocument dublinCoreDocument = builder.build();
		
		PrimoDocument doc = new PrimoDocument(builder.getId(), builder.getPid(), builder.getDmdId());
		
		doc.getListRecords().getRecord().getMetadata().setDublinCoreDocument(dublinCoreDocument);
		
		return doc;
	}
	
	
	
}
