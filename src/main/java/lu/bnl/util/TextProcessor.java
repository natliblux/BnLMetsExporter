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
package lu.bnl.util;

/** 
 * Class to perform more advanced Text Processing methods.
 * 
 * For basic text and string manipulatio, there are other "StringUtils" libraries.
 * 
 * @author Ralph Marschall
 */
public class TextProcessor {

	/**
	 * Replace certain characters that could create unwanted behaviors
	 * 
	 * @param text
	 * @return
	 */
	public static String escapeDangerousHtmlCharacters(String text) {
		if (text == null) { // Protect against null.
			return text;
		}
		
		String processed = text.replace(">", "&gt;").replace("<", "&lt;");
		
		return processed;
	}
	
}
