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
package lu.bnl.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import lu.bnl.domain.model.PrimoDocument;

public class TarGzArchiver implements Archiver {

	private static final Logger logger = LoggerFactory.getLogger(TarGzArchiver.class);
	
	private static final String encoding = "UTF-8";
	
	// Files
	
	private String filename;
	
	private File archiveFile;
	
	// Streams
	
	private FileOutputStream fileOutput = null;
	
	private BufferedOutputStream bufferedOutput = null;
	
	private GzipCompressorOutputStream gzipOutput = null;
	
	private TarArchiveOutputStream tarOutput = null;
	
	// XML
	
	private XStream xstream = null;
	
	// Progress
	
	private Set<String> entries;
	
	public TarGzArchiver(String filename) {
		this.filename = this.getFixedFilename(filename);
		
		XmlFriendlyNameCoder replacer = new XmlFriendlyNameCoder("ddd", "_");  
		xstream = new XStream(new StaxDriver(replacer));
		xstream.processAnnotations(PrimoDocument.class);
		
		this.entries = new HashSet<String>();
	}
	
	@Override
	public void open() throws IOException {
		this.archiveFile 	= new File(this.filename);
		
		this.fileOutput 	= new FileOutputStream(archiveFile);
		this.bufferedOutput = new BufferedOutputStream(fileOutput);
		this.gzipOutput 	= new GzipCompressorOutputStream(bufferedOutput);
		this.tarOutput 		= new TarArchiveOutputStream(gzipOutput);
		tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU); // Avoid error "File name is too long >100bytes)
		
		logger.info("TarGz streams successfully opened."); 
	}
	
	@Override
	public void write(PrimoDocument article, ArchiverFilenameStrategy archiverFilenameStrategy) throws IOException {
		if (article != null) {
			String filename = this.getFilenameForPrimoDocument(article, archiverFilenameStrategy);
				
			// Track unique filename entries and log if duplicates found.
			if (entries.contains(filename)) {
				logger.info(String.format("Duplicate entry in the archive: %s", filename));
			} else {
				entries.add(filename);
			}
			
			//System.out.println(filename); // DEBUG
			
			TarArchiveEntry tarEntry = new TarArchiveEntry(filename);
			
			//String xmlString = this.xstream.toXML(article);
			String xmlString = this.toXml(article);
			byte[] xmlBytes = xmlString.getBytes(encoding); // Make sure the right encoding is used.
			tarEntry.setSize(xmlBytes.length);
			
			this.tarOutput.putArchiveEntry(tarEntry);
			
			this.tarOutput.write(xmlBytes);
			
		    this.tarOutput.closeArchiveEntry();
		    
		    logger.info(String.format("Article \"%s\" successfully added to the archive (%d bytes).", filename, xmlBytes.length));
	    }
	}
	
	@Override
	public void close() throws IOException {
		this.tarOutput.finish();
		this.tarOutput.close();
		
		this.gzipOutput.close();
		this.bufferedOutput.close();
		this.fileOutput.close();
		
		logger.info("TarGz streams successfully closed.");
	}
	
	/**
	 * Converts an PrimoDocument to an XML String with the encoding
	 * defined in the class (default: UTF-8)
	 * 
	 * @param article
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String toXml(PrimoDocument article) throws UnsupportedEncodingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(outputStream, encoding);
		this.xstream.toXML(article, writer);
		String xml = outputStream.toString(encoding);
		
		return xml;
	}
	
	/**
	 * Returns the filename always with the extension ".tar.gz".
	 * If the filename does not end with the desired extension ".tar.gz", 
	 * then the extension is added and the new filename is returned.
	 * 
	 * @param filename
	 * @return The same or modified filename as a string.
	 */
	private String getFixedFilename(String filename) {
		String newFilename = filename;
		String extension = ".tar.gz";
		
		if ( !newFilename.endsWith(extension) ) {
			newFilename += extension;
		}
		
		return newFilename;
	}
	
	
	/**
	 * Returns a path to an XML filename from an PrimoDocument / article based on a given strategy.
	 *  
	 * @param article
	 * @param strategy
	 * @return
	 */
	private String getFilenameForPrimoDocument(PrimoDocument article, ArchiverFilenameStrategy strategy) {
		String filename = null; // TODO: Choose default strategy
		
		if (strategy == ArchiverFilenameStrategy.PID_DIGITOOL) {
			
			filename = this.getFilenameFromPrimoDocument(article);
			
		} else if  (strategy == ArchiverFilenameStrategy.ORIGINAL_DIR) {
			
			filename = this.getFilenameForOriginalDir(article);
			
		} else if ( strategy == ArchiverFilenameStrategy.ARK) {

			filename = this.getFilenameForArk(article);

		}

		return filename;
	}
	
	
	/**
	 * Returns a path to an XML filename from an PrimoDocument / article.
	 * The XML filename is based on the document ID, dmdid and id.
	 * The path is  created by the document ID, such that files are organised into 
	 * folders of about 1000 objects.
	 * 
	 * @param article
	 * @return The file path.
	 */
	private String getFilenameFromPrimoDocument(PrimoDocument article) {
		String documentID 	= article.getDocumentID();
		String dmdid 		= article.getDmdid();
		String id 			= article.getId();
		
		String fileId	= article.getFileId();
		String begin	= article.getBegin();
		
		if (dmdid == null){
			dmdid = "";
		}
		
		String dir = documentID.substring(documentID.length() - 3) + "/" + documentID; // Take the last 3 number of the Document ID and then the Document ID
		
		return String.format("%s/%s-%s-%s.xml", dir, documentID, dmdid, id);
	}
	
	/** 
	 * The XML filename is based on the pid, dmdid an id.
	 * The pid is used as directory.
	 * 
	 * @param article
	 * @return The file path.
	 */
	private String getFilenameForOriginalDir(PrimoDocument article) {
		String documentID 	= article.getDocumentID();
		String dmdid	 	= article.getDmdid();
		String id 			= article.getId();
		
		String dir 			= documentID;
		
		return String.format("%s/%s-%s-%s.xml", dir, documentID, dmdid, id);
	}

	/**
	 * The XML filename is based on the ARK, dmdId and id.
	 * The ARK is cleaned to support the file system (replacement of special characters)
	 * 
	 * @param article
	 * @return The file path.
	 */
	private String getFilenameForArk(PrimoDocument article) {
		String documentID = article.getDocumentID();
		documentID = documentID.replace(":", "-");
		documentID = documentID.replace("/", "-");

		String dmdid 	= article.getDmdid();
		String id 		= article.getId();
		
		if (dmdid == null){
			dmdid = "";
		}
		
		String dir = documentID.substring(documentID.length() - 3) + "/" + documentID; // Take the last 3 number of the Document ID and then the Document ID
		
		return String.format("%s/%s-%s-%s.xml", dir, documentID, dmdid, id);
	}
	
}
