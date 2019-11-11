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
package lu.bnl.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Class XmlUtil.
 * 
 * Utilities for XML.
 */
public class XmlUtil {

	/** The Constant LOG. */
	private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

	/**
	 * Trim xml.
	 * 
	 * @param input
	 *            the input
	 * @return the string
	 */
	private static String trimXml(String input) {
		if (input==null){
			return null;
		}
		return input.trim().replaceAll("\n", "").replaceAll("\r", "");
	}

	/**
	 * Sax Parsing helper.
	 * 
	 * @param input
	 *            the input
	 * @param handler
	 *            the handler
	 */
	public static void parseSax(final String input, final DefaultHandler handler) {
		try {
			parseSax(input, handler, "UTF-8");
		} catch (final Exception e) {
			logger.error("Sax UTF-8 parsing failed in "+StringUtils.left(input,500)+" with " + handler.getClass().toString(), e);
		}
	}
	/**
	 * Parses the sax.
	 * 
	 * @param input
	 *            the input
	 * @param handler
	 *            the handler
	 * @param encoding
	 *            the encoding
	 * @throws SAXException
	 *             the SAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	public static void parseSax(final String input, final DefaultHandler handler, final String encoding) throws SAXException, IOException, ParserConfigurationException {
		String xmlText = trimXml(input);
		if (StringUtils.isBlank(xmlText)) {
			logger.debug("parseSax : xml is empty");
			return;
		}
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false); // No validation required
		factory.setNamespaceAware(false);
		final SAXParser parser = factory.newSAXParser();
		final ByteArrayInputStream bis = new ByteArrayInputStream(xmlText.getBytes(encoding));
		final InputSource is = new InputSource(bis);
		parser.parse(is, handler);
	}
	

}
