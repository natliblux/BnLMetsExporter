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
package lu.bnl.domain.constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lu.bnl.configuration.MetsTypeConfig;

public class MetsTypeHandler {

	private final String typeName;
	
	private Set<String> exportedTypes;

	private Map<String, MetsTypeConfig> metsTypeConfigMap;
	
	public MetsTypeHandler(String typeName) {
		this.typeName = typeName;
		
		this.exportedTypes = new HashSet<>();
		
		this.metsTypeConfigMap = new HashMap<>();
	}
	
	public String getTypeName() {
		return this.typeName;
	}

	
	public String getDefaultTitle() {
		return "Sans titre";
	}
	
	/** Add a type with a title associated. Add it in the set of 
	 *  exportable types if isExportable is true.
	 * 
	 * @param metsTypeConfig
	 */
	public void add(MetsTypeConfig metsTypeConfig) {
		this.exportedTypes.add(metsTypeConfig.type);
		
		this.metsTypeConfigMap.put(metsTypeConfig.type, metsTypeConfig);
	}
	
	/**
	 * Return the title associated with the type. 
	 * Return null if type is not found.
	 * 
	 * @param typeName
	 * @return
	 */
	public String getTitle(String typeName) {
		MetsTypeConfig config = this.metsTypeConfigMap.get(typeName);
		if (config == null) {
			return this.getDefaultTitle();
		} else {
			return config.title;
		}
	}
	
	public String getPanel(String typeName) {
		MetsTypeConfig config = this.metsTypeConfigMap.get(typeName);
		if (config == null) {
			return null;
		} else {
			return config.panel;
		}
	}
	
	public boolean isArticle(String typeName) {
		MetsTypeConfig config = this.metsTypeConfigMap.get(typeName);
		if (config == null) {
			return false;
		} else {
			return config.isArticle;
		}
	}
	
	public String getPreferredDmd(String typeName) {
		MetsTypeConfig config = this.metsTypeConfigMap.get(typeName);
		if (config == null) {
			return null;
		} else {
			return config.preferdmd;
		}
	}
	
	/**
	 * Return true if the typeName is in the set of exportable types.
	 * Return false otherwise.
	 * 
	 * @param typeName
	 * @return
	 */
	public boolean contains(String typeName) {
		return this.exportedTypes.contains(typeName);
	}
	
}
