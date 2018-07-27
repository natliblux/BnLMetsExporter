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

public class AltoLine {

	private String id;
	
	private String article; // MODSMD_ARTICLE4
	
	private StringBuffer buffer = new StringBuffer();
	
	private String hyphenEnd = null;
	
	private String hyphenStart = null;
	
	private String hyphenStartWord = null;
	
	private String hyphenEndWord = null;
	
	private String blockId;
	
	public AltoLine(String id, String article) {
		this.id = id;
		this.article = article;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getArticle() {
		return article;
	}
	
	public void setArticle(String article) {
		this.article = article;
	}
	
	public StringBuffer getBuffer() {
		return buffer;
	}
	
	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

	public void setHyphenEnd(String hyphenEnd) {
		this.hyphenEnd = hyphenEnd;
	}
	
	public String getHyphenEnd() {
		return hyphenEnd;
	}

	public String getHyphenStart() {
		return hyphenStart;
	}

	public void setHyphenStart(String hyphenStart) {
		this.hyphenStart = hyphenStart;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	public String getBlockId() {
		return blockId;
	}

	public String getHyphenStartWord() {
		return hyphenStartWord;
	}

	public void setHyphenStartWord(String hyphenStartWord) {
		this.hyphenStartWord = hyphenStartWord;
	}

	public String getHyphenEndWord() {
		return hyphenEndWord;
	}

	public void setHyphenEndWord(String hyphenEndWord) {
		this.hyphenEndWord = hyphenEndWord;
	}



	
}
