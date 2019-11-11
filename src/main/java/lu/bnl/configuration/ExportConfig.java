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
package lu.bnl.configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lu.bnl.domain.constants.MetsTypeHandler;

public class ExportConfig {
	
	// Public fields that map the YAML file
	//================================================================================
	
	/** Configures the behavior for each mets type (newspaper, serial, ...) and
	 *  for each div tag type.
	 */
	public Map<String, List<LinkedHashMap<String, String>>> metsType;
	
	/** Defines the URL and class to request METS files.
	 */
	public MetsGetterConfig metsGetter;
	
	/** Defines the ARK configuration.
	 */
	public ArkConfig ark;

	/** Defines the URL to request the Identifier based on another id. 
	 *  (Used by the BnL to convert PIDs to ARKs identifiers during a transition phase)
	 */
	@Deprecated
	public RemoteIdentifierConfig remoteIdentifier;
	
	/** Defines the URL to link back to the Viewer (for Primo Export).
	 */
	public String viewerHostBaseURL;
	
	/** Lists all mets type that will be exported as Dublin Core.
	 *  All Dublin Core documents are stored in a tar.gz.
	 */
	public List<String> exportDublinCore;
	
	/** Lists all mets type that will be exported into Solr.
	 */
	public List<String> exportSolr;
	
	
	public PrimoConfig primo;
	
	public SolrServerConfig solr;
	
	// Private fields for runtime access
	//================================================================================
	
	private Map<String, List<MetsTypeConfig>> metsTypeMap;
	
	private Map<String, MetsTypeHandler> metsTypeHandlers;
	
	//================================================================================
	
	/** Convert the LinkedHashMap<String, String> to List<MetsTypeConfig> using an ObjectMapper.
	 *  This is required because, SnakeYAML is not able to convert it directly by himself.
	 */
	public void convertConfig() {
		this.metsTypeMap = new HashMap<>();
		
		this.metsTypeHandlers = new HashMap<>();
		
		ObjectMapper mapper = new ObjectMapper();
		
		for (String metsTypeKey : this.metsType.keySet()) {
			
			List<MetsTypeConfig> mappedList = mapper.convertValue(this.metsType.get(metsTypeKey), new TypeReference<List<MetsTypeConfig>>() { });
			
			this.metsTypeMap.put(metsTypeKey, mappedList);
			
			MetsTypeHandler mth = setupMetsTypeHandlers(metsTypeKey, mappedList);
			this.metsTypeHandlers.put(metsTypeKey, mth);
		}
	}
	
	/**
	 * Gets the set of supported Mets Types from the current loaded configuration.
	 * E.g. (newspaper, serial, manuscript, monograph, poster)
	 * 
	 * @return the set of strings representing each type as defined under 
	 *         the metsType key of the config file
	 */
	public Set<String> getMetsTypeSet() {
		return this.metsTypeHandlers.keySet();
	}
	
	/**
	 * Gets the MetsTypeHandler for a given key.
	 * The key is the typeName e.g. newspaper, serial, ...
	 * 
	 * @param key
	 * @return The MetsTypeHandler of the key or null.
	 */
	public MetsTypeHandler getMetsTypeHandlerByKey(String key) {
		return this.metsTypeHandlers.get(key);
	}
	
	/**
	 * Converts a list of MetsTypeConfig into a MetsTypeHandler.
	 * The MetsTypeHandler is more efficient for testing if a tag is supported.
	 * 
	 * @param metsTypeKey
	 * @param metsTypeConfigs
	 * @return
	 */
	private MetsTypeHandler setupMetsTypeHandlers(String metsTypeKey, List<MetsTypeConfig> metsTypeConfigs) {
		MetsTypeHandler mth = new MetsTypeHandler(metsTypeKey);
		
		for ( MetsTypeConfig metsTypeConfig : metsTypeConfigs ) {			
			if (metsTypeConfig.export) {
				mth.add(metsTypeConfig);
			}
		}
		
		return mth;
	}
	
	//================================================================================
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		
		String notset = "Not Set";
		
		stringBuilder.append("ExportConfig:\n");
		
		stringBuilder.append("- Mets Type Configs:\n");
		if (this.metsTypeMap != null) {
			
			for (String metsTypeKey : metsTypeMap.keySet()) {
				this.addToStringBuilder(stringBuilder, this.metsTypeMap.get(metsTypeKey), metsTypeKey);
			}
		}
		
		stringBuilder.append("- METS Getter:\n");
		if (this.metsGetter != null) {
			stringBuilder.append( String.format(" - URL   : %s\n", Optional.ofNullable(this.metsGetter.url).orElse(notset) ));
			stringBuilder.append( String.format(" - Class : %s\n", Optional.ofNullable(this.metsGetter.clazz).orElse(notset) ));
		}

		stringBuilder.append("- ARK:\n");
		if (this.ark != null) {
			stringBuilder.append( String.format(" - Use ID as ARK : %s\n", Boolean.toString(this.ark.useIdAsArk)) );
			stringBuilder.append( String.format(" - Class         : %s\n", Optional.ofNullable(this.ark.prefix).orElse(notset) ));
		}
		
		stringBuilder.append("- Remote Identifier:\n");
		if (this.remoteIdentifier != null) {
			stringBuilder.append( String.format(" - Enable : %s\n", Boolean.toString(this.remoteIdentifier.enable)) );
			stringBuilder.append( String.format(" - URL    : %s\n", Optional.ofNullable(this.remoteIdentifier.url).orElse(notset)) );
			stringBuilder.append( String.format(" - Class  : %s\n", Optional.ofNullable(this.remoteIdentifier.clazz).orElse(notset)) );
			stringBuilder.append( String.format(" - Prefix : %s\n", Optional.ofNullable(this.remoteIdentifier.prefix).orElse(notset)) );
		}
		
		stringBuilder.append( String.format("- Viewer Host Base URL:\n   %s\n", Optional.ofNullable(this.viewerHostBaseURL).orElse(notset) ));
		
		stringBuilder.append("- Export Dublin Core:\n");
		if (this.exportDublinCore != null) {
			addToStringBuilder(stringBuilder, this.exportDublinCore);
		}
		
		stringBuilder.append("- Export Solr:\n");
		if (this.exportSolr != null) {
			addToStringBuilder(stringBuilder, this.exportSolr);
		}
		
		stringBuilder.append("- Primo:\n");
		if (this.primo != null) {
			stringBuilder.append( String.format(" - OAI Host URL       : %s\n", Optional.ofNullable(this.primo.oaiHostURL).orElse(notset) ));
			stringBuilder.append( String.format(" - Record Identifier  : %s\n", Optional.ofNullable(this.primo.recordIdentifier).orElse(notset) ));
		}
		
		stringBuilder.append("- Solr:\n");
		if (this.solr != null) {
			stringBuilder.append( String.format(" - ZooKeeper Host     : %s\n", Optional.ofNullable(this.solr.zkHost).orElse(notset) ));
			
			stringBuilder.append( String.format(" - Article URL        : %s\n", Optional.ofNullable(this.solr.articleURL).orElse(notset) ));
			stringBuilder.append( String.format(" - Article Collection : %s\n", Optional.ofNullable(this.solr.articleCollection).orElse(notset) ));
			
			stringBuilder.append( String.format(" - Page URL           : %s\n", Optional.ofNullable(this.solr.pageURL).orElse(notset)));
			stringBuilder.append( String.format(" - Page Collection    : %s\n", Optional.ofNullable(this.solr.pageCollection).orElse(notset)));
		}
				
		return stringBuilder.toString();
	}
	
	private void addToStringBuilder(StringBuilder sb, List<MetsTypeConfig> mtcs, String label) {
		for (MetsTypeConfig mtc : mtcs) {
			sb.append( String.format(" - [%1$-10s] ", label) + mtc.toString() + "\n");
		}
	}
	
	private void addToStringBuilder(StringBuilder sb, List<String> values) {
		for (String s : values) {
			sb.append( String.format(" - %s\n", s));
		}
	}
	
}
