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
package lu.bnl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lu.bnl.files.FileUtil;

public class CmdUtil implements Runnable {

	private String cmd = null;
	
	private String dir = null;
	
	private boolean noError = false;

	public static void runCmd(String cmd, String dir) {
		runCmd(cmd, dir, false);
	}
	
	public static void runCmd(String cmd, String dir, boolean noError) {
		Thread thread = new Thread(new CmdUtil(cmd, dir, noError));
		thread.start();
	}
	
	private CmdUtil(String cmd, String dir, boolean noError) {
		this.cmd = cmd;
		this.dir = dir;
		this.noError = noError;
	}
	
	 @Override
	 public void run() {
		//File fileDir = new File(dir, this.userDir);
		
		File file = FileUtil.checkDir(dir);
		if (file == null){
			//use
			dir = System.getProperty("user.dir");
			file = FileUtil.checkDir(dir);
		}
		
		try {
			Process p = Runtime.getRuntime().exec(cmd, null, file);

			new Thread(new ConsoleWriter(p.getInputStream())).start();
            if ( ! noError ) {
            	new Thread(new ConsoleWriter(p.getErrorStream())).start();
            }
            
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class ConsoleWriter implements Runnable {

	    private final InputStream inputStream;

	    ConsoleWriter(InputStream inputStream) {
	        this.inputStream = inputStream;
	    }

	    private BufferedReader getBufferedReader(InputStream is) {
	        return new BufferedReader(new InputStreamReader(is));
	    }

	    @Override
	    public void run() {
	        BufferedReader br = getBufferedReader(inputStream);
	        String line = "";
	        try {
	            while ((line = br.readLine()) != null) {
	                System.out.println(line);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}

}
