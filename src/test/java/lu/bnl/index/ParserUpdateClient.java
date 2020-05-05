/*******************************************************************************
 * Copyright (C) 2017-2020 Bibliotèque nationale de Luxembourg (BnL)
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
package lu.bnl.index;

import java.io.File;
import java.util.List;

import lu.bnl.BnLMetsExporter;
import lu.bnl.files.FileFinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
@Deprecated
/** TODO: Refactor and rewrite all tests. Ideally, based on Open Data local folder
 */
public class ParserUpdateClient {

	private static final Logger logger = LoggerFactory.getLogger(ParserUpdateClient.class);

	private static String METS_DIR = "C:\\dev\\Data\\DTL_EXPORT_RANDOM";
	private static final String ALTO1 = "\\945\\1086945";

	private boolean USE_CLOUD = true;
	
	private BnLMetsExporter client = null;
	
	@Before
    public void setUp() {
		client = new BnLMetsExporter(USE_CLOUD);
    }

	/*@Test
	public void postAllMets() {
		logger.info("Post all mets files...");
		List<File> files = FileFinder.findFiles(METS_DIR);
		client.browseMetsFromFiles(files);
	}

	@Test
	public void post10Mets() {
		logger.info("Post all mets files...");
		List<File> files = FileFinder.findFiles(METS_DIR);
		files = files.subList(0, 10);
		client.browseMetsFromFiles(files);
	}

	@Test
	public void postSingleMets() {
		String path = ALTO1;// mets 1086958

		logger.info("Post single mets files...");
		List<File> files = FileFinder.findFiles(METS_DIR + path);
		client.browseMetsFromFiles(files);
	}*/

	/*@Test
	public void clearAll() {
		client.clearAll();
	}


	@Test
	public void setupShards() {
		client.setupShards(2, 10, SHARD_DEST, SHARD_TPL);
	}
	 */
}
