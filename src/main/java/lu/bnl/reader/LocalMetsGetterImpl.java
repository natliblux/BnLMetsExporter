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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.files.FileFinder;

public class LocalMetsGetterImpl extends MetsGetter {

	private static final Logger logger = LoggerFactory.getLogger(LocalMetsGetterImpl.class);
	
	@Override
	public void findAllMets(String path) {
		
		List<File> files = new ArrayList<>();
		
		try {
			files = FileFinder.findMetsFiles(path);
		} catch (Exception e) {
			logger.error("Failed to read METS directory at " + path, e);
			e.printStackTrace();
		}

		// DEBUG
		/*System.out.println(files.size());
		for (File f : files) {
			System.out.println("PATH: " + f.getPath());
		}*/
		
		List<String> data = files.stream()
								.map( f -> f.getPath())
								.collect(Collectors.toList());
		
		this.setMetsData(data);
	}
	
	/** The data must be a path to the METS file.
	 *  Return the entire METS file.
	 */
	@Override
	public String getMetsContent(String data) {

		String content = null;
		
		try {
			content = new String (Files.readAllBytes( Paths.get(data) ));
		} catch (IOException e) {
			logger.error("Failed to read METS file at " + data, e);
			e.printStackTrace();
		}
		
		return content;
	}

	
	/** The data is a path, the path is unique, and the METS file alone should be unique too.
	 * 	Example: examples\METS_DATA\1917564_newspaper_diekwochen_1848-05-13_01\1917564_newspaper_diekwochen_1848-05-13_01-mets.xml
	 *  will be mapped to 1917564_newspaper_diekwochen_1848-05-13_01
	 * 
	 *  TODO: The method should be reworked to allow METS files with different names and then
	 *  reuse the path to get a unique name, such as the current technical requirements.
	 *  path/to/indeplux/1930/1930-01-01_01/1930-01-01_01-mets.xml should be parsed into: indeplux_1930-01-01_01
	 *  
	 */
	@Override
	public String getUniqueName(String data) {
		
		File f = new File(data);
		
		String name = f.getName().replace("-mets", "").replace(".xml", "");
		
		return name;
	}
	
	
	@Override
	public String getMetsLocation(String data) {
		File f = new File(data);
		
		return f.getParent();
	}
	
}
