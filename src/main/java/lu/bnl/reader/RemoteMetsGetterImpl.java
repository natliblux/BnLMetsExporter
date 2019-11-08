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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.AppGlobal;
import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.files.FileUtil;

public class RemoteMetsGetterImpl extends MetsGetter {

	private static final Logger logger = LoggerFactory.getLogger(RemoteMetsGetterImpl.class);
	
	
	@Override
	public String validateInput(String dir, String items) {
		Boolean hasDir		= dir != null;
		Boolean hasItems	= items != null;
		Boolean isDirOk		= FileUtil.checkDir(dir) != null;
		Boolean isItemsOk	= FileUtil.checkFile(items) != null;

		if (!isDirOk) {
			logger.info("Abording! Reason: You must provide a valid directory.");
		}

		if (!isItemsOk) {
			logger.info("Abording! Reason: You must provide a valid items file.");
		}

		if (hasDir && hasItems && isDirOk && isItemsOk) {
			return items;
		}

		return null;
	}

	@Override
	public void findAllMets(String path) {
		List<String> pids = null;
		
		try {
			pids = FileUtils.readLines(new File(path), Charset.forName("UTF-8"));
		} catch (IOException e) {
			logger.error("Failed to read PIDS file at " + path, e);
			e.printStackTrace();
		}
		
		this.setMetsData(pids);
	}
	
	/** The data must be a PID. for the config getMetsURL + ?pid=data.
	 * Example: data = 1234 and getMetsURL = www.myserver.com/getMets
	 * Full URL will be www.myserver.com/getMets?pid=1234
	 */
	@Override
	public String getMetsContent(String data) {

		String urlGetMets = AppConfigurationManager.getInstance().getExportConfig().metsGetter.url;
		
		String result = null;
		try {
			URIBuilder builder = new URIBuilder(urlGetMets).addParameter("pid", data);
			
			URL url = builder.build().toURL();
			
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");

			result = IOUtils.toString( httpConnection.getInputStream(), AppGlobal.ENCODING );
			
			String message = String.format("getMetsContent: HTTP Request %s for pid %s: %s %s", 
					url.toString() ,data, httpConnection.getResponseCode(), httpConnection.getResponseMessage());
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
	
	/** The data is a PID, the PID is unique.
	 */
	@Override
	public String getUniqueName(String data) {
		return data;
	}
	
	/** For remote, there is no location/path for the METS file.
	 */
	@Override
	public String getMetsLocation(String data) {
		return null;
	}

}
