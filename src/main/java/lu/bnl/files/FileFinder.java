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
package lu.bnl.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFinder {
	
	/** Find all METS files in the path. A METS file ends with "-mets.xml";
	 * 
	 * @param path
	 * @return
	 */
	public static List<File> findMetsFiles(String path) {
		
		List<File> files = null;
		
		try {
			ProcessMetsFile fileProcessor = new ProcessMetsFile();
			Files.walkFileTree(Paths.get(path), fileProcessor);
			files = fileProcessor.getFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return files;
	}
	
	public static Map<String, File> findAlto(String path) {
		Map<String, File> files = null;
		try {
			ProcessAltoFile fileProcessor = new ProcessAltoFile();
			Files.walkFileTree(Paths.get(path), fileProcessor);
			files = fileProcessor.getFiles();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return files;
	}

	private static final class ProcessMetsFile extends SimpleFileVisitor<Path> {
		
		private List<File> files = new ArrayList<File>();
		
		@Override
		public FileVisitResult visitFile(Path aFile, BasicFileAttributes aAttrs) throws IOException {
			String filename = aFile.getFileName().toFile().getName();
			if ( filename.endsWith("-mets.xml") ){
				files.add(aFile.toFile());
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path aDir, BasicFileAttributes aAttrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		public List<File> getFiles() {
			return files;
		}

	}
	 
	private static final class ProcessAltoFile extends SimpleFileVisitor<Path> {
		private Map<String, File> files = new HashMap<String, File>();
		Pattern p = Pattern.compile("\\d+");
		
		@Override
		public FileVisitResult visitFile(Path aFile, BasicFileAttributes aAttrs)
				throws IOException {
			String filename = aFile.getFileName().toFile().getName();
			Matcher m = p.matcher(filename);
			if (m.matches()){
				files.put(filename, aFile.toFile());
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path aDir,
				BasicFileAttributes aAttrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		public Map<String, File> getFiles() {
			return files;
		}

	}


}
