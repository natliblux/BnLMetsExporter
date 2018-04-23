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
package lu.bnl.xml.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.model.DmdSection;
import lu.bnl.domain.model.Pair;
import lu.bnl.domain.model.marc.DataField;
import lu.bnl.domain.model.mods.Author;

public class MetsDMDSecHandler extends DefaultHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(MetsDMDSecHandler.class);
	
	private HashSet<String> supportedTags;
	
	// Data
	//================================================================================
	
	private Map<String, DmdSection> dmds = new HashMap<String, DmdSection>();
	
	// State
	//================================================================================
	
	private StringBuffer currentContent;
	
	private boolean inDmdSec = false;
	
	private boolean inTitleInfo = false;
	
	private boolean inMarc = false;
	
	private boolean inLocalIdentifier 	= false;
	private boolean inBarcodeIdentifier = false;
	
	private boolean inOriginInfoManufacturer = false; // if true, then it is next tag publisher is PRINTER!
	
	
	private DmdSection currentDmd;
	
	
	private String currentTag = null;
	
	private DataField currentDataField = null;
	
	private String currentCode = null;
	
	// Only one Author
	private Pair currentNamePart = null;
	
	// Support for 0..n authors
	private Author currentAuthor = null;
	
	//================================================================================
	
	public MetsDMDSecHandler() {
		this.supportedTags = new HashSet<>();
		this.supportedTags.add( MetsConstant.TAG_DMDSEC.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_DMDSEC_MDWRAP.toLowerCase() );
		
		this.supportedTags.add( MetsConstant.TAG_MODS_IDENTIFIER.toLowerCase() );
		
		this.supportedTags.add( MetsConstant.TAG_MODS_TITLEINFO.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_MODS_NAME.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_MODS_NAMEPART.toLowerCase() );
		
		this.supportedTags.add( MetsConstant.TAG_MODS_ORIGININFO.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_MODS_PUBLISHER.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_MODS_RECORDIDENTIFIER.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_MODS_DATEISSUED.toLowerCase() );
		
		this.supportedTags.add( MetsConstant.TAG_MODS_LANGUAGETERM.toLowerCase() );
		
		this.supportedTags.add( MetsConstant.TAG_MARC_CONTROLFIELD.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_MARC_DATAFIELD.toLowerCase() );
		this.supportedTags.add( MetsConstant.TAG_MARC_SUBFIELD.toLowerCase() );
		
	}
	
	/*
	 * <dmdSec ID="MODSMD_ISSUE1">
		<mdWrap MIMETYPE="text/xml" MDTYPE="MODS">
			<xmlData>
				<mods:mods xmlns:mods="http://www.loc.gov/mods/v3">
					<mods:titleInfo ID="MODSMD_ISSUE1_TI1" xml:lang="de">
						<mods:nonSort>Das</mods:nonSort>
						<mods:title>Luxemburger Land in Wort und Bild.</mods:title>
						<mods:partNumber>Jg. 1, n� 25</mods:partNumber>
					</mods:titleInfo>
					
		<dmdSec ID="MARCMD_ALEPHSYNC">
			<mdWrap MIMETYPE="text/xml" MDTYPE="MARC">
				<xmlData>
					<record xmlns="http://www.loc.gov/MARC21/slim">
						<leader>ntm 22 4u 4500</leader>
						<controlfield tag="FMT">BK</controlfield>
						<controlfield tag="LDR">ntm 22 4u 4500</controlfield>
						<controlfield tag="001">001094930:LUX01</controlfield>
	 */
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.currentContent = new StringBuffer();
		
		// Small optimization
		// CAREFUL, have to lowercase to avoid issues and be more flexible
		if ( ! this.supportedTags.contains(qName.toLowerCase()) ) return; 
		
		if ( MetsConstant.TAG_DMDSEC.equalsIgnoreCase(qName) ) {
			
			this.inDmdSec = true;
			 
			//  /METS:mets/METS:dmdSec[@ID=$issue_dmdid]//METS:xmlData/MODS:mods
			String id = attributes.getValue( MetsConstant.ATTR_NAME_ID );
			this.currentDmd = new DmdSection(id);
			this.dmds.put(id, currentDmd);
		
		} else if ( MetsConstant.TAG_MODS_IDENTIFIER.equalsIgnoreCase(qName) ) {
			
			String type = attributes.getValue( MetsConstant.ATTR_MODS_TYPE );
			
			if (MetsConstant.ATTR_MODS_TYPE.equalsIgnoreCase( type )) {
				this.inLocalIdentifier = true;
			}
			
			if (MetsConstant.ATTR_MODS_TYPE.equalsIgnoreCase( type )) {
				this.inBarcodeIdentifier = true;
			}
			
		} else if ( MetsConstant.TAG_MODS_TITLEINFO.equalsIgnoreCase(qName) ) {
			
			this.inTitleInfo = true;
			
		} else if ( MetsConstant.TAG_MODS_NAME.equalsIgnoreCase(qName) ) {
			
			// METS: name
			/*  <mods:name ID="MODSMD_CHAP28_N1" type="personal">
					<mods:namePart type="given">V.</mods:namePart>
					<mods:namePart type="family">N</mods:namePart>
					<mods:role>
						<mods:roleTerm>aut</mods:roleTerm>
					</mods:role>
				</mods:name>
			*/
			this.currentAuthor = new Author();
			
		} else if ( MetsConstant.TAG_MODS_NAMEPART.equalsIgnoreCase(qName) ) {
			
			// METS: Namepart
			String type = attributes.getValue( MetsConstant.ATTR_MODS_TYPE );
		
			this.currentNamePart = new Pair(type, null);
			
		} else if ( MetsConstant.TAG_MODS_ORIGININFO.equalsIgnoreCase(qName) ) {
			
			// METS: <MODS:originInfo displayLabel="manufacturer"> <MODS:publisher>[PRINTER]</MODS:publisher> </MODS:originInfo> 
			String displayLabel = attributes.getValue( MetsConstant.ATTR_MODS_ORIGININFO_DISPLAYLABEL );
		
			if (MetsConstant.ATTR_ORIGININFO_DISPLAYLABEL_MANUFACTURER.equalsIgnoreCase(displayLabel)) {
				this.inOriginInfoManufacturer = true;
			}
			
			
		} else if ( MetsConstant.TAG_DMDSEC_MDWRAP.equalsIgnoreCase(qName) ) {
			// <dmdSec ID="MARCMD_ALEPHSYNC_CONT1">
			//    <mdWrap MIMETYPE="text/xml" MDTYPE="MARC">
			
			if (this.inDmdSec && this.currentDmd != null) {
				
				String mdType = attributes.getValue( MetsConstant.ATTR_NAME_MDTYPE );

				this.currentDmd.mdType = mdType;

				if ( MetsConstant.ATTR_DMDSEC_MDTYPE_MARC.equalsIgnoreCase(mdType) ) {
					this.inMarc = true;
				}
				
			}
			
		} else if (this.inMarc && this.currentDmd != null) {
			
			if ( MetsConstant.TAG_MARC_CONTROLFIELD.equalsIgnoreCase(qName) ) {
				
				String tag = attributes.getValue( MetsConstant.ATTR_MARC_TAG );
				
				this.currentTag = tag;
				
			} else if ( MetsConstant.TAG_MARC_DATAFIELD.equalsIgnoreCase(qName) ) {
				
				String tag = attributes.getValue( MetsConstant.ATTR_MARC_TAG );
				String ind1 = attributes.getValue( MetsConstant.ATTR_MARC_IND1 );
				String ind2 = attributes.getValue( MetsConstant.ATTR_MARC_IND2 );
				
				this.currentDataField = new DataField(tag, ind1, ind2);
				
			} else  if ( MetsConstant.TAG_MARC_SUBFIELD.equalsIgnoreCase(qName) ) {
				
				String code = attributes.getValue( MetsConstant.ATTR_MARC_CODE );
				
				this.currentCode = code;
				
			}
			
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		// Small optimisation, return if we are not in an actual <dmdSec>
		if ( ! this.inDmdSec ) {
			return;
		}
		
		if ( MetsConstant.TAG_DMDSEC.equalsIgnoreCase(qName) ) {
			
			//currentDmdSecId = null;
			this.inDmdSec = false;
			
			this.inMarc = false;
			
		} else if ( MetsConstant.TAG_MODS_TITLEINFO.equalsIgnoreCase(qName) ) {
			
			this.inTitleInfo = false;
			
		} else if (currentDmd != null) {
			// Could continue to add everything
			
			String content = this.currentContent.toString(); 
			
			if ( MetsConstant.TAG_MODS_IDENTIFIER.equalsIgnoreCase(qName) ) {
				
				if (this.inLocalIdentifier) {
					this.currentDmd.setIdentifierLocal(content);
				}
				
				if (this.inBarcodeIdentifier) {
					this.currentDmd.setIdentifierBarcode(content);
				}
				
				// Reset state
				this.inLocalIdentifier = false;
				this.inBarcodeIdentifier = false;
			
			} else if ( MetsConstant.TAG_MODS_RECORDIDENTIFIER.equalsIgnoreCase(qName) ) {
				
				this.currentDmd.setRecordIdentifer(content);
				
			} else if ( MetsConstant.TAG_MODS_DATEISSUED.equalsIgnoreCase(qName) ) {
				
				this.currentDmd.setDateIssued(content);
				
			} else if ( MetsConstant.TAG_MODS_PUBLISHER.equalsIgnoreCase(qName) ) {
				
				if (this.inOriginInfoManufacturer) {
					this.currentDmd.setPrinter(content);
				} else {
					this.currentDmd.setPublisher(content);
				}
				
			} else if ( MetsConstant.TAG_MODS_ORIGININFO.equalsIgnoreCase(qName) ) {	
				
				// Reset state
				this.inOriginInfoManufacturer = false;
				
			} else if ( MetsConstant.TAG_MODS_LANGUAGETERM.equalsIgnoreCase(qName) ) {
				
				this.currentDmd.languages.add(content);
				
			} else if ( MetsConstant.TAG_MODS_NAME.equalsIgnoreCase(qName) ) {

				// NEW - SUPPORT N AUTHORS
				if (this.currentAuthor != null) {
					
					this.currentDmd.authors.add(currentAuthor);
					
					this.currentAuthor = null;
				}
				
			} else if ( MetsConstant.TAG_MODS_NAMEPART.equalsIgnoreCase(qName) ) {
				
				this.currentNamePart.value = content;
				
				this.currentAuthor.nameParts.add( new Pair(this.currentNamePart.key, this.currentNamePart.value) );
				
			} else if ( this.inTitleInfo ) {
				
				// All tags added (mods:title, mods:subTitle, ...)
				
				Pair titleInfoPart = new Pair(qName, content);
				this.currentDmd.titleInfoParts.add( titleInfoPart );
				
			} else if ( MetsConstant.TAG_MARC_CONTROLFIELD.equalsIgnoreCase(qName) ) {
				// Controlfielde end. Just add content for tag in map
				
				this.currentDmd.controlFields.put(this.currentTag, content);
				
				this.currentTag = null;
				
			} else if ( MetsConstant.TAG_MARC_DATAFIELD.equalsIgnoreCase(qName) ) {
				// Datafield end. Reset the currentTag.
				
				//this.currentTag = null;
				
				this.currentDmd.addDataField(this.currentDataField);
				
				this.currentDataField = null;
			
			} else if ( MetsConstant.TAG_MARC_SUBFIELD.equalsIgnoreCase(qName) ) {
				// Subfield end. Reset currentCode, keep currentTag.
				
				//this.currentDmd.dataFields.get(this.currentTag).put(this.currentCode, content);
				
				this.currentDataField.put(this.currentCode, content);
				
				this.currentCode = null;
			}
			
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( this.inDmdSec ) {
			this.currentContent.append(ch, start, length);
		}
	}

	//================================================================================
	//================================================================================
	
	//================================================================================
	// Getters and Setters
	//================================================================================

	public DmdSection getDmdSectionPrint() {
		return dmds.get( MetsConstant.ATTR_PRINT_ID );
	}
	
	public DmdSection getDmdSectionCollection() {
		return dmds.get( MetsConstant.ATTR_COLLECTION_ID );
	}
	
	public DmdSection getDmdSection(String id) {
		return dmds.get(id);
	}
}
