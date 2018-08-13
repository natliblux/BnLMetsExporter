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
package lu.bnl.domain.managers.remote;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import lu.bnl.AppGlobal;
import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.configuration.RemoteIdentifierConfig;
import lu.bnl.domain.model.DivSection;

/**
 * This class is an example implementation of the RemoteIdentifierManager by the BnL.
 * The BnL is currently migrating toward ARK identifiers. 
 * This class allows to get the new ARK identifier given a PID.
 * In addition, the qualifier part of the ARK is added with the use of the article (DivSection).
 */
public class BnLRemoteIdentifierManager extends RemoteIdentifierManager {

	private static final Logger logger = LoggerFactory.getLogger(BnLRemoteIdentifierManager.class);
	
	@Override
	public String getIdByPID(String pid) {
		return null;
	}
	
	@Override
	public String getIdByPID(String pid, DivSection article) {
		
		// 1) Get ARK
		String ark = this.getArkForPid(pid);
		
		// To not continue if ARK is null.
		if (ark == null) {
			return null;
		} else {
			ark = this.getPrefixFromConfig() + ark;
		}
		
		// 2) Add Qualifier
		if (article.getType().equalsIgnoreCase("ISSUE") || article.getType().equalsIgnoreCase("VOLUME")) {
			// Issue/Volume level
			// Do nothing
		} else {
			// Any Article level
			ark += String.format("/articles/%s", article.getId());
		}
		
		return ark;
	}
	
	private String getArkForPid(String pid) {
		
		RemoteIdentifierConfig ric = AppConfigurationManager.getInstance().getExportConfig().remoteIdentifier;
		if (ric == null || ric.enable == false) {
			return null;
		}
		
		// Should be something like: http://my-server.lu/path/to/pid/<PID> and returns the ARK
		String remoteIdentifierUrl = ric.url;
		remoteIdentifierUrl += pid;
		
		String result = null;
		try {
			URIBuilder builder = new URIBuilder(remoteIdentifierUrl);
			
			URL url = builder.build().toURL();
			
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");

			result = IOUtils.toString( httpConnection.getInputStream(), AppGlobal.ENCODING );
			
			String message = String.format("getArkForPid: HTTP Request %s for pid %s: %s %s", 
					url.toString() , pid, httpConnection.getResponseCode(), httpConnection.getResponseMessage());
			logger.info(message);
			
			logger.debug("GetMetsFromUrl: Size of XML: " + result.length());
			
		} catch (MalformedURLException e) {
			logger.error("getArkForPid: An error has occured while requesting the METS.", e);
		} catch (IOException e) {
			logger.error("getArkForPid: An error has occured while requesting the METS.", e);
		} catch (URISyntaxException e) {
			logger.error("getArkForPid: An error has occured while requesting the METS.", e);
		}
		
		return this.extractArkFromResponse(result);
	}
	
	/** Converts the String response to a Java Object using the GSON library in order
	 *  to extract the ARK value.
	 * 
	 * @param response
	 * @return
	 */
	private String extractArkFromResponse(String response) {
		if (response != null) {
			try {
				ResponseMessage responseMessage = new Gson().fromJson(response, ResponseMessage.class);
				return responseMessage.data.ark;
			} catch (Exception e) {
				logger.error("Error during extraction of ARK from Response. Response:" + response);
			}
		}
		
		return response;
	}
	
	private String getPrefixFromConfig() {
		RemoteIdentifierConfig ric = AppConfigurationManager.getInstance().getExportConfig().remoteIdentifier;
		if (ric == null || ric.enable == false) {
			return "";
		}
		
		if (ric.prefix != null) {
			return ric.prefix;
		} else {
			return "";
		}
	}
	
	// Custom class to convert JSON to objects
	
	private class ResponseMessage {
		
		public String timestamp;
		
		public String message;
		
		public String status;
		
		public MetsData data;

	}
	
	private class MetsData {
		
		public String ark;
		
		public String pid;

		public String createdOn;
		
		public String lastUpdatedOn;
		
		public int numberOfFileMaps;
		
		public boolean containsMetsContent;

	}
	
	
}
