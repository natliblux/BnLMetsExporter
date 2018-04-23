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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;

/**
 * This class extends RollingFileAppender of log4j.
 * The difference is that UniqueRollingFileAppender
 * create a unique file name based on the timestamp
 * of when the program has started, more precisely,
 * when log4j is initialized.
 * 
 * @author Ralph Marschall
 */
public class UniqueFileAppender extends FileAppender {

	@Override
	public void setFile(String file) {

		final String timestampPlaceholder = "%timestamp";
		
		if (file.indexOf(timestampPlaceholder) >= 0) {
            Date d = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss-SS");
            file = file.replaceAll(timestampPlaceholder, format.format(d));
        }
		
        super.setFile(file);
	}
	
}
