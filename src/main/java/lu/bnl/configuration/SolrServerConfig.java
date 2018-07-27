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
package lu.bnl.configuration;

public class SolrServerConfig {

	/** The ZK_HOST url of all zookeepers. This will be used in Cloud mode.
	 */
	public String zkHost;
	
	/** The URL of the articles core. This will be used in Solr Standalone mode.
	 */
	public String articleURL;
	
	/** The name of the collection when using SolrCloud.
	 */
	public String articleCollection;	
	
	/** The URL of the pages core. This will be used in Solr Standalone mode.
	 */
	public String pageURL;
	
	/** The name of the collection when using SolrCloud.
	 */
	public String pageCollection;
	
}
