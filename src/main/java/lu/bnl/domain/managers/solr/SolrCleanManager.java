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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.configuration.SolrServerConfig;

/**
 * This class sole purpose is to clean Solr Indices.
 * 
 * @author Ralph Marschall
 *
 */
public class SolrCleanManager {

	private static final Logger logger = LoggerFactory.getLogger(SolrCleanManager.class);
	
	private boolean useCloud;
	
	private SolrManager articleSolrManager;
	
	private SolrManager pageSolrManager;
	
	public SolrCleanManager(boolean useCloud) {
		this.useCloud = useCloud;
		
		this.createSolrManagers();
	}

	private void createSolrManagers() {
		
		// TODO: check if solr config is okay in BnLMetsExporter
		SolrServerConfig solrConfig = AppConfigurationManager.getInstance().getExportConfig().solr;
		
		if (!useCloud) {
			this.articleSolrManager = new SolrManager(solrConfig.articleURL, solrConfig.articleCollection, useCloud);
			this.pageSolrManager 	= new SolrManager(solrConfig.pageURL, solrConfig.pageCollection, useCloud);
		} else {
			this.articleSolrManager = new SolrManager(solrConfig.zkHost, solrConfig.articleCollection, useCloud);
			this.pageSolrManager 	= new SolrManager(solrConfig.zkHost, solrConfig.pageCollection, useCloud);
		}
	}
	
	public void run() {
	
		console("Starting Solr Clean.");
		
		this.connectAll();
			
		console("Cleaning all Collections/Cores...");
		this.cleanAll();
		
		this.closeAll();
		
		console("Done.");
	}
	
	//================================================================================
	
	private void console(String msg) {
		logger.info(msg);
		System.out.println(msg);
	}
	
	private void connectAll() {
		this.articleSolrManager.connect();
		this.pageSolrManager.connect();
	}
	
	private boolean cleanAll() {
		boolean articleOk = this.articleSolrManager.clean();
		boolean pageOk = this.pageSolrManager.clean();
		
		if (articleOk) {
			console("[OK] Article Collection/Core Cleaned!");
		} else {
			console("[ERROR] Article Collection/Core failed to be cleaned");
		}
		
		if (pageOk) {
			console("[OK] Page Collection/Core Cleaned!");
		} else {
			console("[ERROR] Page Collection/Core failed to be cleaned");
		}
		
		return articleOk && pageOk;
	}
	
	private boolean closeAll() {
		boolean articleOk = this.articleSolrManager.close();
		boolean pageOk = this.pageSolrManager.close();
		
		return articleOk && pageOk;
	}
	
}
