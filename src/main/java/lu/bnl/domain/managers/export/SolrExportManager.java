/*******************************************************************************
 * Copyright (C) 2017-2021 Bibliothèque nationale de Luxembourg (BnL)
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.configuration.ExportConfig;
import lu.bnl.configuration.SolrServerConfig;
import lu.bnl.domain.managers.StatisticsManager;
import lu.bnl.domain.managers.solr.SolrManager;
import lu.bnl.domain.model.Alto;
import lu.bnl.domain.model.AltoWord;
import lu.bnl.domain.model.ArticleDocumentBuilder;
import lu.bnl.domain.model.DivSection;
import lu.bnl.domain.model.DocList;
import lu.bnl.domain.model.marc.MarcControlFieldDTO;
import lu.bnl.domain.model.marc.MarcDataFieldDTO;
import lu.bnl.domain.model.marc.MarcRecordDTO;
import lu.bnl.domain.model.marc.MarcSubFieldDTO;
import lu.bnl.reader.MetsAltoReaderManager;
import lu.bnl.reader.MetsGetter;
import lu.bnl.xml.AltoXMLParserHandler;
import lu.bnl.xml.MetsXMLParserHandler;

/**
 * This class sole purpose is to manage the export of documents.
 * to Solr.
 * 
 */
public class SolrExportManager extends ExportManager {

	private static final Logger logger = LoggerFactory.getLogger(SolrExportManager.class);
	
	/** Number of Document IDs to process before a hard commit.
	 */
	private static final int INTERMEDIATE_COMMIT_COUNT = 100;

	private MetsGetter metsGetter;
	
	private String dir;
	
	private boolean useCloud;
	
	private boolean parallel;
	
	private SolrManager articleSolrManager;
	
	private SolrManager pageSolrManager;

	private static final String UNKNOWN = "unknown";
	
	public SolrExportManager(String dir, boolean useCloud, boolean parallel) {
		this.dir = dir;
		this.useCloud = useCloud;
		this.parallel = parallel;
		
		this.createSolrManagers();
	}

	private void createSolrManagers() {
		
		// TODO: check if solr config is okay in BnLMetsExporter
		SolrServerConfig solrConfig = AppConfigurationManager.getInstance().getExportConfig().solr;
		
		if (!useCloud) {
			this.articleSolrManager = new SolrManager(solrConfig.articleURL, solrConfig.articleCollection, useCloud);
			this.pageSolrManager 	= new SolrManager(solrConfig.pageURL, solrConfig.pageCollection, useCloud);
		} else {
			this.articleSolrManager = new SolrManager(solrConfig.zkHost, solrConfig.articleCollection, useCloud);
			this.pageSolrManager 	= new SolrManager(solrConfig.zkHost, solrConfig.pageCollection, useCloud);
		}
	}
	
	@Override
	public void run(MetsGetter metsGetter) {
		
		this.metsGetter = metsGetter;
	
		console("Starting Solr Export.");
		
		this.startGlobal = this.getExecutionTimeTracker().start();
		this.count = metsGetter.getMetsData().size();
		
		StatisticsManager.getInstance().countFiles(count);
		
		logger.info(String.format("%d Mets Document IDs have been found.", this.count));

		this.connectAll();
		
		if (this.parallel) {
			console("Mode: Parallel");
			this.exportParallel();
		} else {
			console("Mode: Sequential");
			this.export(metsGetter.getMetsData());
		}
		
		this.closeAll();
		
		this.printStats();
	}
	
	//================================================================================
	
	private void connectAll() {
		this.articleSolrManager.connect();
		this.pageSolrManager.connect();
	}
	
	private boolean closeAll() {
		boolean articleOk = this.articleSolrManager.close();
		boolean pageOk = this.pageSolrManager.close();
		
		return articleOk && pageOk;
	}

	private boolean commitAll() {
		boolean articleOk = this.articleSolrManager.commit();
		boolean pageOk = this.pageSolrManager.commit();
		
		return articleOk && pageOk;
	}
	
	//================================================================================
	
	private void export(List<String> metsData) {
		ExportConfig ec = AppConfigurationManager.getInstance().getExportConfig();
		List<String> supportedMetsTypes = ec.exportSolr;
		
		// Use i as internal count to commit periodically
		int i = 1;
		
		for (String metsAddress : metsData) {
			
			String documentID 	= this.metsGetter.getUniqueName(metsAddress); // Note: document ID must be processed differently for local
			String path 		= this.metsGetter.getMetsLocation(metsAddress);

			String dir = this.dir;
			
			// If the location of the METS is defined by the metsGetter then use it, otherwise use the argument dir
			// LocalMetsGetterImpl found a METS at path/to/1-mets.xml and so the relative alto paths are path/to/
			// RemoteMetsGetterImpl gets METS from server and so does not have a local repository. There the dir parameter is
			// used to find alto files directly.
			if (path != null) {
				dir = path;
			}

			String content = metsGetter.getMetsContent(metsAddress, path);
			
			MetsXMLParserHandler metsHandler = MetsAltoReaderManager.parseMets(documentID, content, dir, false, supportedMetsTypes);
			
			if (metsHandler.isSupported()) {
			
				DocList docList = this.handleMetsResult(metsHandler, documentID, false);
				
				if ( this.articleSolrManager.add( docList.articles ) ) {
					// Count statistics if successful
					StatisticsManager.getInstance().countArticles( docList.articles.size() );
					StatisticsManager.getInstance().countPages( docList.pages );
				}
				
				for (List<SolrInputDocument> documents : docList.words.values()) {
					StatisticsManager.getInstance().countWords(documents.size());
					
					//this.wordSolrManager.add( documents );
				}
				
				
				if (this.pageSolrManager.add( docList.altoPages )) {
					// DO NOTHING, ALREADY COUNTED PAGES ON TOP
				}
				
			} else {
				logger.info(String.format("METS with Document ID %s is not supported.", documentID));
				StatisticsManager.getInstance().countCustom("Not Supported", 1);
			}
			
			StatisticsManager.getInstance().countCustom("Processed", 1);

			getExecutionTimeTracker().stopETA(startGlobal, "[X] ", StatisticsManager.getInstance().getCustomValue("Processed"), count);
			
			i++;
			
			// Commit every X Document IDs
			// Actually never do that! Use auto commit maybe
			if (i % INTERMEDIATE_COMMIT_COUNT == 0) {
				this.commitAll();
			}
			
		}
		
		// Commit as the end one last time to be sure
		this.commitAll(); 
	}
	
	private void exportParallel() {
		
		// Get the optimal number of cores
		// Do not use more cores than we have Document IDs (overkill)
		int cores = this.getReasonableNumberOfAvailableCores( this.metsGetter.getMetsData().size() );
		
		// Partition list of Document IDs for each core
		List<List<String>> partitions = this.getPartitions(this.metsGetter.getMetsData(), cores);
		
		console("Number of partitions: " + partitions.size());

		List<Thread> threads = new ArrayList<>();
		
		for (int i = 0; i < partitions.size(); i++) {

			console(String.format("Starting Thread %d", i));
			
			List<String> partition = partitions.get(i);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					export(partition);
				}
			});
			
			threads.add(t);
			
			t.start();
		}
		
		// Follow and wait for finishing
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				logger.error("Thread InterruptedException.", e);
				e.printStackTrace();
			}
		}
		
	}
	
	private DocList handleMetsResult(MetsXMLParserHandler handler, String documentID, boolean useTokenizer) {
		DocList docList = new DocList();
	
		try { // If anything fails, report the error. Mostly it can ba a corrupt file.
			
			Map<String, DivSection> articles = handler.getArticles();
			List<Alto> pages = handler.getPages();
			
			Map<String, List<String>> articleAltoBlockMap = handler.getArticleAltoBlockMap();
			
			logger.info( String.format("Pages    : %d", pages.size()) );
			logger.info( String.format("Articles : %d", articles.size()) );
			
			docList.pages += pages.size();
			
			for (Alto page : pages) {
				AltoXMLParserHandler handlerAlto = MetsAltoReaderManager.parseAlto(page, articleAltoBlockMap, articles, useTokenizer);
	
				// Handle Words
				this.createDocWordsAlto(documentID, page, handlerAlto, docList);
	
				// Handle Page
				this.createDocPages(documentID, page, handlerAlto, docList);
			}
			
			// Handle Article
			for (DivSection divSection : articles.values()) {
				if (  !divSection.getText().isEmpty() ) {
					SolrInputDocument solrDocument = this.createDocArticle(documentID, divSection, handler);
					
					// Add the lines for this article
					
					docList.articles.add(solrDocument);
				} else {
					logger.info(String.format("Skipped SolrDocument %s, %s, %s", documentID, divSection.getDmdid(), divSection.getId()));
					
					StatisticsManager.getInstance().countSkipArticles(1);
				}
			}
		
		} catch (Exception e) {
			logger.error(String.format("Mets %s failed to be parsed.", documentID), e);
			StatisticsManager.getInstance().countCustom("Mets Fail", 1);
		}
		
		return docList;
	}
	
	private void createDocPages(String documentID, Alto page, AltoXMLParserHandler handlerAlto, DocList docList) {
		String pageId = page.getFileid();
		
		SolrInputDocument doc = new SolrInputDocument();
		
		String routePrefix = String.format("%s!", documentID);
		
		doc.addField("id", 		String.format("%s%s_%s", routePrefix, documentID, pageId));
		doc.addField("pid", 	documentID);
		doc.addField("page", 	pageId);
		
		addIfNotEmpty(doc, "text_lines",	handlerAlto.getPageFullTextLines());
		addIfNotEmpty(doc, "text_words",	this.createTextWordData(documentID, page, handlerAlto));
		
		docList.altoPages.add(doc);
	}
	
	
	
	private void createDocWordsAlto(String documentID, Alto page, AltoXMLParserHandler handlerAlto, DocList docList) {
		String pageId = page.getFileid();
		
		// Index of words containing the coordinates per page/alto
		List<AltoWord> pageWords = handlerAlto.getPageWords();
		List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		for (AltoWord altoWord : pageWords) {
			SolrInputDocument doc = new SolrInputDocument();
			
			String routePrefix = String.format("%s!", documentID);
			
			if ( altoWord.getId() != null ) {
				doc.addField("id", String.format("%s%s_%s_%s", 
						routePrefix, documentID, pageId, altoWord.getId()));
			} else {
				// Use coordinates as unique key, because if manual tokenizer, we can have multiple token with same id.
				doc.addField("id", String.format("%s%s_%s_%d_%d_%d_%d", 
						routePrefix, documentID, pageId, altoWord.getX(), altoWord.getY(), altoWord.getW(), altoWord.getH())); 
			}

			doc.addField("pid", documentID);
			doc.addField("article", altoWord.getArticle());
			
			// The blockId is required to locate the word in a block
			doc.addField("begin", 	altoWord.getBlockId() );
			doc.addField("page", 	pageId);
			
			doc.addField("word", altoWord.getText());
			
			doc.addField("x", altoWord.getX());
			doc.addField("y", altoWord.getY());
			doc.addField("w", altoWord.getW());
			doc.addField("h", altoWord.getH());
			
			//doc.addField("count", altoWord.getText().length()); // For debug and statistics
			
			// Optional Content (NOT USED)
			/*if (altoWord.getContent()!=null){
				doc.addField("content", altoWord.getContent());
			}*/
			
			docs.add(doc);
		}
		
		docList.words.put(pageId, docs);
	}
	
	/**
	 * Concatenate all Alto Words, with their coordinates and metadata, into 
	 * a string. Each Alto Word is stored as an HTML tag such as:
	 * <w [attributes]>ALTOWORD<\/w>
	 * Where attributes are HTML attributes such as article id, block id
	 * and x, y, w, h coordinates. 
	 * 
	 * @param documentID
	 * @param page
	 * @param handlerAlto
	 * @return
	 */
	private String createTextWordData(String documentID, Alto page, AltoXMLParserHandler handlerAlto) {
		StringBuilder sb = new StringBuilder();
		
		List<AltoWord> pageWords = handlerAlto.getPageWords();
		for (AltoWord altoWord : pageWords) {
			sb.append( altoWord.toHTML(false) );
		}
		
		return sb.toString();
	}

	
	private SolrInputDocument createDocArticle(String documentID , DivSection article, MetsXMLParserHandler handler) {
		SolrInputDocument doc = new SolrInputDocument();
		
		ArticleDocumentBuilder builder = ExportManager.getArticleDocumentBuilder(documentID, article, handler);

		String routePrefix = String.format("%s!", builder.getDocumentID());
		
		doc.addField("id", 					String.format("%s%s_%s_%s", routePrefix, builder.getDocumentID(), builder.getDmdId(), builder.getId()));
		
		doc.addField("article", 			builder.getDmdId());
		doc.addField("pid", 				builder.getDocumentID());
		doc.addField("begin", 				builder.getId());
		doc.addField("source", 				builder.getRecordIdentifier());
		
		addIfNotEmpty(doc, "title",   		builder.getTitle());
		addIfNotEmpty(doc, "text_words",	builder.getWordText());
		addIfNotEmpty(doc, "text_lines",	builder.getLineText());
		
		addIfNotEmpty(doc, "date", 			builder.getDate());
		addIfNotEmpty(doc, "publisher", 	builder.getPublisher());
		addIfNotEmpty(doc, "type", 			builder.getType());
		
		doc.addField("creators", 			builder.getCreators());
		doc.addField("lang",    			builder.getLanguages());
		doc.addField("ispartofs",  			builder.getIsPartOfs());

		// New Fields

		doc.addField("document_type", 		builder.getDocumentType().toLowerCase());
		addIfNotEmpty(doc, "collection", 	builder.getPaperId());
	
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date dateObject = formatter.parse(builder.getDate());
			
			// If the above worked, we can safely create the Solr date
			doc.addField("date_document", formatter.format(dateObject) + "T00:00:00.000Z");
		} catch (ParseException e) {
			logger.error(String.format("Document '%s' Failed to convert date '%s' for SolrIndex ", builder.getDocumentID(), builder.getDate()), e);
		}

		// Mode

		doc.addField("mode_viewer", builder.isModeViewer());
		doc.addField("mode_search", builder.isModeSearch());

		// MARC21 Fields
		/** What will be indexed
		 * 	- OK Control Field 			001
		 * 	- OK Author 				100 $a : $b / $c
		 *  - OK Title 					245 $a : $b / $c
		 *  - OK BibliographicAddress 	260 $a : $b, $c
		 *  - OK PhysicalDescription 	300 $a : $b
		 *  - OK Fond 					852 $b - $d
		 *  - OK Archives 				949 $0 - $j
		 *  - OK CatalogueLink 			DRL $a
		 */

		MarcRecordDTO marcRecord = handler.getMarcRecord();
		if (marcRecord != null) {

		
			String controlField_001 = null;
			for (MarcControlFieldDTO controlField : marcRecord.getControlFields()) {
				if (controlField.getTag().equalsIgnoreCase("001")) {
					controlField_001 = controlField.getContent();
					doc.addField("MARC21_controlfield_001", controlField_001);
				}
			} // end for MarcControlFieldDTO

			for (MarcDataFieldDTO dataField : marcRecord.getDataFields()) {

				System.out.println(dataField.getTag());

				if (dataField.getTag().equalsIgnoreCase("100")) {
					// Author 				100 $a : $b / $c
					Map<String, List<String>> mapping = createMapping("a", UNKNOWN);
					processMarcField(dataField, mapping);

					String text = String.join(" ", new String[]{
						String.join(" ", mapping.get("a")),
						String.join(" ", mapping.get(UNKNOWN))
					});

					doc.addField("MARC21_datafield_100", text);
				}

				if (dataField.getTag().equalsIgnoreCase("245")) {
					// Title 				245 $a : $b / $c
					Map<String, List<String>> mapping = createMapping("a", "b", "c", UNKNOWN);
					processMarcField(dataField, mapping);

					String text = String.join(" ", new String[]{
						String.join(" ", mapping.get("a")),
						mapping.get("b").size() > 0 ? String.format(": %s", String.join(" ", mapping.get("b"))) : "",
						mapping.get("c").size() > 0 ? String.format("/ %s", String.join(" ", mapping.get("c"))) : "",
					});

					doc.addField("MARC21_datafield_245", text);
				}

				if (dataField.getTag().equalsIgnoreCase("260")) {
					// BibliographicAddress 	260 $a : $b, $c
					Map<String, List<String>> mapping = createMapping("a", "b", "c", UNKNOWN);
					processMarcField(dataField, mapping);

					String text = String.join(" ", new String[]{
						String.join(" ", mapping.get("a")),
						mapping.get("b").size() > 0 ? String.format(": %s", String.join(" ", mapping.get("b"))) : "",
						mapping.get("c").size() > 0 ? String.format(", %s", String.join(" ", mapping.get("c"))) : "",
					});

					doc.addField("MARC21_datafield_260", text);
				}

				if (dataField.getTag().equalsIgnoreCase("300")) {
					// PhysicalDescription 	300 $a : $b
					Map<String, List<String>> mapping = createMapping("a", "b", UNKNOWN);
					processMarcField(dataField, mapping);

					String text = String.join(" ", new String[]{
						String.join(" ", mapping.get("a")),
						mapping.get("b").size() > 0 ? String.format(": %s", String.join(" ", mapping.get("b"))) : ""
					});

					doc.addField("MARC21_datafield_300", text);
				}

				// NO NEED TO INDEX THOSE FIELDS
				/*if (dataField.getTag().equalsIgnoreCase("852")) {
					// Fond 					852 $b - $d
					Map<String, List<String>> mapping = createMapping("b", "d", UNKNOWN);
					processMarcField(dataField, mapping);

					String text = String.join(" ", new String[]{
						String.join(" ", mapping.get("b")),
						mapping.get("d").size() > 0 ? String.format("- %s", String.join(" ", mapping.get("d"))) : ""
					});

					doc.addField("MARC21_datafield_852", text);
				}*/

				/*if (dataField.getTag().equalsIgnoreCase("949")) {
					// Archives 				949 $0 - $j
					Map<String, List<String>> mapping = createMapping("0", "j", UNKNOWN);
					processMarcField(dataField, mapping);

					String text = String.join(" ", new String[]{
						String.join(" ", mapping.get("0")),
						mapping.get("j").size() > 0 ? String.format("- %s", String.join(" ", mapping.get("j"))) : ""
					});

					doc.addField("MARC21_datafield_300", text);
				}*/

				/*if (dataField.getTag().equalsIgnoreCase("DRL") && controlField_001 != null) {
					// CatalogueLink 		DRL $a
					Map<String, List<String>> mapping = createMapping("a", UNKNOWN);
					processMarcField(dataField, mapping);

					String text = String.join(" ", new String[]{
						String.join(" ", mapping.get("a"))
					});

					doc.addField("MARC21_datafield_DRL", text);
				}*/
				
			} // end for MarcDataFieldDTO
		} // end marcRecord no null

		//doc.addField("reference", "issue:"+issue_title+"/article:"+dmdid);
		
		return doc;
	}

	private void addIfNotEmpty(SolrInputDocument doc, String key, String value) {
		if (!StringUtils.isEmpty(value)) {
			doc.addField(key, value);
		}
	}

	private Map<String, List<String>> createMapping(String... strings) {
		HashMap<String, List<String>> mapping = new HashMap<>();
		for (int i = 0; i < strings.length; i++) {
			mapping.put(strings[i], new ArrayList<String>());
			System.out.println("Putting " + strings[i]);
		}
		return mapping;
	}

	private void processMarcField(MarcDataFieldDTO dataField, Map<String, List<String>> mapping) {
		for (MarcSubFieldDTO subField : dataField.getSubFields()) {
			String key = subField.getCode();
			if (mapping.containsKey(key)) {
				mapping.get(key).add(subField.getContent());
			} else {
				mapping.get(UNKNOWN).add(subField.getContent());
			}
		}
	}

}
