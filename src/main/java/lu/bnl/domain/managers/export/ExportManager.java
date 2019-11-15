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
package lu.bnl.domain.managers.export;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.AppGlobal;
import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.constants.MetsTypeHandler;
import lu.bnl.domain.managers.ExecutionTimeTracker;
import lu.bnl.domain.managers.StatisticsManager;
import lu.bnl.domain.model.ArticleDocumentBuilder;
import lu.bnl.domain.model.DivSection;
import lu.bnl.domain.model.DmdSection;
import lu.bnl.reader.MetsGetter;
import lu.bnl.util.ArkUtils;
import lu.bnl.xml.MetsXMLParserHandler;

public abstract class ExportManager {

	private static final Logger logger = LoggerFactory.getLogger(ExportManager.class);

	private ExecutionTimeTracker executionTimeTracker;

	// Time Tracker
	// ================================================================================

	public long startGlobal;

	public int count;

	public ExportManager() {
		this.executionTimeTracker = new ExecutionTimeTracker();
	}

	public abstract void run(MetsGetter metsGetter);

	protected void console(String message) {
		this.executionTimeTracker.console(message);
	}

	protected ExecutionTimeTracker getExecutionTimeTracker() {
		return this.executionTimeTracker;
	}

	protected int getRuntimeCores() {
		int cores = Runtime.getRuntime().availableProcessors();
		console(String.format("%d cores available.", cores));

		return cores;
	}

	protected int getReasonableNumberOfAvailableCores(int workload) {
		return Math.min(getRuntimeCores() - 1, workload);
	}
	
	protected List<List<String>> getPartitions(List<String> documentIDContentLists, int cores) {
		int partitionSize = documentIDContentLists.size() / cores;
		List<List<String>> partitions = new LinkedList<List<String>>();
		for (int i = 0; i < documentIDContentLists.size(); i += partitionSize) {
			List<String> sublist = documentIDContentLists.subList(i, Math.min(i + partitionSize, documentIDContentLists.size()));
		    partitions.add(sublist);
		    
		    console(String.format(" - Partition %d : %d Document IDs", i, sublist.size()));
		}

		return partitions;
	}

	protected void printStats() {
		console(""); // Go next line in console, because or stopETA doing same line return

		long duration = this.getExecutionTimeTracker().stop(startGlobal, "[All parsed]");

		StatisticsManager.getInstance().logStatistics();

		double avg = (((double) duration) / ((double) count));
		console(String.format("Average time/file : %.2f s", avg));

		double hours = (AppGlobal.FILES_TOTAL * avg) / (60 * 60);
		console(String.format("Projection for %d files: %.2f h = %.2f days.", AppGlobal.FILES_TOTAL, hours, (hours / 24)));
	}
	
	public static ArticleDocumentBuilder getArticleDocumentBuilder(String documentID, DivSection article, MetsXMLParserHandler handler) {
		String id 		= article.getId();
		String text 	= article.getText();
		String type 	= article.getType();
		
		String lineText = article.getTextInLineFormat();
		String wordText = article.getHtmlizedWords(true);

		// Define variables
		
		String ark				= ArkUtils.getArkForArticle(documentID, article.getType(), article.getId());
		String title 			= null;
		String alternative 		= null;
		String publisher 		= null;
		String recordIdentifier = null;
		String date = null;

		List<String> isPartOfs = new ArrayList<>();
		List<String> creators  = new ArrayList<>();
		List<String> languages = new ArrayList<>();

		// Step 0 - Gather handlers, preferences and dmdSec objects
		// ################################################################################

		MetsTypeHandler currentMetsTypeHandler = handler.getMetsTypeHandler();

		// Select the preferred DMDID
		String dmdid = article.getDmdid();

		String preferedDmdId = ExportManager.choosePreferredDmdId(dmdid, type, handler);
		if (preferedDmdId != null) {
			dmdid = preferedDmdId;
			// System.out.println("Preferred: " + dmdid);
			logger.debug("Preferred: " + dmdid);
		}

		// Get the preferred DmdSection
		DmdSection dmd = handler.getDmdSection(dmdid);

		// Careful! dmdSec Issue is only for Newspaper and Serials
		// 2018, replaced by MODSMD_COLLECTION below with backup to MODSMD_ISSUE?
		// DmdSection issueDmd = handler.getDmdSectionIssue();

		// Priority is MODSMD_COLLECTION
		// 1) If it does not exists, then try MODSMD_ISSUE
		// 2) Otherwise it will be null
		boolean useCollection = true;

		DmdSection collectionDmd = handler.getDmdSectionCollection();
		if (collectionDmd == null) {
			useCollection = false;
			collectionDmd = handler.getDmdSectionIssue();
		}

		// Step 1 - Get metadata from current DMDSec element
		// ################################################################################

		// Get data from the preferred DmdSection
		if (dmd != null) {

			// TITLE

			title = dmd.getTitle();
			// logger.info("1: TITLE: " + title);

			// CREATOR

			creators = dmd.getCreators();

			// LANGUAGE

			languages = dmd.getLanguages();

			// DATE
			// date = dmd.getDateIssued(); // Commented because handled below

			// PUBLISHER
			// publisher = dmd.getPublisher();

			// ISPARTOF

			// FIX FOR INDEPLUX
			// NEED TO REVISE DMDSECTION getIsPartOfs in case of MARC vs MODS
			/*
			 * if (collectionDmd != null) { isPartOfs = collectionDmd.getIsPartOfs(); }
			 */

			if (dmdid.contains("MARCMD")) {
				System.out.println(title + " ; " + creators.size() + " ; " + languages.size() + " ; " + date + " ; "
						+ isPartOfs.size() + " ; " + publisher);
			}

		}

		// Check empty values and take from other DmdSections if needed:

		// Fix Title if empty
		if (StringUtils.isBlank(title)) {
			// 1st cascade, mods null use LABEL attribute
			title = article.getLabel();

			if (StringUtils.isBlank(title)) {
				// 2nd cascade, use a constant type-dependant
				title = currentMetsTypeHandler.getTitle(type);
			}
		}

		// logger.info("2: TITLE: " + title);

		// IMPORTANT NOTE:
		// If some information has not been found in the preferred DmdSection,
		// then the information can be extracted from the default MODS DmdSection.
		// This can be done for: recordIdentifier, date, isPartOf, publisher.

		// Step 2 - Complete the metadata with MODSMD_COLLECTION (or MODSMD_ISSUE) and
		// MODSMD_PRINT
		// ################################################################################

		boolean shouldCompleteWithMods = true;

		if (shouldCompleteWithMods) {

			// Issue MODS
			// DmdSection dmdIssue = handler.getDmdSectionIssue();

			// This is for backward compatibility to documents with MODSMD_ISSUE
			if (collectionDmd != null) {
				if (StringUtils.isBlank(recordIdentifier))
					recordIdentifier = collectionDmd.getRecordIdentifer();

				if (StringUtils.isBlank(date))
					date = collectionDmd.getDateIssued();

				if (isPartOfs.isEmpty())
					isPartOfs = collectionDmd.getIsPartOfs();

				// LEGACY data has this info in MDOSMD_ISSUE
				if (alternative == null)
					alternative = collectionDmd.getPartNumber();
			}

			// Print MODS
			DmdSection dmdSectionPrint = handler.getDmdSectionPrint();

			if (dmdSectionPrint != null) {
				if (StringUtils.isBlank(recordIdentifier) || useCollection)
					recordIdentifier = dmdSectionPrint.getRecordIdentifer();

				if (StringUtils.isBlank(date) || useCollection)
					date = dmdSectionPrint.getDateIssued();

				if (StringUtils.isBlank(publisher) || useCollection)
					publisher = dmdSectionPrint.getPublisher();

				// NEW data has this info in MODSMD_PRINT
				if (alternative == null) {
					alternative = dmdSectionPrint.getPartNumber();
				}
					
			}

			if (date != null) {
				try {
					Date dateObject = new SimpleDateFormat("yyyy-MM-dd").parse(date);
					String year = new SimpleDateFormat("yyyy").format(dateObject);
				
					if (alternative != null) {
						alternative = String.format("%s (%s)", alternative, year);
					} else {
						alternative = String.format("%s", year);
					}

				} catch (ParseException e) {
					logger.error("Failed to convert date for the alternative title.", e);
				}


			}
			
		}
		
		ArticleDocumentBuilder builder = new ArticleDocumentBuilder(id, documentID)
				.ark(ark)
				.dmdId(dmdid)
				.recordIdentifier(recordIdentifier)
				.date(date)
				.isPartOfs(isPartOfs)
				.publisher(publisher)
				.text(text)
				.lineText(lineText)
				.wordText(wordText)
				.title(title)
				.alternative(alternative)
				.creators(creators)
				.languages(languages)
				.type(type)
				.isArticle(currentMetsTypeHandler.isArticle(type))
				.panel(currentMetsTypeHandler.getPanel(type));
		
		controlBuilderQuality(builder);

		return builder;
	}
	
	/** Checks the DMDID to find. 
	 *  If DMDID is null, then use the DIVTYPE.
	 *  If DMDID contains one or more spaces ("MODS_ID_1 MODS_ID_2 MARC_ID_1"), 
	 *  then we split it and check each individual DMDID and select 
	 *  the preferred one as configured by the user. If more MODS, select the first one.
	 * 
	 * @param dmdId
	 * @param divType
	 * @param handler
	 * @return divType if dmdId is null. The preferred dmdId if it has spaces. Otherwise dmdid.
	 */
	public static String choosePreferredDmdId(String dmdId, String divType, MetsXMLParserHandler handler) {
		
		// If dmdid is null then set it to type such that we have final names such as 1382-ADVERTISMENT-12390.xml
		// Reason: We export many objects, not all have an associated dmdSec.
		if (dmdId == null) {
			return divType;
		}
		
		String chosenDmdId = null;
		if ( dmdId.contains(" ") ) {
			//System.out.println("DMD: Contains multiple DMDIDs : " + dmdId);
			
			MetsTypeHandler currentMetsTypeHandler = handler.getMetsTypeHandler();
			
			String preferedMdType = currentMetsTypeHandler.getPreferredDmd(divType);
			
			List<String> dmdIdOptions = new ArrayList<String>( Arrays.asList(dmdId.split(" ")) );
			
			// The default DmdId will be the first MODS found
			String defaultDmdId = null;
			
			for (String dmdIdOption : dmdIdOptions) {
				// 1. Find DmdSection
				DmdSection dmdSection = handler.getDmdSection( dmdIdOption );
				
				// 2. Get the Type 
				String dmdSectionMdType = dmdSection.mdType;
				
				//System.out.println(String.format("COMPARING: %s with %s", dmdSectionMdType, preferedMdType));	
				
				// 3. Compare Type
				if ( chosenDmdId == null && dmdSectionMdType.equalsIgnoreCase( preferedMdType ) ) { // Will only choose the first prefered found
					
					// 4. Use this ID if Type matches prefered type.
					chosenDmdId = dmdIdOption;
					
				} else if ( defaultDmdId == null && dmdSectionMdType.equalsIgnoreCase( MetsConstant.ATTR_DMDSEC_MDTYPE_MODS ) ) {
					// Find a default
					defaultDmdId = dmdIdOption;
				}

			} // end for
			
			// Use default if nothing found
			if ( chosenDmdId == null ) {
				chosenDmdId = defaultDmdId;
			}
			
			//System.out.println("CHOSEN: " + chosenDmdId);
			
			return chosenDmdId;
			
		} else {
			return dmdId;
		}
	}
	
	private static void controlBuilderQuality(ArticleDocumentBuilder builder) {
		testString(builder.getArk(), "ARK");
		testString(builder.getDmdId(), "DMDID");
		testString(builder.getRecordIdentifier(), "RecordIdentifier");
		testString(builder.getDate(), "Date");
		testStringList(builder.getIsPartOfs(), "IsPartOf");
		testString(builder.getPublisher(), "Publisher");
		//testString(builder.getText(), "Text");
		//testString(builder.getLineText(), "Line Text");
		//testString(builder.getWordText(), "Word Text");
		testString(builder.getTitle(), "Title");
		testString(builder.getAlternative(), "Alternative");
		//testStringList(builder.getCreators(), "Creator");
		//testStringList(builder.getLanguages(), "Language");
		testString(builder.getType(), "Type");
	}

	private static void testString(String data, String label) {
		if ( StringUtils.isBlank(data) ) {
			logger.error(String.format("Missing %s , Value: %s", label, data));
		}
	}

	private static void testStringList(List<String> items, String label) {
		if (items.size() == 0) {
			logger.error(String.format("Missing %s , Empty List", label));
		}
	}

}
