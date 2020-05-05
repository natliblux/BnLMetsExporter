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
package lu.bnl.io;

import lu.bnl.reader.MetsGetter;

public enum ArchiverFilenameStrategy {

	PID_DIGITOOL, 	// This strategy is for digitool data with a PID
	ARK,			// This startegy is for ark like 'ark:naan/name'
	ORIGINAL_DIR 	// This strategy is for local mets files, where the parent path will be used. e.g path/to/mets without /my_mets.xml
	;
	
	public static ArchiverFilenameStrategy getPreferedStrategyForMetsGetter(MetsGetter metsGetter) {
		return metsGetter.getArchiverFilenameStrategy();
	}
	
}
