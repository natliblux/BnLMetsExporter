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
package lu.bnl.domain.model;


import java.text.SimpleDateFormat;
import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lu.bnl.configuration.AppConfigurationManager;

/*
 * <OAI-PMH 
 * xmlns="http://www.openarchives.org/OAI/2.0/" 
 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
 * xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
 */

@XStreamAlias("OAI-PMH")
public class PrimoDocument {
	
    @XStreamAsAttribute
    final String xmlns = "http://www.openarchives.org/OAI/2.0/";

    @XStreamAsAttribute 
    @XStreamAlias("xmlns:xsi")
    final String xsi = "http://www.w3.org/2001/XMLSchema-instance";
    
	@XStreamAsAttribute 
    @XStreamAlias("xsi:schemaLocation")
    final String schemaLocation = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
	
	/**
	 * Id of the DIV tag in the logical structure.
	 */
	@XStreamOmitField
	private String id;
	
	/**
	 * ID of the METS object.
	 */
	@XStreamOmitField
	private String documentID;
	
	@XStreamOmitField
	private String dmdid;
	
	@XStreamOmitField
	private String fileId;
	
	@XStreamOmitField
	private String begin;
	
	private String responseDate;
	private String request;
	
	@XStreamAlias("ListRecords")
	private ListRecords listRecords = null;
	
	private static final SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	
	public PrimoDocument() {
		
	}
	
	public PrimoDocument(String id, String documentID, String dmdid) {
		this.id = id;
		this.documentID = documentID;
		this.dmdid = dmdid;
		
		this.responseDate = formater.format(new Date()); //"2014-04-23T17:49:05";
		
		this.request = AppConfigurationManager.getInstance().getExportConfig().primo.oaiHostURL;
		
		this.listRecords = new ListRecords();

		String recordIdentifierBase = AppConfigurationManager.getInstance().getExportConfig().primo.recordIdentifier;
		String recordIdentifier = String.format("%s:%s-%s", recordIdentifierBase, documentID, id);
		
		this.listRecords.getRecord().getHeader().setIdentifier( recordIdentifier );
	}

	public String getDocumentID() {
		return documentID;
	}

	public void setDocumentID(String documentID) {
		this.documentID = documentID;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDmdid() {
		return dmdid;
	}

	public void setDmdid(String dmdid) {
		this.dmdid = dmdid;
	}

	public String getFileId() {
		return fileId;
	}
	
	public void setBegin(String begin) {
		this.begin = begin;
	}
	
	public String getBegin() {
		return begin;
	}
	
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getResponseDate() {
		return responseDate;
	}

	public void setResponseDate(String responseDate) {
		this.responseDate = responseDate;
	}
	
	public ListRecords getListRecords() {
		return listRecords;
	}
	
	public void setListRecords(ListRecords listRecords) {
		this.listRecords = listRecords;
	}

	
	public static class ListRecords {
		
		private PrimoRecord record = new PrimoRecord();

		public PrimoRecord getRecord() {
			return record;
		}

		public void setRecord(PrimoRecord record) {
			this.record = record;
		}
		
	}

}
