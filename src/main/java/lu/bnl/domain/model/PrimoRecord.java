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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("record")
public class PrimoRecord {
	
	private Header header;
	private Metadata metadata;
	
	public PrimoRecord() {
		header = new Header();
		metadata = new Metadata();
	}
	
	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	static class Header {
		
		private String identifier;
		
		private String datestamp;
		
		public Header() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			Date date = new Date();
			this.datestamp = dateFormat.format(date);
		}
		
		public String getIdentifier() {
			return identifier;
		}

		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		
		public String getDatestamp() {
			return datestamp;
		}
		
	}

	
	public static class Metadata {
		/*
		 * oai_dc:dc 
		 * 			xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" 
		 * 			xmlns:dc="http://purl.org/dc/elements/1.1/" 
		 *  		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		 *   		xmlns:dcterms="http://purl.org/dc/terms/"
		 *    		xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc.xsd http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
		 */
		@XStreamAlias("oai_dc:dc")
		private DublinCoreDocument dublinCoreDocument;

		public DublinCoreDocument getDublinCoreDocument() {
			return dublinCoreDocument;
		}

		public void setDublinCoreDocument(DublinCoreDocument dublinCoreDocument) {
			this.dublinCoreDocument = dublinCoreDocument;
		}

	}
	
}
