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

import org.xml.sax.Attributes;

/**
 * This class is a simple Java object to store a parsed XML Tag
 * 
 * @author Ralph Marschall
 *
 */
public class XmlParserStackElement {

	// This is given by the startElement() method of the SaxHandler
	public String uri;

	// This is given by the startElement() method of the SaxHandler
	public String localName;
	
	// This is given by the startElement() method of the SaxHandler
	public String qName;
	
	// This is given by the startElement() method of the SaxHandler
	public Attributes attributes;
	
	// Can hold any data. Idea is to store for example, the DivSection object.
	public Object data = null;
	
	public XmlParserStackElement(String uri, String localName, String qName, Attributes attributes) {
		this.uri = uri;
		this.localName = localName;
		this.qName = qName;
		this.attributes = attributes;
	}
	
}
