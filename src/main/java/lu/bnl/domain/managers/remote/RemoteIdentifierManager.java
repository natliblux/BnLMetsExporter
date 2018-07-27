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
package lu.bnl.domain.managers.remote;

import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.configuration.RemoteIdentifierConfig;
import lu.bnl.domain.model.DivSection;

/** Abstract class to implement your own manager that will query a 
 *  remote system or do any other computation to get the right ID
 *  based on another initial ID.
 */
public abstract class RemoteIdentifierManager {

	/**
	 * Get the new ID given the PID.
	 * This method should be overwritten.
	 * 
	 * @param pid	
	 * @return
	 */
	public String getIdByPID(String pid) {
		return null;
	}
	
	/**
	 * Get the new ID given the PID and an DivSection object.
	 * This method should be overwritten.
	 * 
	 * @param pid
	 * @param article
	 * @return
	 */
	public String getIdByPID(String pid, DivSection article) {
		return null;
	}
	
	/**
	 * Takes the clazz parameter from the app configuration and tries to create a class
	 * that is a subclass of RemoteIdentifierManager. This allows that the RemoteIdentifierManager
	 * is configurable and reduces the amount of code that has to change.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static RemoteIdentifierManager getInstanceForConfig() throws Exception {
		
		RemoteIdentifierConfig ric = AppConfigurationManager.getInstance().getExportConfig().remoteIdentifier;
		if (ric != null && ric.enable) {
			Class<? extends RemoteIdentifierManager> c = Class.forName(ric.clazz).asSubclass(RemoteIdentifierManager.class);
			return c.newInstance();	
		}
		
		return null;
	}
	
}
