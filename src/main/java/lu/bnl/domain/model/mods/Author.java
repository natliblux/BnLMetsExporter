/*******************************************************************************
 * Copyright (C) 2017-2020 Bibliothèque nationale de Luxembourg (BnL)
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
package lu.bnl.domain.model.mods;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.model.Pair;

public class Author {

	public List<Pair> nameParts = new ArrayList<>();

	public String getCreator() {
		ArrayList<String> givens = new ArrayList<String>();
		ArrayList<String> families = new ArrayList<String>();
		
		String date = "";
		
		for (Pair pair : this.nameParts) {
			//logger.info("NAMEPART: "+ pair.value + " - " + pair.key);
			String namePartType = "NOTYPE";
			if (pair.key != null) {
				namePartType = pair.key;
			}
			
			if (namePartType.equalsIgnoreCase( MetsConstant.ATTR_NAMEPART_TYPE_GIVEN )) {
				givens.add(pair.value);
			} else if (namePartType.equalsIgnoreCase( MetsConstant.ATTR_NAMEPART_TYPE_FAMILY )) {
				families.add(pair.value);
			} else if (namePartType.equalsIgnoreCase( MetsConstant.ATTR_NAMEPART_TYPE_DATE )){
				date = String.format(" (%s)", pair.value);
			} else {
				// Ignore other NameParts
			}
		}
		
		String given = StringUtils.join(givens, " ");
		String family = StringUtils.join(families, " ");
		
		String spacer = "";
		if (family.length() > 0 && (given.length() > 0 || date.length() > 0)) {
			spacer = ", ";
		}
		
		String creator = String.format("%s%s%s%s", family, spacer, given, date); // If no date, date will just be an empty string
		
		return creator;
	}
	
}
