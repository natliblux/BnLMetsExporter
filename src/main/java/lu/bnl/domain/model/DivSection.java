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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

public class DivSection {

	private String id;
	private String dmdid;
	private String label;
	private String type;
	private String begin = null;
	private String fileid = null;//ALTOid
	private SolrInputDocument doc;
	
	private Map<String, StringBuilder> blocks;
	
	private Map<String, StringBuilder> blocksForLines;
	
	private List<AltoWord> words;

	public DivSection(String id, String dmdid, String label, String type) {
		this.id = id;
		this.dmdid = dmdid;
		this.label = label;
		this.type = type;
		
		blocks = new LinkedHashMap<>();
		blocksForLines = new LinkedHashMap<>();
		words = new ArrayList<>();
	}

	/** Returns all blocks as one standard string. Blocks are concatenated by space.
	 * 
	 * @return Full Text with no formatting.
	 */
	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (StringBuilder blockText : blocks.values()) {
			
			if ( sb.length() > 0 && blockText.length() > 0 ) {
				sb.append(" ");
			}
			
			sb.append(blockText);
		}
		return sb.toString();
	}
	
	/** Returns all blocks as one string with HTML formatting for lines.
	 * 
	 * @return Full Text with some HTML formatting.
	 */
	public String getTextInLineFormat() {
		StringBuilder sb = new StringBuilder();
		for (StringBuilder blockText : blocksForLines.values()) {
			
			/* Comment out because not really needed when using <block>
			if ( sb.length() > 0 && blockText.length() > 0 ) {
				sb.append("<br/>");
			}*/
			
			sb.append(blockText);
		}
		return sb.toString();
	}
	
	/**
	 * Returns the HTMLized full text containing all words with their
	 * coordinates. Essentially, concatenate all Alto Words, with 
	 * their coordinates and metadata, into a string. Each Alto Word 
	 * is stored as an HTML tag such as: <w [attributes]>ALTOWORD<\/w>
	 * Where attributes are HTML attributes such as article id, block id
	 * and x, y, w, h coordinates. 
	 * 
	 * @return HTML with coordinate Metadata for each word.
	 */
	public String getHtmlizedWords(Boolean includePage) {
		StringBuilder sb = new StringBuilder();
		
		for (AltoWord altoWord : this.words) {
			sb.append( altoWord.toHTML(includePage) );
		}
		
		return sb.toString();
	}
	
	public SolrInputDocument getDoc() {
		return doc;
	}

	public void setDoc(SolrInputDocument doc) {
		this.doc = doc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDmdid() {
		return dmdid;
	}

	public void setDmdid(String dmdid) {
		this.dmdid = dmdid;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBegin() {
		return begin;
	}

	public void setBegin(String begin) {
		this.begin = begin;
	}

	public String getFileid() {
		return fileid;
	}

	public void setFileid(String fileid) {
		this.fileid = fileid;
	}

	public Map<String, StringBuilder> getBlocks() {
		return blocks;
	}
	
	public Map<String, StringBuilder> getBlocksForLines() {
		return blocksForLines;
	}
	
	public List<AltoWord> getWords() {
		return this.words;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		DivSection other = (DivSection) obj;
		
		if (dmdid == null) {
			if (other.dmdid != null)
				return false;
		} else if (!dmdid.equals(other.dmdid))
			return false;
		
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		
		return true;
	}


	
	
	
	
}
