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
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.configuration.ExportConfig;
import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.constants.MetsTypeHandler;
import lu.bnl.domain.model.DivSection;
import lu.bnl.domain.model.XmlParserStackElement;

public class MetsLogicalStructureHandler extends DefaultHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(MetsLogicalStructureHandler.class);
	
	private List<String> supportedMetsTypes; 
	
	// Data
	//================================================================================
	
	private Map<String, DivSection> articles = new HashMap<>();
	
	private Map<String, List<String>> articleAltoBlockMap = new HashMap<>();
	
	@Deprecated // The new requirements to not have issues anymore. This is for some backward comptability.
	private String issueDmdId = null;
	
	// State
	//================================================================================
	
	private boolean isSupported = false;
	
	private boolean inLogicalStructure = false;
	
	private MetsTypeHandler metsTypeHandler = null;
	
	// Add all elements to this stack
	private Stack<XmlParserStackElement> elementStack = new Stack<>();
	
	// Only add article elements to this stack
	private Stack<DivSection> articleStack = new Stack<>();
	
	//================================================================================
	
	public MetsLogicalStructureHandler(List<String> supportedMetsTypes) {
		this.supportedMetsTypes = supportedMetsTypes;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		this.push(uri, localName, qName, attributes);
		
		if (this.inLogicalStructure) {
			
			this.handleLogicalStructure(uri, localName, qName, attributes);
			
			return; // No need to continue after this.
		} else if ( MetsConstant.TAG_STRUCTURAL_MAP.equalsIgnoreCase(qName) ) {
			
			String type = attributes.getValue( MetsConstant.ATTR_NAME_TYPE );
			
			if ( MetsConstant.ATTR_TYPE_LOGICAL.equalsIgnoreCase(type) ) {
				this.inLogicalStructure = true;
			}
			
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (this.inLogicalStructure) {
			if ( MetsConstant.TAG_STRUCTURAL_MAP.equalsIgnoreCase(qName) ) { // End of logical or physical structure
				this.inLogicalStructure = false;
			}			
			
		}
		
		this.pop();
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
	}
	
	//================================================================================
	//================================================================================
	
	private void push(String uri, String localName, String qName, Attributes attributes) {
		XmlParserStackElement stackElement = new XmlParserStackElement(uri, localName, qName, attributes);
		this.elementStack.push(stackElement);
	}
	
	private void pop() {
		// Only Pop element when done. 
		// NOTE: Every start of element pushes a Tag, so this will never be called on an empty stack!
		XmlParserStackElement stackElement = this.elementStack.pop();
		
		// If the popped element has the same article as the one on the article stack, then
		// remove the article from the article stack. The next item becomes the new current article
		// because it is on top of the stack.
		
		if ( ! this.articleStack.isEmpty() ) {
			
			// NOTE: This can be called when the articleStack is empty. Check to avoid exception.
			DivSection currentArticle = this.articleStack.peek();
			
			// Check for null, because data is optional
			if ( stackElement.data != null ) {
				
				// Finally compare elements if both have a value.
				if ( currentArticle.equals(stackElement.data) ) {
					this.articleStack.pop();
				}
			}
		}
	}
	
	private void handleLogicalStructure(String uri, String localName, String qName, Attributes attributes) {
		
		if (  MetsConstant.TAG_DIV.equalsIgnoreCase(qName) ) {
			
			// Example: <div ID="DTL75" TYPE="SECTION" DMDID="MODSMD_SECTION1" LABEL="Budget-Bérde">
			// Example: <div ID="DTL232" TYPE="Serial" LABEL="Recueil des mémoires et des travaux ...">
			
			String type 	= attributes.getValue( MetsConstant.ATTR_NAME_TYPE );
			String id 		= attributes.getValue( MetsConstant.ATTR_NAME_ID );
			String dmdid 	= attributes.getValue( MetsConstant.ATTR_NAME_DMDID );
			String label 	= attributes.getValue( MetsConstant.ATTR_NAME_LABEL );
			
			// Only handle tags with a TYPE
			if ( type != null ) {

				// Try to detect the type until it has been found.
				if ( this.metsTypeHandler == null ) {
					
					this.detectExportType(type);
					
				// Else, check if we have to handle
				} else if ( this.metsTypeHandler.contains(type) ) {
					DivSection article = new DivSection(id, dmdid, label, type); // create new article and push on stack
					
					if (this.articles.containsKey(id)) {
						logger.warn(String.format("Article with key %s already exists!", id));
					}
					
					// Keep the article in a list. ID is unique.
					this.articles.put(id, article);
					
					// Add the article to the stack of articles
					this.articleStack.push(article);
					
					// Add the article to the last stack element
					XmlParserStackElement stackElement = this.elementStack.peek();
					stackElement.data = article;
				}
				
				// TODO: To be refactored
				if ( MetsConstant.ATTR_TYPE_ISSUE.equalsIgnoreCase(type) ) {
					//div ID="DTL66" TYPE="ISSUE" DMDID="MODSMD_ISSUE1
					this.issueDmdId = dmdid;
				}
				
			}
			
		} else if (  MetsConstant.TAG_AREA.equalsIgnoreCase(qName) ) {
			
			String begin 	= attributes.getValue( MetsConstant.ATTR_NAME_BEGIN );
			String fileid 	= attributes.getValue( MetsConstant.ATTR_NAME_FILEID );
			
			String key = fileid + "-" + begin; 
			
			DivSection currentArticle = null;
			try {
				currentArticle = this.articleStack.peek();
			} catch (EmptyStackException exception) {
				logger.warn(String.format("EmptyStackException has occured. %s", exception.getMessage()));
			}
			
			// TO CHECK
			if (currentArticle != null) { // use last article on the stack and put block with key
				
				currentArticle.getBlocks().put(key, new StringBuilder());
				currentArticle.getBlocksForLines().put(key, new StringBuilder());
				
				//this.currentArticle.getBlocks().put(begin, new StringBuilder()); // begin should be key of the block
				//this.articles.put(key, this.currentArticle); // NO, already did before
				
				// Keep track of fileid/begin and article id
				if ( ! this.articleAltoBlockMap.containsKey(key) ) {
					this.articleAltoBlockMap.put(key, new ArrayList<String>());
				}
				this.articleAltoBlockMap.get(key).add(currentArticle.getId());
			}
			
		}
		
	}
	
	/** Tries to identify the type of the current METS file based on the given
	 *  attribute TYPE. Makes use of the ExportConfig from the AppConfigurationManager.
	 *  If no MetsTypeHandler matches, then nothing happens and this.metsTypeHandler
	 *  stay unchanged, meaning that it will probably stay null.
	 * 
	 * @param typeName
	 */
	private void detectExportType(String typeName) {
		ExportConfig ec = AppConfigurationManager.getInstance().getExportConfig();
		MetsTypeHandler mth = ec.getMetsTypeHandlerByKey( typeName.toLowerCase() );

		if ( mth != null ) {
			
			// Find if it is supported (exportDublinCore or exportSolr config)
			boolean supported = false;
			for (String smt : this.supportedMetsTypes) {
				if (smt.equalsIgnoreCase(typeName)) {
					supported = true;
				}
			}
			
			if (supported) {
				this.isSupported = true;
				
				this.metsTypeHandler = mth;
				
				logger.info(String.format("Mets Type Detected: %s", mth.getTypeName()));
				//System.out.println("1 TYPE OF METS: " + metsTypeHandler.getTypeName() ); // TODO: REMOVE
			}
		}
		
        //System.out.println("2 TYPE OF METS: " + metsTypeHandler.getTypeName() ); // TODO: REMOVE
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================
	
	public boolean isSupported() {
		return this.isSupported;
	}
	
	public MetsTypeHandler getMetsTypeHandler() {
		return this.metsTypeHandler;
	}
	
	public Map<String, DivSection> getArticles() {
		return this.articles;
	}
	
	public Map<String, List<String>> getArticleAltoBlockMap() {
		return this.articleAltoBlockMap;
	}
	
	public String getIssueDmdId() {
		return this.issueDmdId;
	}
}
