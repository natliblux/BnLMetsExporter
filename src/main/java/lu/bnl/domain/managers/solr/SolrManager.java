/*******************************************************************************
 * Copyright (C) 2017-2018 Bibliotèque nationale de Luxembourg (BnL)
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
package lu.bnl.domain.managers.solr;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.domain.managers.export.SolrExportManager;

public class SolrManager {

	private static final Logger logger = LoggerFactory.getLogger(SolrExportManager.class);
	
	private String hostString;
	
	private String collection;
	
	private boolean useCloud;
	
	private SolrClient solr;
	
	
	private int documentsToCommit;
	
	
	// collection can be ignore if useCloud is False
	public SolrManager(String hostString, String collection, boolean useCloud) {
		this.hostString = hostString;
		this.collection = collection;
		this.useCloud = useCloud;
		
		this.documentsToCommit = 0;
	}

	//================================================================================
	
	private void initSolrClient() {
		if (this.useCloud) {
			logger.info(prepareLog("Initalizing Solr Client. Mode: Cloud"));
			CloudSolrClient cloudSolr = new CloudSolrClient.Builder()
					.withZkHost(this.hostString)
					.build();
			cloudSolr.setDefaultCollection(collection);
			cloudSolr.connect();
			
			this.solr = cloudSolr;
		} else {
			logger.info(prepareLog("Initalizing Solr Client. Mode: Single Host"));
			
			int cores = Runtime.getRuntime().availableProcessors();
			
			this.solr = new ConcurrentUpdateSolrClient.Builder(this.hostString)
					.withQueueSize(2500)
					.withThreadCount(cores - 1)
					.build();
		}
	}
	
	// TODO: Improve by returning something
	private void checkResponse(UpdateResponse response) {
		if ( response.getStatus() != 0 ) {
			System.out.println(prepareLog(String.format("Status of Update Response is not 0. Value: %d", response.getStatus())));
		}
	}
	
	//================================================================================
	
	public void connect() {
		this.initSolrClient();
	}
	
	public boolean add(List<SolrInputDocument> documents) {
		boolean success = false;
		
		if ( ! documents.isEmpty() ) {

			try {
				UpdateResponse response = this.solr.add(documents);//, 15000); // Commit withins 15sec
				checkResponse(response);
				
				this.documentsToCommit += documents.size();
				logger.info(prepareLog(String.format("%d documents added.", documents.size())));
				
				success = true;
			} catch (SolrServerException e) {
				logger.error(prepareLog("Failed to add Documents."), e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(prepareLog("Failed to add Documents."), e);
				e.printStackTrace();
			}
			
		}
		
		return success;
	}
	
	/**
	 * Analyses a value for a field using the Solr FieldAnalysis RequestHandler.
	 * 
	 * @param field	The field to analyse
	 * @param value The string to run through the field analyser
	 * @return The Solr FieldAnalysisResponse
	 */
	public FieldAnalysisResponse analyzeField(String field, String value) {
		FieldAnalysisRequest request = new FieldAnalysisRequest("/analysis/field");
		request = request.addFieldName(field);
		request = request.setFieldValue(value);
		
		FieldAnalysisResponse response = null;
		
		try {
			response = request.process(this.solr);
		} catch (SolrServerException e) {
			logger.error(prepareLog("Failed to analyse field."), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(prepareLog("Failed to analyse field."), e);
			e.printStackTrace();
		}
		
		return response;
	}
	
	/** Clean the entire core or collection by removing every document.
	 *  This is literally a "*:*" delete query.
	 * 
	 * @return True if the clean and commit are successful, False otherwise.
	 */
	public boolean clean() {
		boolean success = false;
		
		try {
			UpdateResponse response = this.solr.deleteByQuery("*:*");
			checkResponse(response);
			
			success = this.commit();
			
		} catch (SolrServerException e) {
			logger.error(prepareLog("Failed to clean."), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(prepareLog("Failed to clean."), e);
			e.printStackTrace();
		}
		
		return success;
	}
	
	/** Commits documents to the Index. Checks if any documents has been added before
	 *  by the add(...) method.
	 * 
	 * @return True if commit is successful, False otherwise.
	 */
	public boolean commit() {
		boolean success = false;
		
		try {
			UpdateResponse response = this.solr.commit(true, false); // change first parameter to false to not block until committed to disk.
			logger.info(prepareLog("Commit done in: " + response.getElapsedTime()));
			
			if (this.documentsToCommit > 0) {
				logger.info(prepareLog(String.format("%d documents commited.", this.documentsToCommit)));
				this.documentsToCommit = 0;
			} else {
				logger.warn(prepareLog("No new documents has been committed."));
			}
			
			success = true;
		} catch (SolrServerException e) {
			logger.error(prepareLog("Failed to commit Documents."), e); // Side note: core is single, collection is cloud.
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(prepareLog("Failed to commit Documents."), e);
			e.printStackTrace();
		}
		
		return success;
	}
	
	/** Optimizes the Index for the core or collection.
	 * 
	 * @return True if successfully optimized, False otherwise.
	 */
	public boolean optimize() {
		boolean success = false;
		try {
			UpdateResponse response = this.solr.optimize(true, false);
			logger.info(prepareLog("Optimize done in: " + response.getElapsedTime()));
			
			success = true;
		} catch (SolrServerException e) {
			logger.error(prepareLog("Failed to optimize."), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(prepareLog("Failed to optimize."), e);
			e.printStackTrace();
		}
		
		return success;
	}
	
	/** Closes the SolrClient stream.
	 * 
	 * @return True if successfully closed, False otherwise.
	 */
	public boolean close() {
		boolean success = false;
		try {
			this.solr.close();
			success = true;
		} catch (IOException e) {
			logger.error(prepareLog("Failed to close stream/connection."), e);
			e.printStackTrace();
		}
		
		return success;
	}
	
	//================================================================================

	private String prepareLog(String message) {
		return String.format("[%s] %s", this.collection, message);
	}
}
