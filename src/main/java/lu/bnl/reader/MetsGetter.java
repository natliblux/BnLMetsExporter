/*******************************************************************************
 * Copyright (C) 2018 Bibliothèque nationale de Luxembourg (BnL)
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

import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.configuration.MetsGetterConfig;
import lu.bnl.io.ArchiverFilenameStrategy;

public abstract class MetsGetter {

	private List<String> metsData = new ArrayList<>();
	
	/** Validate the command line inputs.
	 *  MetsGetter processes directory and/or items.
	 * 
	 * @param dir
	 * @param items
	 * @return The data to be used as input data
	 */
	public abstract String validateInput(String dir, String items);

	/** Finds and return the list of all paths to METS file in a specific path.
	 * 
	 * @param path	The path to find METS files.
	 * @return	The list of METS file paths.
	 */
	public abstract void findAllMets(String path);
	
	/** Get the content of the METS file as String.
	 * 
	 * @param data	Should be an ID, resulting from the findAllMets. This is decided by the implementation.
	 * @param path	Should be a path, to complement the data. This is decided by the implementation.
	 * @return The METS file as String.
	 */
	public abstract String getMetsContent(String data, String path);

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
	
	/** Returns the ArchiverFilenameStrategy for saving the exported documents.
	 * 
	 * @return
	 */
	public abstract ArchiverFilenameStrategy getArchiverFilenameStrategy();

	public List<String> getMetsData() {
		return metsData;
	}

	public void setMetsData(List<String> metsData) {
		this.metsData = metsData;
	}
	
	/**
	 * Takes the clazz parameter from the app configuration and tries to create a class
	 * that is a subclass of MetsGetter. This allows that the MetsGetter
	 * is configurable and reduces the amount of code that has to change.
	 * 
	 * @return A new instance of a MetsGetter, null otherwise.
	 * @throws Exception
	 */
	public static MetsGetter getInstanceForConfig() throws Exception {
		MetsGetterConfig metsGetterConfig = AppConfigurationManager.getInstance().getExportConfig().metsGetter;
		if (metsGetterConfig != null) {
			Class<? extends MetsGetter> c = Class.forName(metsGetterConfig.clazz).asSubclass(MetsGetter.class);
			return c.newInstance();	
		}
		
		return null;
	}

}
