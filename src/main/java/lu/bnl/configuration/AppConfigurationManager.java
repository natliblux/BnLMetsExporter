/*******************************************************************************
 * Copyright (C) 2017-2019 Bibliothèque nationale de Luxembourg (BnL)
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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class AppConfigurationManager {

	private static final Logger logger = LoggerFactory.getLogger(AppConfigurationManager.class);
	
	private static AppConfigurationManager instance = new AppConfigurationManager();
	
	private ExportConfig exportConfig;
	
	private AppConfigurationManager() {
		
	}
	
	public static AppConfigurationManager getInstance() {
		return instance;
	}
	
	//================================================================================
	
	public boolean loadYmlExportConfig(String path) {
		logger.info("Loading YML config: " + path);
		boolean success = false;
		
		Yaml yaml = new Yaml( new Constructor( ExportConfig.class ) );
		
		try {
			InputStream inputStream = Files.newInputStream( Paths.get(path) );
			
			this.exportConfig = (ExportConfig) yaml.load(inputStream);
			this.exportConfig.convertConfig();
			
			System.out.println(this.exportConfig.toString());
			
			success = true;
		} catch (Exception e) {
			String message = String.format("Error while loading YAML file '%s'", path);
			System.out.println(message);
			logger.error(message, e);
			
			e.printStackTrace();
		}
		
		return success;
	}
	
	public ExportConfig getExportConfig() {
		return this.exportConfig;
	}
	
	
}
