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

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.model.Collection;

public class ModsCollectionDescriptiveSectionHandler extends DefaultHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ModsCollectionDescriptiveSectionHandler.class);

    private HashSet<String> supportedTags;

    // Parsing State
    // ##################################################

    private boolean inModsMdCollecton = false;

    private boolean inIdentifier = false;

    private boolean inTitle = false;

    // Current Data
	// ##################################################

    private StringBuilder currentText = new StringBuilder(); 

    // Result Data
    // ##################################################
    
    private Collection collection;

    public ModsCollectionDescriptiveSectionHandler() {
        this.supportedTags = new HashSet<>();
        this.supportedTags.add( MetsConstant.TAG_DMDSEC.toLowerCase() );
        this.supportedTags.add( MetsConstant.TAG_MODS_IDENTIFIER.toLowerCase() );
        this.supportedTags.add( MetsConstant.TAG_MODS_TITLE.toLowerCase() );
    }

    @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ( ! this.supportedTags.contains(qName.toLowerCase()) ) return;

        if ( qName.equalsIgnoreCase( MetsConstant.TAG_DMDSEC ) ) {

            this.handleDmdSec(uri, localName, qName, attributes);

        } else if (this.inModsMdCollecton) {

            if ( qName.equalsIgnoreCase( MetsConstant.TAG_MODS_IDENTIFIER ) ) {

                this.handleIdentifier(uri, localName, qName, attributes);

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MODS_TITLE ) ) {

                this.handleTitle(uri, localName, qName, attributes);

            }

        }

    }


    @Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
        if ( ! this.supportedTags.contains(qName.toLowerCase()) ) return;

        if ( qName.equalsIgnoreCase( MetsConstant.TAG_DMDSEC ) ) {

            this.handleDmdSecEnd(uri, localName, qName);

        } else if (this.inModsMdCollecton) {
            
            if ( qName.equalsIgnoreCase( MetsConstant.TAG_MODS_IDENTIFIER ) ) {

                this.handleIdentifierEnd(uri, localName, qName);

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MODS_TITLE ) ) {

                this.handleTitleEnd(uri, localName, qName);

            }

        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.inModsMdCollecton) {
            this.currentText.append(new String(ch, start, length));
        }
    }

    // TAG HANDLER METHODS
    //================================================================================
    
    private void handleDmdSec(String uri, String localName, String qName, Attributes attributes) {

        String id = attributes.getValue( MetsConstant.ATTR_ID );

        if ( id.equalsIgnoreCase( MetsConstant.VALUE_MODSMD_COLLECTION ) ) {
            this.inModsMdCollecton = true;
            this.collection = new Collection();
            logger.info("Found DmdSec MODSMD_COLLECTION");
        }

    }

    private void handleDmdSecEnd(String uri, String localName, String qName) {
        this.inModsMdCollecton = false;
    }


    private void handleIdentifier(String uri, String localName, String qName, Attributes attributes) {
        resetCurrentText();
        
        String type = attributes.getValue( MetsConstant.ATTR_MODS_TYPE );

        System.out.println(qName + " - " + type);

        if (type.equalsIgnoreCase( MetsConstant.VALUE_IDENTIFIER_LOCAL ) ) {
            this.inIdentifier = true;
        }

    }

    private void handleIdentifierEnd(String uri, String localName, String qName) {
        if (this.inIdentifier) {
            this.collection.setPaperId(getCurrentText());
            this.inIdentifier = false;
        }
    }


    private void handleTitle(String uri, String localName, String qName, Attributes attributes) {
        resetCurrentText();
        this.inTitle = true;
    }

    private void handleTitleEnd(String uri, String localName, String qName) {
        if (this.inTitle) {
            this.collection.setTitle(getCurrentText());
            this.inTitle = false;
        }
    }


    // Helper Methods
    //================================================================================

    private void resetCurrentText() {
        this.currentText = new StringBuilder();
    }

    private String getCurrentText() {
        return this.currentText.toString();
    }

    // Public Methods To Get and Set Results
    //================================================================================
    
    public Collection getCollection() {
        return this.collection;
    }

}