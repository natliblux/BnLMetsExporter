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
package lu.bnl.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LuceneUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(LuceneUtils.class);
	
	private static final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
	
	public static List<String> tokenizeString(Analyzer analyzer, String string) {

    	List<String> result = new ArrayList<String>();
        try {
          
        	TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
        	stream.reset();
        	while (stream.incrementToken()) {
        		result.add(stream.getAttribute(CharTermAttribute.class).toString());
        	}
        	stream.close();
          
        } catch (IOException e) {
          // Should never thrown, because we're using a string reader...
          throw new RuntimeException(e);
        }
        
        return result;
    }
	
	public List<String> parse(String text) {
		return LuceneUtils.tokenizeString(analyzer, text);
	}
	
	/*@Test
	public void testAnalyzeText() {
		String text = "Muller-Wagner.";
		LuceneUtils lu = new LuceneUtils();
		List<String> result = lu.parse(text);
		logger.info(String.valueOf(result));

	}*/
}