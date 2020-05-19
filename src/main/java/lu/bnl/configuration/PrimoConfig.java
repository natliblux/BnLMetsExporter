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
package lu.bnl.configuration;

public class PrimoConfig {

	/** Defines the URL of the OAI server.
	 */
	public String oaiHostURL;
	
	/** The base of the identifier tag of the OAI message containing the Dublin Core object.
	 */
	public String recordIdentifier;
	
	/** The pattern to use for creating the hasVersion tag.
	 */
	public String hasVersionPattern;

	/** Code for alternativeTitle if the partNumber is not valid.
	 */
	public String alternativeTitleCode;
	
}
