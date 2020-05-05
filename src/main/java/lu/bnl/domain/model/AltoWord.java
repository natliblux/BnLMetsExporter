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
package lu.bnl.domain.model;

public class AltoWord {

	private Integer x;
	private Integer y;
	private Integer w;
	private Integer h;
	private String id;
	private String article;
	private String text;
	private String content;
	private String blockId;
	private String page;
	
	public void addCoord(Integer x, Integer y, Integer w, Integer h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	public Integer getW() {
		return w;
	}

	public void setW(Integer w) {
		this.w = w;
	}

	public Integer getH() {
		return h;
	}

	public void setH(Integer h) {
		this.h = h;
	}

	public void setArticle(String article) {
		this.article = article;
	}
	
	public String getArticle() {
		return article;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	
	public String getBlockId() {
		return blockId;
	}
	
	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}
	
	// Custom Methods

	/**
	 * Convert the Alto Word into an HTML representation.
	 * Example: <w a="Article01" b="Block01" x="10" y="12" w="20" h="8">theword<\/w>
	 * 
	 * @return
	 */
	public String toHTML(Boolean includePage) {
		// TODO: REFACTOR THIS TO BE MORE BEAUTIFUL
		String startTag = String.format("<w a=\"%s\" b=\"%s\" p=\"%s\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\">",
				this.getArticle(),
				this.getBlockId(),
				this.getPage(),
				this.getX(),
				this.getY(),
				this.getW(),
				this.getH());
		if (!includePage) {
			startTag = String.format("<w a=\"%s\" b=\"%s\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\">",
					this.getArticle(),
					this.getBlockId(),
					this.getX(),
					this.getY(),
					this.getW(),
					this.getH());
		}
		String endTag = "</w> "; // IMPORTANT TO ADD SPACE
		
		String word = String.format("%s%s%s", startTag, this.getText(), endTag);
		
		return word;
	}
	
}
