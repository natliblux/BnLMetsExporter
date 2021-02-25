/*******************************************************************************
 * Copyright (C) 2021 Bibliothèque nationale de Luxembourg (BnL)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.model.marc.MarcControlFieldDTO;
import lu.bnl.domain.model.marc.MarcDataFieldDTO;
import lu.bnl.domain.model.marc.MarcRecordDTO;
import lu.bnl.domain.model.marc.MarcSubFieldDTO;

public class MarcSectionHandler extends DefaultHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MarcSectionHandler.class);

    private HashSet<String> supportedTags;

    // Parsing State
    // ##################################################
    
    private boolean inMarcSection = false;

    // Current Data
    // ##################################################

    private MarcControlFieldDTO currentControlField;

    private MarcDataFieldDTO currentDataField;

    private MarcSubFieldDTO currentSubField;

    private StringBuffer currentContent;

    // Result Data
    // ##################################################

    private MarcRecordDTO marcRecordDTO;
    
    public MarcSectionHandler() {
        this.supportedTags = new HashSet<>();
        this.supportedTags.add( MetsConstant.TAG_DMDSEC.toLowerCase() );

        this.supportedTags.add( MetsConstant.TAG_MARC_LEADER.toLowerCase() );
        this.supportedTags.add( MetsConstant.TAG_MARC_CONTROLFIELD.toLowerCase() );
        this.supportedTags.add( MetsConstant.TAG_MARC_DATAFIELD.toLowerCase() );
        this.supportedTags.add( MetsConstant.TAG_MARC_SUBFIELD.toLowerCase() );
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ( ! this.supportedTags.contains(qName.toLowerCase()) ) return;

        if ( qName.equalsIgnoreCase( MetsConstant.TAG_DMDSEC ) ) {

            logger.info("Marc Section handler DMDSEC");

            this.handleDmdSec(uri, localName, qName, attributes);

        } else if (inMarcSection) {

            logger.info("In MarcSection: " + qName);

            if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_LEADER ) ) {

                this.handleLeader(uri, localName, qName, attributes);

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_CONTROLFIELD ) ) {

                this.handleControlField(uri, localName, qName, attributes);

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_DATAFIELD ) ) {

                this.handleDataField(uri, localName, qName, attributes);

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_SUBFIELD ) ) {

                this.handleSubField(uri, localName, qName, attributes);

            }

        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ( ! this.supportedTags.contains(qName.toLowerCase()) ) return;

        if ( qName.equalsIgnoreCase( MetsConstant.TAG_DMDSEC ) ) {

            this.handleEndDmdSec();

        } else if (inMarcSection) {
            
            if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_LEADER ) ) {

                this.handleEndLeader();

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_CONTROLFIELD ) ) {

                this.handleEndControlField();

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_DATAFIELD ) ) {

                this.handleEndDataField();

            } else if ( qName.equalsIgnoreCase( MetsConstant.TAG_MARC_SUBFIELD ) ) {

                this.handleEndSubField();

            }

        }


    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inMarcSection) {
            this.currentContent.append(ch, start, length);
        }
    }

    // TAG HANDLER METHODS
	//================================================================================
    
    private void handleDmdSec(String uri, String localName, String qName, Attributes attributes) {
        // Only handle the main MARCMD_ALEPCHSYNC
        logger.info(attributes.getValue(MetsConstant.ATTR_ID));
        if (attributes.getValue(MetsConstant.ATTR_ID).equalsIgnoreCase(MetsConstant.VALUE_MARC_ALEPHSYNC)) {
            logger.info("Found MARCMD_ALEPHSYNC");
            this.inMarcSection = true;
            this.resetCurrentString();
            this.marcRecordDTO = new MarcRecordDTO();
        }
    }

    private void handleEndDmdSec() {
        logger.info("End DMDSec");
        this.inMarcSection = false;
    }

    private void handleLeader(String uri, String localName, String qName, Attributes attributes) {
        this.resetCurrentString();
    }

    private void handleEndLeader() {
        logger.info("MARC Record: Found Leader:" + this.currentContent.toString());
        this.marcRecordDTO.setLeader(this.currentContent.toString());
    }

    private void handleControlField(String uri, String localName, String qName, Attributes attributes) {
        this.resetCurrentString();

        this.currentControlField = new MarcControlFieldDTO();
        this.currentControlField.setTag( attributes.getValue(MetsConstant.ATTR_MARC_TAG) );

        if (this.marcRecordDTO.getControlFields() == null) {
            this.marcRecordDTO.setControlFields(new ArrayList<>());
        }

        this.marcRecordDTO.getControlFields().add(this.currentControlField);
    }

    private void handleEndControlField() {
        this.currentControlField.setContent(this.currentContent.toString());
        this.currentControlField = null;
    }

    private void handleDataField(String uri, String localName, String qName, Attributes attributes) {
        this.currentDataField = new MarcDataFieldDTO();
        this.currentDataField.setTag( attributes.getValue(MetsConstant.ATTR_MARC_TAG) );
        this.currentDataField.setInd1( attributes.getValue(MetsConstant.ATTR_MARC_IND1) );
        this.currentDataField.setInd2( attributes.getValue(MetsConstant.ATTR_MARC_IND2) );
        this.currentDataField.setSubFields(new ArrayList<>());

        if (this.marcRecordDTO.getDataFields() == null) {
            this.marcRecordDTO.setDataFields(new ArrayList<>());
        }

        this.marcRecordDTO.getDataFields().add(this.currentDataField);
    }

    private void handleEndDataField() {
        this.currentDataField = null;
    }

    private void handleSubField(String uri, String localName, String qName, Attributes attributes) {
        this.resetCurrentString();

        this.currentSubField = new MarcSubFieldDTO();
        this.currentSubField.setCode( attributes.getValue(MetsConstant.ATTR_MARC_CODE) );

        this.currentDataField.getSubFields().add(this.currentSubField);
    }

    private void handleEndSubField() {
        this.currentSubField.setContent(this.currentContent.toString());
        this.currentSubField = null;
    }

    // Helper Methods
    //================================================================================

    private void resetCurrentString() {
        this.currentContent = new StringBuffer();
    }

    // Public Methods To Get and Set Results
	//================================================================================
    
    public MarcRecordDTO getMarcRecordDTO() {
        return this.marcRecordDTO;
    }


}
