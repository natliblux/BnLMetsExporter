/*******************************************************************************
 * Copyright (C) 2017-2019 Bibliothèque nationale de Luxembourg (BnL)
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

import java.util.List;

public class ArticleDocumentBuilder {
	private String ark;
	
	private String id;
	private String documentID;
	private String dmdId;
	private String recordIdentifier;
	private String date;
	
	private List<String> isPartOfs;
	
	private String publisher;
	private String text;
	private String lineText;
	private String wordText; // large string containing all "HTMLized ALTO" <w >word</w>
	private String title;
	private String alternative;
	
	private List<String> creators;
	
	private List<String> languages;
	
	private String type;
	
	private boolean isArticle;
	private String panel;
	
	//================================================================================
	//================================================================================
	
	public ArticleDocumentBuilder(String id, String documentID) {
		this.id = id;
		this.documentID = documentID;
	}
	
	public ArticleDocumentBuilder ark(String ark) {
		this.ark = ark;
		return this;
	}
	
	public ArticleDocumentBuilder dmdId(String dmdId) {
		this.dmdId = dmdId;
		return this;
	}
	
	public ArticleDocumentBuilder recordIdentifier(String recordIdentifier) {
		this.recordIdentifier = recordIdentifier;
		return this;
	}
	
	public ArticleDocumentBuilder date(String date) {
		this.date = date;
		return this;
	}
	
	public ArticleDocumentBuilder isPartOfs(List<String> isPartOfs) {
		this.isPartOfs = isPartOfs;
		return this;
	}
	
	public ArticleDocumentBuilder publisher(String publisher) {
		this.publisher = publisher;
		return this;
	}
	
	public ArticleDocumentBuilder text(String text) {
		this.text = text;
		return this;
	}
	
	public ArticleDocumentBuilder lineText(String lineText) {
		this.lineText = lineText;
		return this;
	}
	
	public ArticleDocumentBuilder wordText(String wordText) {
		this.wordText = wordText;
		return this;
	}
	
	public ArticleDocumentBuilder title(String title) {
		this.title = title;
		return this;
	}
	
	public ArticleDocumentBuilder alternative(String alternative) {
		this.alternative = alternative;
		return this;
	}

	public ArticleDocumentBuilder creators(List<String> creators) {
		this.creators = creators;
		return this;
	}
	
	public ArticleDocumentBuilder languages(List<String> languages) {
		this.languages = languages;
		return this;
	}
	
	public ArticleDocumentBuilder type(String type) {
		this.type = type;
		return this;
	}
	
	public ArticleDocumentBuilder isArticle(boolean isArticle) {
		this.isArticle = isArticle;
		return this;
	}
	
	public ArticleDocumentBuilder panel(String panel) {
		this.panel = panel;
		return this;
	}
	
	public DublinCoreDocument build() {
		return new DublinCoreDocument(this);
	}

	//================================================================================
	//================================================================================
		
	public String getArk() {
		return ark;
	}
	
	public String getId() {
		return id;
	}

	public String getDocumentID() {
		return documentID;
	}

	public String getDmdId() {
		return dmdId;
	}

	public String getRecordIdentifier() {
		return recordIdentifier;
	}

	public String getDate() {
		return date;
	}

	public List<String> getIsPartOfs() {
		return isPartOfs;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getText() {
		return text;
	}
	
	public String getLineText() {
		return lineText;
	}
	
	public String getWordText() {
		return wordText;
	}

	public String getTitle() {
		return title;
	}

	public String getAlternative() {
		return alternative;
	}

	public List<String> getCreators() {
		return creators;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public String getType() {
		return type;
	}

	public boolean isArticle() {
		return isArticle;
	}

	public String getPanel() {
		return panel;
	}
	
	// Special and Computed
	
	public int getWordCount() {
		if (this.text == null) {
			return 0;
		}
		
		String[] words = this.text.split("\\s+"); // Split by >=1 spaces
		return words.length;
	}
	
}
