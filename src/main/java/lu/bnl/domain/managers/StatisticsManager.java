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
package lu.bnl.domain.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsManager {

	private static final Logger logger = LoggerFactory.getLogger(StatisticsManager.class);
	
	private static StatisticsManager instance = new StatisticsManager();
	
	private Map<String, Integer> counters;
	
	private StatisticsManager() {
		this.counters = new HashMap<>();
	}
	
	public static StatisticsManager getInstance() {
		return instance;
	}
	
	//================================================================================
	
	private synchronized void addCount(String key, int n) {
		if (this.counters.containsKey(key) ) {
			this.counters.put(key, this.counters.get(key) + n);
		} else {
			this.counters.put(key, n);
		}
	}
	
	//================================================================================
	
	public void logStatistics() {
		
		this.log("Statistics");
		this.log("---------------------------------------------------");
		
		List<String> sortedKeys = new ArrayList<>(this.counters.keySet());
		Collections.sort(sortedKeys);
		
		for ( String key : sortedKeys ) {
			String message = String.format(" - %-20s : %10s", key, this.counters.get(key).toString());
			this.log(message);
		}
		
		this.log("---------------------------------------------------");
	}
	
	//================================================================================
	
	public void countFiles(int n) {
		this.addCount("Files", n);
	}
	
	public void countPages(int n) {
		this.addCount("Pages", n);
	}
	
	public void countArticles(int n) {
		this.addCount("Articles", n);
	}
	
	public void countSkipArticles(int n) {
		this.addCount("Skip Articles", n);
	}
	
	public void countLines(int n) {
		this.addCount("Lines", n);
	}
	
	public void countWords(int n) {
		this.addCount("Words", n);
	}
	
	// Useful for now
	public void countCustom(String key, int n) {
		this.addCount(key, n);
	}
	
	//================================================================================
	
	public synchronized int getCustomValue(String key) {
		return this.counters.get(key);
	}
	
	//================================================================================
	
	private void log(String message) {
		System.out.println(message);
		logger.info(message);
	}
	
}
