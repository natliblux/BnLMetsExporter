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
package lu.bnl.xml.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.model.Alto;

public class MetsFileSecHandler extends DefaultHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(MetsFileSecHandler.class);
	
	private HashSet<String> supportedTags;  
	
	private String dir;
	
	// Data
	//================================================================================
	
	private List<Alto> pages = new ArrayList<Alto>();
	
	// State
	//================================================================================
	
	private boolean inAltoGroup = false;
	
	private String currentFileId; // From <file> TAG
	
	//================================================================================
	
	public MetsFileSecHandler(String dir) {
		this.supportedTags = new HashSet<>();
		this.supportedTags.add( MetsConstant.TAG_FILEGRP.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_FILE.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_FLOCAT.toLowerCase() );
		
		this.dir = dir;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ( ! this.supportedTags.contains(qName.toLowerCase()) ) return; // Only use this little optimisation if no stack is used
		
		if ( MetsConstant.TAG_FILEGRP.equalsIgnoreCase(qName) ) {
			
			String id = attributes.getValue( MetsConstant.ATTR_NAME_ID );
			
			if ( MetsConstant.ATTR_FILEGRP_ID_ALTOGRP.equalsIgnoreCase(id) ) {
				// fileGrp ID="ALTOGRP"
				this.inAltoGroup = true;
			}
			
		} else if (this.inAltoGroup) {
			
			this.handleAltoGroup(uri, localName, qName, attributes);
			
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( ! this.supportedTags.contains(qName.toLowerCase()) ) return; // Only use this little optimisation if no stack is used
		
		if ( MetsConstant.TAG_FILEGRP.equalsIgnoreCase(qName) ) { // End of FILEGRP
		
			this.inAltoGroup = false;
			
		}
		
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
	}
	
	//================================================================================
	//================================================================================
	
	private void handleAltoGroup(String uri, String localName, String qName, Attributes attributes) {
		
		if ( MetsConstant.TAG_FILE.equalsIgnoreCase(qName) ) {
			// <file ID="ALTO00001" CREATED="2010-12-03T11:18:00"
			// MIMETYPE="text/xml" CHECKSUM="92780687a1b07d6ee42ed366f3bdd903"
			// CHECKSUMTYPE="MD5" SIZE="303488" GROUPID="1">
			//     <FLocat LOCTYPE="URL" xlink:href="1755016"/>
			// </file>
			this.currentFileId = attributes.getValue( MetsConstant.ATTR_NAME_ID );
		
		} else if (  MetsConstant.TAG_FLOCAT.equalsIgnoreCase(qName) ) {
			// <FLocat LOCTYPE="URL" xlink:href="1755016"/>
			
			String path = attributes.getValue( MetsConstant.ATTR_XLINKHERF );
			path = path.replaceFirst("file://", "").replace("\\", "/");
			
			if (this.dir != null) {
				// We are not using relative paths anymore, so remove the starting "./".
				path = path.replace("./", "/");
				path = this.dir + path;
			}

			this.pages.add(new Alto(this.currentFileId, path));

		}
		
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================

	public List<Alto> getPages() {
		return this.pages;
	}
	
}
