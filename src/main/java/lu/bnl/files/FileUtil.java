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
package lu.bnl.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil<T> {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public static final String separator = "/";

	public static void save(String content, String filename) {
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));
			try {
				out.write(content);
			} finally {
				out.close();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String normalizePath(String value) {
		return value.replaceAll("\\\\", separator).replaceFirst("^\\w\\:", "");
	}
	
	public static List<String> readDocumentIDs(String file, boolean randomly)  {
		List<String> documentIDs = null;
		
		try {
			documentIDs = FileUtils.readLines( new File(file), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (randomly) {
			Collections.shuffle(documentIDs);
		}
		
		return documentIDs;
	}
	
	public static File checkDir(String dir) {
		File file = null;
		if (dir != null){
			file = new File(dir);
		}
		
		return checkDir(file);
	}
	
	public static File checkDir(File file) {
		if ( !( file != null  && file.exists() && file.isDirectory() ) ){
			file = null;
		}
		
		return file;
	}

	public static File checkFile(String path) {
		File file = null;
		if (path != null){
			file = new File(path);
		}
		
		return checkFile(file);
	}
	
	public static File checkFile(File file) {
		if ( !( file != null  && file.exists() && file.isFile() ) ){
			file = null;
		}
		
		return file;
	}

	public static long computeSize(File dir) {
		if (dir == null){
			return 0;
		} else {
			return FileUtils.sizeOfDirectory(dir);
		}
	}
	

	/**
	 * Takes a path and a default filename as parameter and tests if the
	 * path given is a file. If it is a directory, then the full path will
	 * be adapted to point to a file such as path/to/[defaultFilename].
	 * 
	 * @param path The path to test and enforce to be a file
	 * @param defaultFileName The default filename to use if the path is not a file.
	 * @return Returns the fixed path or just the path if nothing has been changed.
	 */
	public static String enforceFile(String path, String defaultFilename) {
		File file = new File(path);
		if ( file.isDirectory() ) {
			if (path.endsWith("/")) {
				path += defaultFilename;
			} else {
				path += "/" + defaultFilename;
			}
		}
		return path;
	}
	
	/**
	 * Save list of items in a file
	 * 
	 * @param separators
	 * @param filename
	 * @param key
	 */
	public void saveListInFile(List<T> separators, String filename, String key) {
		if (separators != null && !separators.isEmpty()) {
			try {
				FileUtils.writeLines(new File(filename), separators);
				logger.info(String.format("List %s saved into file : %s", key, filename));
			} catch (IOException e) {
				logger.error("Cannot save file for " + key, e);
			}
		}
	}
	

}

