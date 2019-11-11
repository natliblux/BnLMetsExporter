/*******************************************************************************
 * Copyright (C) 2018 Bibliothèque nationale de Luxembourg (BnL)
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
package lu.bnl.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.AppGlobal;
import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.domain.model.Alto;
import lu.bnl.domain.model.DivSection;
import lu.bnl.xml.AltoXMLParserHandler;
import lu.bnl.xml.MetsXMLParserHandler;
import lu.bnl.xml.XmlUtil;

public class MetsAltoReaderManager {

	private static final Logger logger = LoggerFactory.getLogger(MetsAltoReaderManager.class);
	
	public static MetsXMLParserHandler parseMets(String documentID, String content, String dir, boolean useTokenizer, List<String> supportedMetsTypes) {
		MetsXMLParserHandler handler = new MetsXMLParserHandler(dir, supportedMetsTypes);
		
		try {
			XmlUtil.parseSax(content, handler, "UTF-8");
		} catch (Exception e) {
			logger.error(String.format("Error while parsing Mets %s file.", documentID), e);
		}
		
		return handler;
	}
	
	public static AltoXMLParserHandler parseAlto(Alto page, Map<String, List<String>> articleAltoBlockMap, Map<String, DivSection> articles, boolean useTokenizer) {
		AltoXMLParserHandler handler = null;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			File file = new File(page.getPath());
			if (file == null || !file.exists()) {
				throw new Exception("File not found " + ((file != null) ? file.getPath() : "null"));
			}

			InputStream xmlInput = new FileInputStream(file);
			SAXParser saxParser = factory.newSAXParser();

			handler = new AltoXMLParserHandler(page.getFileid(), articleAltoBlockMap, articles, useTokenizer);
			saxParser.parse(xmlInput, handler);

		} catch (Exception e) {
			logger.error(String.format("Error while parsing Alto %s file.", page.getFileid()), e);
		}
		
		return handler;
	}
	
	public static String downloadMetsContent(String documentID) {
		
		String urlGetMets = AppConfigurationManager.getInstance().getExportConfig().metsGetter.url;
		
		String result = null;
		try {
			URIBuilder builder = new URIBuilder(urlGetMets).addParameter("pid", documentID);
			
			URL url = builder.build().toURL();
			
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");

			result = IOUtils.toString( httpConnection.getInputStream(), AppGlobal.ENCODING );
			
			String message = String.format("getMetsContent: HTTP Request %s for document ID %s: %s %s", 
					url.toString() ,documentID, httpConnection.getResponseCode(), httpConnection.getResponseMessage());
			logger.info(message);
			
			logger.debug("GetMetsFromUrl: Size of XML: " + result.length());
			
		} catch (MalformedURLException e) {
			logger.error("GetMetsFromUrl: An error has occured while requesting the METS.", e);
		} catch (IOException e) {
			logger.error("GetMetsFromUrl: An error has occured while requesting the METS.", e);
		} catch (URISyntaxException e) {
			logger.error("GetMetsFromUrl: An error has occured while requesting the METS.", e);
		}
		
		return result;
	}
	
}
