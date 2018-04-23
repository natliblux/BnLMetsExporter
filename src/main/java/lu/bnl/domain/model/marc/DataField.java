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
package lu.bnl.domain.model.marc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataField {
	
	public String tag = null;
	
	public String ind1 = null;
	
	public String ind2 = null;
	
	private Map<String, String> subFields = new HashMap<>();
	
	public DataField(String tag, String ind1, String ind2) {
		this.tag = tag;
		this.ind1 = ind1;
		this.ind2 = ind2;
	}
	
	// PUT
	public void put(String key, String value) {
		this.subFields.put(key, value);
	}
	
	// GET
	public String get(String key) {
		return this.subFields.get(key);
	}
	
	// CONTAINS
	public boolean containsKey(String key) {
		return this.subFields.containsKey(key);
	}
	
	// KEYSET
	public Set<String> keySet() {
		return this.subFields.keySet();
	}
	
}
