/*******************************************************************************
 * Copyright (C) 2018 Bibliotèque nationale de Luxembourg (BnL)
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
package lu.bnl.reader;

import java.util.ArrayList;
import java.util.List;

public abstract class MetsGetter {

	private List<String> metsData = new ArrayList<>();
	
	/** Finds and return the list of all paths to METS file in a specific path.
	 * 
	 * @param path	The path to find METS files.
	 * @return	The list of METS file paths.
	 */
	public abstract void findAllMets(String path);
	
	/** Get the content of the METS file as String.
	 * 
	 * @param data	Can be any string, such as a PID, URL or Path. This is decided by the implementation.
	 * @return The METS file as String.
	 */
	public abstract String getMetsContent(String data);

	/** Return the unique name for a given data. 
	 * 
	 * @param data Can be any string, such as a PID, URL or Path. This is decided by the implementation.
	 * @return The unique name for that data as String.
	 */
	public abstract String getUniqueName(String data);
	
	/** Returns the location of the where the METS is stored.
	 *  In case of a local path "path/to/mets.xml", this method should return "path/to/".
	 * 
	 * @param data
	 * @return
	 */
	public abstract String getMetsLocation(String data);
	
	public List<String> getMetsData() {
		return metsData;
	}

	public void setMetsData(List<String> metsData) {
		this.metsData = metsData;
	}
	
}
