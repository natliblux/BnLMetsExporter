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
package lu.bnl.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.constants.MetsTypeHandler;
import lu.bnl.domain.model.Alto;
import lu.bnl.domain.model.Collection;
import lu.bnl.domain.model.DivSection;
import lu.bnl.domain.model.DmdSection;
import lu.bnl.xml.handlers.MetsDMDSecHandler;
import lu.bnl.xml.handlers.MetsFileSecHandler;
import lu.bnl.xml.handlers.MetsLogicalStructureHandler;
import lu.bnl.xml.handlers.ModsCollectionDescriptiveSectionHandler;

public class MetsXMLParserHandler extends DefaultHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(MetsXMLParserHandler.class);

	// Data
	//================================================================================
	
	private String dir;
	
	private List<String> supportedMetsTypes;
	
	// Handlers
	//================================================================================
	
	private ArrayList<DefaultHandler> handlers;
	
	private ModsCollectionDescriptiveSectionHandler modsCollectionHandler;
	private MetsDMDSecHandler metsDMDSecHandler;
	private MetsFileSecHandler metsFileSecHandler;
	private MetsLogicalStructureHandler metsLogicalStructureHandler;
	
	//================================================================================
	
	public MetsXMLParserHandler(String dir, List<String> supportedMetsTypes) {
		this.dir = dir;
		this.supportedMetsTypes = supportedMetsTypes;
		
		this.initHandlers();
		
		logger.debug("All Handlers intialized and ready to use.");
	}
	
	private void initHandlers() {
		this.handlers = new ArrayList<>();
		
		this.modsCollectionHandler = new ModsCollectionDescriptiveSectionHandler();
		this.handlers.add(this.modsCollectionHandler);

		this.metsDMDSecHandler = new MetsDMDSecHandler();
		this.handlers.add(this.metsDMDSecHandler);
		
		this.metsFileSecHandler = new MetsFileSecHandler(this.dir);
		this.handlers.add(this.metsFileSecHandler);
		
		this.metsLogicalStructureHandler = new MetsLogicalStructureHandler(this.supportedMetsTypes);
		this.handlers.add(this.metsLogicalStructureHandler);
	}
	
	//================================================================================
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		for (DefaultHandler handler : this.handlers) {
			handler.startElement(uri, localName, qName, attributes);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		for (DefaultHandler handler : this.handlers) {
			handler.endElement(uri, localName, qName);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		for (DefaultHandler handler : this.handlers) {
			handler.characters(ch, start, length);
		}
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================
	
	public boolean isSupported() {
		return this.metsLogicalStructureHandler.isSupported();
	}
	
	public MetsTypeHandler getMetsTypeHandler() {
		return this.metsLogicalStructureHandler.getMetsTypeHandler();
	}
	
	public DmdSection getDmdSectionIssue() {
		String issueDmdId = this.metsLogicalStructureHandler.getIssueDmdId();
		return this.metsDMDSecHandler.getDmdSection( issueDmdId );
	}
	
	public DmdSection getDmdSectionPrint() {
		return this.metsDMDSecHandler.getDmdSectionPrint();
	}
	
	@Deprecated
	public DmdSection getDmdSectionCollection() {
		return this.metsDMDSecHandler.getDmdSectionCollection();
	}

	public Collection getModsMdCollection() {
		return this.modsCollectionHandler.getCollection();
	}
	
	public DmdSection getDmdSection(String id) {
		return this.metsDMDSecHandler.getDmdSection(id);
	}
	
	
	public List<Alto> getPages() {
		return this.metsFileSecHandler.getPages();
	}
	
	
	public Map<String, DivSection> getArticles() {
		return this.metsLogicalStructureHandler.getArticles();
	}
	
	public Map<String, List<String>> getArticleAltoBlockMap() {
		return this.metsLogicalStructureHandler.getArticleAltoBlockMap();
	}
	
}
