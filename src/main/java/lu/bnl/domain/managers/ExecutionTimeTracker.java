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
package lu.bnl.domain.managers;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionTimeTracker {

	private static final Logger logger = LoggerFactory.getLogger(ExecutionTimeTracker.class);
	
	private static final String FORMAT_DURATION = "H:mm:ss";
	
	private long start;
	
	public ExecutionTimeTracker() {
		
	}
	
	public long start() {
		return System.currentTimeMillis();
	}
	
	/**
	 * Stops counter time and returns duration from last start (in seconds)
	 * @param inStart
	 * @param msg
	 * @return
	 */
	public long stop(Long inStart, String msg) {
		long s = (inStart != null) ? inStart.longValue() : start;
		String m = (msg != null) ? msg : "";
		
		long duration = Math.round(System.currentTimeMillis() - s);
		String text = DurationFormatUtils.formatDuration(duration, FORMAT_DURATION);
		
		console(String.format("**** %s done in %s", m, text));
		
		return duration / 1000;
	}
	
	/**
	 * Show percentage, elapsed time and ETA time
	 * 
	 * @param inStart
	 * @param msg
	 * @param i
	 * @param count
	 * @param startGlobal
	 */
	public synchronized void stopETA(Long startGlobal, String msg, int i, int count) {
		long s = (startGlobal != null) ? startGlobal.longValue() : start;
		String m = (msg != null) ? msg : "";
		
		long durationMillis = System.currentTimeMillis() - s;
		//infoline("**** "+m+" done in "+duration+"s");
		String elapsed = DurationFormatUtils.formatDuration(durationMillis, FORMAT_DURATION);
		
		long totalMillis = durationMillis * count / i;
		long etaMillis = totalMillis - durationMillis;
		
		String eta = DurationFormatUtils.formatDuration(etaMillis, FORMAT_DURATION);
		String total = DurationFormatUtils.formatDuration(totalMillis, FORMAT_DURATION);

		int percent = Math.round(i * 100 / count);
		infoline("==> "+m+" : "+percent+"% in "+elapsed + " ; total: "+total+", ETA "+ eta + "                                                      ");
	}
	
	/**
	 * Print a message in the console on the same line.
	 * 
	 * @param msg
	 */
	private void infoline(String msg) {
		System.out.print(msg+"\r");
		//System.out.println(msg);
		logger.info(msg);
	}

	/**
	 * Show message in console and in file.
	 * 
	 * @param msg
	 */
	public void console(String msg) {
		logger.info(msg);
		
		//Show it in console
		System.out.println(msg);
	}
	
}
