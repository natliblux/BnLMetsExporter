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
package lu.bnl.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.bnl.domain.constants.AltoConstant;
import lu.bnl.domain.model.AltoHyphenation;
import lu.bnl.domain.model.AltoLine;
import lu.bnl.domain.model.AltoWord;
import lu.bnl.domain.model.DivSection;
import lu.bnl.domain.model.XmlParserStackElement;
import lu.bnl.util.LuceneUtils;
import lu.bnl.util.TextProcessor;

/**
 * Parse and create full text.
 * Improve this by just parsing and preparing the data, then
 * externalize the string management.
*/
public class AltoXMLParserHandler extends DefaultHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(AltoXMLParserHandler.class);
	
	private LuceneUtils lu = new LuceneUtils();
	
	private String fileid;
	
	private boolean useTokenizer = false;
	
	// Data
	//================================================================================
	
	private Map<String, List<String>> articleAltoBlockMap;
	
	private Map<String, DivSection> articles;
	
	private List<AltoWord> pageWords = new ArrayList<>();
	
	private StringBuilder pageTextForLines = new StringBuilder();
	
	// State
	//================================================================================
	
	// Add all elements to this stack
	private Stack<XmlParserStackElement> elementStack = new Stack<>();
	
	private Stack<DivSection> stackArticle = new Stack<DivSection>();
	
	private List<AltoWord> consecutiveTags = new ArrayList<AltoWord>();
	
	
	private String currentTextBlockId;
	
	private String previousSiblingTag;
	
	private DivSection dummyArticle;
	
	
	private StringBuilder currentArticleText = new StringBuilder();
	
	private StringBuilder currentArticleTextForLines = new StringBuilder();
	
	private AltoLine currentAltoLine;
	
	private AltoHyphenation currentAltoHyphenation;
	
	private Boolean didFindHyphen = false;
	
	//private Boolean DEBUG_isInDummyArticle = false; // For debugging words landing in the dummy article
	
	// Flag to track if we are in a TextBlock that is part of an exportable element.
	// Goal: Do not include header of newspapers inside full text, only the articles, etc.
	//private Boolean inRelevantTextBlock = false;
	
	//  Constants
	//================================================================================

	private static final String SPACE 			= " ";

	private static final String HTML_BLOCK_END 	= "</block>";

	private static final String HTML_BREAK 		= "<br/>";


	//================================================================================
	
	public AltoXMLParserHandler(String fileid, Map<String, List<String>> articleAltoBlockMap, Map<String, DivSection> articles, boolean useTokenizer) {
		this.fileid = fileid;
		this.useTokenizer = useTokenizer;

		this.articleAltoBlockMap = articleAltoBlockMap;
		this.articles = articles;
		
		//Create a dummy article to maintain sync with open/close tag
		this.dummyArticle = new DivSection("0", "00", null, null);
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// Push directly element data to the stack. Then, next method can access it with peek() method.
		XmlParserStackElement stackElement = new XmlParserStackElement(uri, localName, qName, attributes);
		this.elementStack.push(stackElement);
		
		// Can clear remember list if no tag <String>
		if ( !AltoConstant.TAG_STRING.equalsIgnoreCase(qName) ) {
			consecutiveTags.clear();
		}
		
		if (AltoConstant.TAG_STRING.equalsIgnoreCase(qName)) {
			
			this.handleString(uri, localName, qName, attributes);

		} else if(AltoConstant.TAG_SP.equals(qName)) {
			
			this.handleSpace(uri, localName, qName, attributes);
		
		} else if(AltoConstant.TAG_HYP.equals(qName)) {
			
			this.handleHyphen(uri, localName, qName, attributes);
				
		} else if (AltoConstant.TAG_TEXTBLOCK.equalsIgnoreCase(qName) || AltoConstant.TAG_COMPOSEDBLOCK.equalsIgnoreCase(qName)) {
			
			this.handleTextBlock(uri, localName, qName, attributes);
			
		} else if(AltoConstant.TAG_TEXTLINE.equals(qName)) {
			
			this.handleTextLine(uri, localName, qName, attributes);
			
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (AltoConstant.TAG_TEXTBLOCK.equals(qName) || AltoConstant.TAG_COMPOSEDBLOCK.equals(qName)) {
			
			this.handleTextBlockEnd(uri, localName, qName);

		}
		
		previousSiblingTag = qName;
	}
	
	
	// Parser Helper Methods
	//================================================================================
	
	/**
	 * Handle TextBlock Tag.
	 * 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 */
	private void handleTextBlock(String uri, String localName, String qName, Attributes attributes) {
		// Example: <TextBlock ID="P1_TB00014" HPOS="2949" VPOS="702" WIDTH="934" HEIGHT="3097" STYLEREFS="TXT_9 PAR_BLOCK">
		
		String id = attributes.getValue("ID");

		this.currentTextBlockId = id;
		String key = this.fileid + "-" + id; 
		
		DivSection article = null;
		
		if ( this.articleAltoBlockMap.containsKey(key) ) {
			List<String> articleIDs = this.articleAltoBlockMap.get(key); // Why is does this return a List?
			logger.debug("Articles: " + articleIDs.size());
			for (String s : articleIDs) {
				logger.debug("ARTICLE ID FROM MAP: " + s + " - KEY: " + key);
			}
			
			article = articles.get( articleIDs.get(0) ); // Return the value, or null if no mapping found
		}
		
		if (article != null) {
			// New article
			
			this.prepareForNewArticle(article, key);
			
		} else {
			// 1) This code is run when no article with `key` has been found in `articleAltoBlockMap`.
			// 2) It also run on and TB inside a CB, where the CB is referenced in the METS
			
			// BUG "komesch" Article contains text that belongs to the newspaper and no particular article...
			// Somehow: Have to ignore TB belonging to no article...
			
			//Stack the same again to be sure to keep parsing alignment (see .pop in endElement)
			if ( !stackArticle.isEmpty() ) {
				article = stackArticle.peek();
				
				logger.warn("ARTICLE SHOULD NOT BE PEEKED! " + id);
			}
			
		}
		
		//DEBUG_isInDummyArticle = article == null;
		if (article == null) {
			//Use a dummy article to maintain sync with open/close tag
			article = this.dummyArticle;
			
			this.prepareForNewArticle(article, key);
			
			//logger.warn("EMPTY DUMMY ARTICLE!");
		}

		// Add blocks start string
		String blockHtml = String.format("<block page=\"%s\" begin=\"%s\">", this.fileid, this.currentTextBlockId);
		this.currentArticleTextForLines.append(blockHtml);
		this.pageTextForLines.append(blockHtml);
		
		stackArticle.add(article);
	}

	private void handleTextBlockEnd(String uri, String localName, String qName) {

		if (this.currentTextBlockId != null) {
			this.currentArticleTextForLines.append(HTML_BLOCK_END);
			this.pageTextForLines.append(HTML_BLOCK_END);	
		}

		// Add <br/> at the end of TextBlocks so that titles and main paragraphs are spaced. 
		this.currentArticleTextForLines.append(HTML_BREAK);
		this.pageTextForLines.append(HTML_BREAK);
		
		this.currentTextBlockId = null;

		stackArticle.pop();
	}
	
	/**
	 * Handle the SP Tag. It defines a space.
	 * Adds a space symbol to the current text.
	 * 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 */
	private void handleSpace(String uri, String localName, String qName, Attributes attributes) {
		this.currentArticleText.append(SPACE);
		this.currentArticleTextForLines.append(SPACE);
		this.pageTextForLines.append(SPACE);
		this.currentAltoLine.getBuffer().append(SPACE);
	}
	
	/**
	 * Handle a TextLine Tag.
	 * 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param attributes
	 */
	private void handleTextLine(String uri, String localName, String qName, Attributes attributes) {
		//Example: <TextLine ID="P1_TL00018" HPOS="85" VPOS="1505" WIDTH="1081" HEIGHT="39">
		String id = attributes.getValue("ID");
		
		String articleId = null;
		DivSection currentArticle = null;
		
		if ( !stackArticle.isEmpty() ) { 
			currentArticle = stackArticle.peek();
		}
		
		if (currentArticle != null) {
			// DMDID is the key to use as article
			// Example: <div ID="DIVL39" TYPE="ARTICLE" DMDID="MODSMD_ARTICLE4" LABEL="Der arme Frieden " ORDER="4">
			articleId = currentArticle.getDmdid();
		}
		
		this.currentAltoLine = new AltoLine(id, articleId);
		this.currentAltoLine.setBlockId(currentTextBlockId);
		
		if (currentArticleText != null && currentArticleText.length() > 0 
				&& currentArticleText.charAt(currentArticleText.length() -1) != ' ' 
				&& this.didFindHyphen == false ) {
			// Add space at the end of a line (OR the start of a the next, except first)
			currentArticleText.append(SPACE);
			
			this.currentArticleTextForLines.append(HTML_BREAK);
			this.pageTextForLines.append(HTML_BREAK);
		}
		
		this.didFindHyphen = false;
	}
	
	private void handleString(String uri, String localName, String qName, Attributes attributes) {
		// Example: <String ID="P1_ST02021" HPOS="2959" VPOS="1320" WIDTH="89" HEIGHT="33" CONTENT="diesen" WC="0.60" CC="405750"/>
		
		XmlParserStackElement lastElement = null;
		if ( this.elementStack.size() > 1 ) { 										// If there is at least 2 elements
			lastElement = this.elementStack.get( this.elementStack.size() - 2 );	// Take the element before the current pushed one. (size()-1 is the last)
		}
		

		String id 				= attributes.getValue( AltoConstant.ATTR_STRING_ID );
		String content 			= attributes.getValue( AltoConstant.ATTR_STRING_CONTENT ); // MAIN TEXT IS HERE, no need character method on handler
		
		// Pre-Process the String content
		content = TextProcessor.escapeDangerousHtmlCharacters(content);
		
		String contentForLine 	= content;

		Integer y = integer(attributes.getValue( AltoConstant.ATTR_STRING_VPOS ));
		Integer x = integer(attributes.getValue( AltoConstant.ATTR_STRING_HPOS ));
		Integer w = integer(attributes.getValue( AltoConstant.ATTR_STRING_WIDTH ));
		Integer h = integer(attributes.getValue( AltoConstant.ATTR_STRING_HEIGHT ));
		
		// Example: <String ID="P1_ST02164" HPOS="3823" VPOS="1897" WIDTH="33" HEIGHT="27" CONTENT="Ge" SUBS_TYPE="HypPart1" SUBS_CONTENT="Gewissensfreiheit" WC="0.49" CC="761"/>
		
		String subType = attributes.getValue( AltoConstant.ATTR_STRING_SUBSTYPE ); //HypPart1
		if (subType != null) {
			String subContent = attributes.getValue( AltoConstant.ATTR_STRING_SUBSCONTENT );
			subContent = TextProcessor.escapeDangerousHtmlCharacters(subContent);
			
			if (subContent != null) {
				contentForLine = subContent;
			}
			
			// Example: <String ID="S13" STYLEREFS="TXT_2" HPOS="672" VPOS="163" HEIGHT="16" WIDTH="51" WC="0.99" CONTENT="Jeu-" SUBS_TYPE="HypPart1" SUBS_CONTENT="Jeunessefelde"/>
			// Example: <String ID="S14" STYLEREFS="TXT_3" HPOS="132" VPOS="181" HEIGHT="16" WIDTH="123" WC="0.99" CONTENT="nessefelde" SUBS_TYPE="HypPart2" SUBS_CONTENT="Jeunessefelde"/>
			if ("HypPart1".equalsIgnoreCase(subType)) {
				
				// Save hyphenation to replace later (last HyphenWord  will be replaced by HyphenEnd)
				this.currentAltoLine.setHyphenStart(content);
				this.currentAltoLine.setHyphenStartWord(subContent); // The real complete word
				
				this.didFindHyphen = true;
				
				this.currentAltoHyphenation = new AltoHyphenation();
				this.currentAltoHyphenation.setFull(subContent);
				this.currentAltoHyphenation.setStart(content);
				
				// [C1] Replace with content (=whole word)
				content = subContent;
				
			} else if ("HypPart2".equalsIgnoreCase(subType)) {
				
				this.currentAltoLine.setHyphenEnd(content);
				this.currentAltoLine.setHyphenEndWord(subContent); // The real complete word 
			
				if (this.currentAltoHyphenation != null) {
					this.currentAltoHyphenation.setEnd(content);
					
					// End is found, so we can write the hyphen and unset the AltoHyphenation object
					this.currentArticleTextForLines.append( this.currentAltoHyphenation.toHtml() );
					this.pageTextForLines.append( this.currentAltoHyphenation.toHtml() );
					
					this.currentAltoHyphenation = null;
				}
				
				// [C1] Clear this word for line (because already there for )
				content = null;
			}
		}
		
		// Test if the last tag is the same as the current tag.
		
		if ( AltoConstant.TAG_STRING.equalsIgnoreCase(lastElement.qName) ) {
			// Previous was String, add a space
			//currentArticleText.append(" ");
		}
		
		// Add text to alto line for text view
		currentAltoLine.getBuffer().append(contentForLine);
		
		// [C1] Use the content in the current article text 
		if (StringUtils.isNotBlank(content)){
			// Add text to current article
			currentArticleText.append(content);
			
			// Only add if not hyphenation is in progress
			if (this.currentAltoHyphenation == null) {
				this.currentArticleTextForLines.append(content);
				this.pageTextForLines.append(content);
			}
		}
		
		AltoWord wordCoord = null;
		if (useTokenizer) {
			// Tokenize and index for each token
			List<String> texts = lu.parse(contentForLine);
			for (String text : texts) {
				wordCoord = addToIndex(text, x, y, w, h, null);
				consecutiveTags.add(wordCoord); //not sure !
			}
		} else {
			wordCoord = addToIndex(contentForLine, x, y, w, h, id);
			consecutiveTags.add(wordCoord);
		}
		
		boolean isConsecutive = (AltoConstant.TAG_STRING.equalsIgnoreCase(previousSiblingTag));
		
		if (isConsecutive && wordCoord != null && consecutiveTags != null && !consecutiveTags.isEmpty()){
			String firstText = consecutiveTags.get(0).getText();
			String text = firstText + wordCoord.getText();
			// TODO: WHY IS THIS NEEDED?
			// Update all previous sibling String with the same text
			for (AltoWord wc : consecutiveTags) {
				// Copy original text
				if (wc.getContent() == null) {
					wc.setContent(wc.getText());
				}
				wc.setText(text);
			}
		}
		
		/*if (DEBUG_isInDummyArticle) { // Debug words landing in the Dummy Article
			logger.warn("DEBUG DUMMY WORD: " + content);
		}*/
	}
	
	private void handleHyphen(String uri, String localName, String qName, Attributes attributes) {
		
		String content = attributes.getValue( AltoConstant.ATTR_STRING_CONTENT ); // MAIN TEXT IS HERE, no need character method on handler
		
		
		if (this.currentAltoLine !=null) {
			this.currentAltoLine.getBuffer().append(content);
		}
		
		if (this.currentAltoHyphenation != null) {
			this.currentAltoHyphenation.setStart( this.currentAltoHyphenation.getStart() + content );
		}
		
		if (this.pageTextForLines != null) {
			this.pageTextForLines.append(content);
		}
		
	}
	
	/** Set the class variables for the new current article text and lines.
	 *  First, load the current text and text lines StringBuilders or create new empty StringBuilders.
	 * 
	 * @param article
	 * @param key
	 */
	private void prepareForNewArticle(DivSection article, String key) {
		
		this.currentArticleText 		= article.getBlocks().get(key);
		this.currentArticleTextForLines = article.getBlocksForLines().get(key);
		
		if (this.currentArticleText == null) {
			this.currentArticleText 		= new StringBuilder();
			this.currentArticleTextForLines = new StringBuilder();
			//Warning : won't be stored... because orphan text
			//article.setText(currentArticleText);
		}
		
		this.elementStack.clear();
	}
	
	// Index Methods
	//================================================================================

	
	private AltoWord addToIndex(String text, Integer x, Integer y, Integer w, Integer h, String id) {
		AltoWord wordCoord = null;

		// If word is cleared, do not index it 
		if (text != null && text.length() > 0) {
			// Add index for word.
			
			String key = id;
			
			if (key == null) { // Nearly never used.
				// For tokenizer it is null. RM: What?
				key = String.format("%s_%s-%s-%s-%s", text, String.valueOf(x), String.valueOf(y), String.valueOf(w), String.valueOf(h));
			}
			
			wordCoord = new AltoWord();
			wordCoord.setId(key);
			wordCoord.setBlockId(currentTextBlockId); // required block id for word
			wordCoord.setText(text);
			wordCoord.addCoord(x, y, w, h);
			wordCoord.setPage(this.fileid);
			
			DivSection currentArticle = null;
			if ( !stackArticle.isEmpty() ) { 
				currentArticle = stackArticle.peek();
			}
			
			if (currentArticle != null) {
				//wordCoord.setBegin(currentArticle.getId()); 		// begin is not an attribute at the moment
				//wordCoord.setArticle(currentArticle.getBegin()); 	// EMPTY ...
				//wordCoord.setArticle(currentArticle.getDmdid());	//MODSMD_ARTICLE4
				wordCoord.setArticle(currentArticle.getId()); 		// id:DTL56, dmdid:MODSMD_ARTICLE4, begin:Empty
				
				currentArticle.getWords().add(wordCoord);
			}
			
			// Add all words, even outside of exported articles on the page.
			this.pageWords.add(wordCoord);
		}
		
		return wordCoord;
	}

	private Integer integer(String value) {
		Integer i = null;
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			//
		}
		return i;
	}
	
	//================================================================================
	// Getters and Setters
	//================================================================================

	
	public List<AltoWord> getPageWords() {
		return pageWords;
	}
	
	public String getPageFullTextLines() {
		return this.pageTextForLines.toString();
	}

}
