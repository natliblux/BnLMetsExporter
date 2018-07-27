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
package lu.bnl.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

public class DocList {

	// Solr
	public List<SolrInputDocument> articles = new ArrayList<>();
	public Map<String, List<SolrInputDocument>> words = new HashMap<>();

	public List<SolrInputDocument> altoPages = new ArrayList<>();
	
	
	// Primo export
	public List<PrimoDocument> primoarticles = new ArrayList<>();
	
	// STATS
	
	public int pages = 0;

}

