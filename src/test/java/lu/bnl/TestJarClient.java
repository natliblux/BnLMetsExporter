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
package lu.bnl;

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Deprecated
/** TODO: Refactor and rewrite all tests. Ideally, based on Open Data local folder
 */
public class TestJarClient {

	@Test
	public void testConfig() throws ParseException {
		run(" -tc");
	}
	
	private void run(String cmdRun) throws ParseException {
		BnLMetsExporter client = new BnLMetsExporter();
		
		String[] args=null;
		if (cmdRun != null) {
			args = cmdRun.split(" ");
		}

		client.parseArguments(args);
	}
}
