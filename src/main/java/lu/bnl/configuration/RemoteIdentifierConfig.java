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

public class RemoteIdentifierConfig {

	/** Enable or disable the usage of remote
	 */
	public boolean enable = false;
	
	/** The url to request the remote ID.
	 */
	public String url;
	
	/** The name of the class, including package, that will be used as a RemoteIdentifierManager
	 */
	public String clazz;

	/** The prefix to put in front of the identifier.
	 *  This can be any string and most often will be a URL.
	 *  Remove or leave as empty string if you don't want to use this.
	 */
	public String prefix;
	
}
