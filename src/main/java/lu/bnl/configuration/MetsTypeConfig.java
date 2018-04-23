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
package lu.bnl.configuration;

/**
 * MetsTypeConfig describes the configuration for a METS file of type newspaper, serial, etc.
 * Each type describes what export behavior should be used for a tag of TYPE `type`.
 * The behavior defines the panel to be used (e.g pp, pi) and if the article part should be 
 * added in the URL.
 */
public class MetsTypeConfig {

	/** The value of the TYPE attribute.
	 *  E.g. ISSUE, ARTICLE, SECTION, ...
	 */
	public String type;
	
	/** The default title. 
	 *  Set only if different than "Sans titre"
	 */
	public String title = "";
	
	/** The panel to be used in the viewer. 
	 *  This is the <hasVersion> part of the exported Dublin Core Document.
	 *  Default value is "pp".
	 */
	public String panel = "pp";
	
	/** True if the article part of the URL, e.g. "|article:DTL-XX", should be added to not.
	 *  Default is true.
	 */
	public boolean isArticle = true;
	
	/** True if this type should be exported, false otherwise.
	 *  Used to disable a type without having to remove it in the config.
	 *  Default is True.
	 */
	public boolean export = true;
	
	/** Sets the prefered DmdSec to choosed when multilple DMDID are set.
	 *  Possible formats are "mods", "marc".
	 *  Default is "mods".
	 */
	public String preferdmd = "mods";
	
	@Override
	public String toString() {
		return new StringBuilder()
				.append( String.format("Type: %1$-25s , ", type) )
				.append( String.format("Title: %1$-20s , ", title) )
				.append( String.format("Panel: %1$-5s , ", panel) )
				.append( String.format("IsArticle: %1$-5s , ", isArticle) )
				.append( String.format("Prefered DMD: %1$-5s , ", preferdmd) )
				.append( String.format("Export: %s.", export) )
				.toString();
	}
	
}
