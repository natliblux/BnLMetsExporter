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
package lu.bnl.domain.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import lu.bnl.configuration.AppConfigurationManager;

@XStreamAlias("oai_dc:dc")
public class DublinCoreDocument {

	@XStreamAsAttribute 
    @XStreamAlias("xmlns:oai_dc")
    final String oai_dc = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    
	@XStreamAsAttribute 
    @XStreamAlias("xmlns:dc")
    final String dc = "http://purl.org/dc/elements/1.1/";
	
	@XStreamAsAttribute 
    @XStreamAlias("xmlns:xsi")
    final String xsi = "http://www.w3.org/2001/XMLSchema-instance";
	
	@XStreamAsAttribute 
    @XStreamAlias("xmlns:dcterms")
    final String dcterms = "http://purl.org/dc/terms/";
	
	@XStreamAsAttribute 
    @XStreamAlias("xsi:schemaLocation")
    final String schemaLocation = "http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd";

	@XStreamOmitField
	private String host = "http://www.my-server.com"; // This will be overwritten

	//================================================================================
	
	//Example: <dc:source>newspaper/luxwort/1929-11-01</dc:source>
	@XStreamAlias("dc:source")
	private String source;
	
	//Example: <dcterms:isPartOf>Luxemburger Wort</dcterms:isPartOf>
	@XStreamImplicit(itemFieldName="dcterms:isPartOf")
	private List<String> isPartOfs;
	
	//Example: <dcterms:isReferencedBy>issue:newspaper/luxwort/1929-11-01/article:MODSMD_ARTICLE1-DTL3</dcterms:isReferencedBy>
	@XStreamAlias("dcterms:isReferencedBy")
	private String isReferencedBy;
	
	//Example: <dc:date>1929-11-01</dc:date>
	@XStreamAlias("dc:date")
	private String date;
	
	//Example: <dc:publisher>Verl. der St-Paulus-Druckerei</dc:publisher>
	@XStreamAlias("dc:publisher")
	private String publisher;
	
	//Example: <dc:relation>1367006</dc:relation>
	@XStreamAlias("dc:relation")
	private String relation;
	
	//Example: <dcterms:hasVersion>http://www.example.com/service?pid=1367006#panel:pp|issue:1367006|article:DTL67</dcterms:hasVersion>
	@XStreamAlias("dcterms:hasVersion")
	private String hasVersion;
	
	//Example: <dc:description>Allerseelen.....
	@XStreamAlias("dc:description")
	private String description;
	
	//Example: <dc:title>Allerseelen.</dc:title>
	@XStreamAlias("dc:title")
	private String title;
	
	//Example: <dc:creator>M. Ch.</dc:creator>
	@XStreamImplicit(itemFieldName="dc:creator")
	private List<String> creators;
	
	//Example: <dc:type>ARTICLE</dc:type>
	@XStreamAlias("dc:type")
	private String type;
	
	//Example: <dc:language>de</dc:language>
	@XStreamImplicit(itemFieldName="dc:language")
	private List<String> languages;
	

	public DublinCoreDocument(ArticleDocumentBuilder builder) {
		this.host = AppConfigurationManager.getInstance().getExportConfig().viewerHostBaseURL;
		
		this.source = builder.getRecordIdentifier();
		
		this.isReferencedBy = this.createIsReferencedBy(builder);
		
		this.date = builder.getDate();
		
		this.publisher = builder.getPublisher();
		
		this.relation = builder.getPid();
		
		this.hasVersion = this.createHasVersion(builder);
		
		this.description = builder.getText();
		
		this.isPartOfs = builder.getIsPartOfs();
		
		this.title = builder.getTitle();
		
		this.creators = builder.getCreators();
		
		this.languages = builder.getLanguages();
		
		this.type = builder.getType();
	}
	
	//================================================================================
	// Helper Functions
	//================================================================================
	
	private String createIsReferencedBy(ArticleDocumentBuilder builder) {
		/*return String.format("issue:%s/article:%s-%s", 
				builder.recordIdentifier, 
				builder.dmdid, 
				builder.id);*/
		return String.format("issue:%s/article:%s", 
				builder.getRecordIdentifier(),  
				builder.getId());
	}
	
	private String createHasVersion(ArticleDocumentBuilder builder) {
		String pattern = AppConfigurationManager.getInstance().getExportConfig().primo.hasVersionPattern;
				
		Map <String, String> mapping = new HashMap<>();
		mapping.put("host"		, this.host);
		mapping.put("pid"		, builder.getPid());
		mapping.put("id"		, builder.getId());
		mapping.put("panel"		, builder.getPanel());
		
		for (String key : mapping.keySet()) {
			pattern = pattern.replace("{{"+key+"}}", mapping.get(key));
		}
		
		return pattern;
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<String> getIsPartOfs() {
		return isPartOfs;
	}

	public void setIsPartOf(List<String> isPartOfs) {
		this.isPartOfs = isPartOfs;
	}

	public String getIsReferencedBy() {
		return isReferencedBy;
	}

	public void setIsReferencedBy(String isReferencedBy) {
		this.isReferencedBy = isReferencedBy;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getHasVersion() {
		return hasVersion;
	}

	public void setHasVersion(String hasVersion) {
		this.hasVersion = hasVersion;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getCreators() {
		return creators;
	}

	public void setCreators(List<String> creators) {
		this.creators = creators;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	
	
}
