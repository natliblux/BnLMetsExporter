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
package lu.bnl.domain.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.neovisionaries.i18n.LanguageCode;

import lu.bnl.domain.constants.MetsConstant;
import lu.bnl.domain.model.marc.DataField;
import lu.bnl.domain.model.mods.Author;

public class DmdSection {

	private String id;
	
	/** MDTYPE of the DmdSection. Is most often MODS or MARC.
	 */
	public String mdType;
	
	public Set<String> languages = new HashSet<>();
	
	public List<Author> authors = new ArrayList<>();
	
	public List<Pair> titleInfoParts = new ArrayList<Pair>();
	
	public List<String> titles = new ArrayList<String>(); 
	
	public List<String> records = new ArrayList<String>();
	
	// Example: <controlfield tag="001">001094930:LUX01</controlfield>
	public Map<String, String> controlFields = new HashMap<>();
	
	// <datafield ind1=" " ind2=" " tag="040">
	//     <subfield code="a">LUX BINA</subfield>
	//     <subfield code="b">fre</subfield>
	// </datafield>
	// PROBLEM:
	// EXAMPLE: Bulletin / Société des naturalistes luxembourgeois Luxembourg 110(2009), p. 7-41, ill. ; Bulletin / Soci�t� des naturalistes luxembourgeois Luxembourg 111(2010), p. 3-9, ill.
	// EXAMPLE ISPARTOF: String1 ; String 2 ; ...
	// ADD methods to easily add to existing list or initialize list
	private Map<String, List<DataField>> dataFieldsMap = new HashMap<>();
	
	
	private String identifierLocal;
	
	private String identifierBarcode;
	
	private String recordIdentifer;
	
	private String printer;
	
	private String publisher;
	
	private String dateIssued; 
	
	
	public DmdSection(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	/** Add a DataField object to a Map of list of DataFields.
	 *  The key of the map is the TAG attribute of the DataField
	 *  as defined in the MARC schema.
	 * 
	 * @param dataField The DataField to store in the list for the 
	 * 		  key TAG of the DataField.
	 */
	public void addDataField(DataField dataField) {
		// Initialize List for DataFields with the same same
		if ( !this.dataFieldsMap.containsKey(dataField.tag) ) {
			this.dataFieldsMap.put(dataField.tag, new ArrayList<>());
		}
		
		this.dataFieldsMap.get(dataField.tag).add(dataField);
	}
	
	//================================================================================
	//================================================================================
	
	/**
	 * Returns a string representing the creator based on the
	 * NamePart (mods:namePart) objects.
	 * The result will a string such as "family, given" e.g "Doe, John".
	 * 
	 * If more NameParts of type family or given are present, then all family,
	 * respectively given values, are joined with the implicit order and separated 
	 * with single spaces.
	 * 
	 * Other types of Namepart are ignored.
	 * 
	 * @return
	 */
	public List<String> getCreators() {
		if ( MetsConstant.ATTR_DMDSEC_MDTYPE_MARC.equalsIgnoreCase(this.mdType) ) {
			return getCreatorsFromMarc();
		}
		
		List<String> creators = new ArrayList<>();
		for (Author author : this.authors) {
			creators.add( author.getCreator() );
		}
		
		return creators;
	}
	
	public List<String> getCreatorsFromMarc() {
		List<String> creators = new ArrayList<>();
		
		List<DataField> dataFields = this.dataFieldsMap.get( MetsConstant.ATTR_MARC_TAG_CREATOR_100 );
		
		if (dataFields != null && !dataFields.isEmpty()) {
			for (DataField df :  dataFields) {
				String creator = "";
			
				if ( df.containsKey("a") ) {
					String value = df.get("a").replaceAll(";", " ");
					creator += value;
				}
				
				if ( df.containsKey("b") ) {
					creator += df.get("b");
					creator = creator.trim();
				}
				
				if ( df.containsKey("q") ) {
					String value = df.get("q").replaceAll(";", " ");
					creator += " (" + value + ")";
				}
				
				if ( df.containsKey("c") ) {
					String value = df.get("c").replaceAll(";", " ");
					creator += ", " + value;
				}	
				
				if ( df.containsKey("d") ) {
					String value = df.get("d").replaceAll(";", " ");
					creator += ", " + value;
				}
				
				creator = creator.trim();
				
				creators.add( creator );
			}
		}
		
		return creators;
	}

	//================================================================================
	//================================================================================
	
	/**
	 * Returns a string representing the title of the Dublin Core export.
	 * The string is the concatenation of mods:nonsort, mods:title and mods:subtitle.
	 * 
	 * 
	 * 
	 * @param dmdSection
	 * @return
	 */
	public String getTitle() {
		if ( MetsConstant.ATTR_DMDSEC_MDTYPE_MARC.equalsIgnoreCase(this.mdType) ) {
			return getTitleFromMarc();
		}
		
		String nonSort = "";
		String title = "";
		String subTitle = "";
		String partNumber = "";
		String partName = "";
		
		for (Pair pair : this.titleInfoParts) {
			
			if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_NONSORT ) ) {
				nonSort = pair.value;
			} else if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_TITLE ) ) {
				// Only keep the shortest title as the title. Important for illustrations.
				if ( title.length() == 0 || title.length() > pair.value.length() ) {
					title = pair.value;
				}
			} else if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_SUBTITLE ) ) {
				subTitle = pair.value;
			} else if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_PARTNUMBER ) ) {
				partNumber = pair.value;
			} else if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_PARTNAME ) ) {
				partName = pair.value;
			}

		}
		
		String result = "";
		
		String[] tokens = {nonSort, title, subTitle, partNumber, partName};
		
		for (int i = 0; i < tokens.length; i++) {
			if (result.length() > 0 && tokens[i].length() > 0) {
				result += " ";
			}
			result += tokens[i];
		}
		
		return result;
	}

	/**
	 * Returns the part number of the issue.
	 * 
	 * @return
	 */
	public String getPartNumber() {
		
		String partNumber = null;

		for (Pair pair : this.titleInfoParts) {
			//System.out.println(String.format("Pair = %s : %s", pair.key, pair.value)); // DEBUG
			if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_PARTNUMBER ) ) {
				partNumber = pair.value;
			}
		}

		return partNumber;
	}
	
	public String getTitleFromMarc() {
		List<String> values = getSubFieldsValueForMarc(MetsConstant.ATTR_MARC_TAG_TITLE_245, 
				new String[]{"a", "b", "d", "i", "j", "n", "p"});
		
		if (values.size() > 0) {
			int last = values.size() - 1;
			values.set( last , values.get(last).replaceAll("/:;=", "") );
		}
		
		String result = String.join(" ", values);
		
		return result;
	}
	
	//================================================================================
	//================================================================================
	
	/**
	 * Returns a string representing the <dcterms:isPartOf> of the Dublin Core export.
	 * The string is the concatenation of mods:nonsort and mods:title.
	 * Note that mods:nonsort is optional.
	 * 
	 * Limitation: MODS describes each mods:title, mods:nonSort as unbounded.
	 * So, it might be better to assume that there can be 0..n mods:title or mods:title 
	 * for example. Right now, the last mods:nonSortmods:title or mods:title will be kept
	 * 
	 * @return
	 */
	public List<String> getIsPartOfs() {
		if ( MetsConstant.ATTR_DMDSEC_MDTYPE_MARC.equalsIgnoreCase(this.mdType) ) {
			return getIsPartOfsFromMarc();
		}
		
		String nonSort = "";
		String title = "";
		
		for (Pair pair : this.titleInfoParts) {
			
			if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_NONSORT ) ) {
				nonSort = pair.value;
			} else if ( pair.key.equalsIgnoreCase( MetsConstant.TAG_MODS_TITLE ) ) {
				title = pair.value;
			}
		
		}
		
		String spacer = (nonSort.length() > 0) ? " " : "";
		
		String isPartOf = String.format("%s%s%s", nonSort, spacer, title).trim(); // Also remove leading and trailing spaces
		
		// TEMPORARY, becuase unclear how to separate TITLEINFO Tags
		List<String> result = new ArrayList<>();
		result.add(isPartOf);
		
		return result;
	}

	
	/** Creates the isPartOf text by extracting MARC21 information.
	 *  If multiples tags 773 are found, then each text is concatenated
	 *  with a " ; " as delimiter.
	 * 
	 * @return
	 */
	public List<String> getIsPartOfsFromMarc() {
		List<String> isPartOfs = new ArrayList<>();
		
		List<DataField> dataFields = this.dataFieldsMap.get( "773" );
		if (dataFields != null && !dataFields.isEmpty()) {
			for (DataField df : dataFields) {
				List<String> parts = new ArrayList<>();
				List<String> excluded = new ArrayList<>( Arrays.asList("x", "y", "z", "w", "j") );
				
				for (String key : df.keySet()) {
					if ( ! excluded.contains(key) ) {
						parts.add( df.get(key) );
					}
				}
				
				isPartOfs.add( String.join(", ", parts) );
			}
		}
		
		return isPartOfs;
	}
	
	//================================================================================
	//================================================================================
	
	public String getDateFromMarc() {
		String date = null;
		
		List<DataField> dataFields = this.dataFieldsMap.get( MetsConstant.ATTR_MARC_TAG_DATE_260 );
		if (dataFields != null && !dataFields.isEmpty()) {
			// TODO: SUPPORT LOOP
			DataField df = dataFields.get(0);
						
			if (df.containsKey("c")) {
				String value = df.get("c");
				if (value.startsWith("[")) {
					value = value.replaceFirst("\\[", "");
				}
				
				// TODO: OR REGEX [\\]:,=/.]$
				if (value.endsWith("]") || value.endsWith(":") || value.endsWith(",") || value.endsWith("=") || value.endsWith("/") || value.endsWith(".")) {
					value = value.substring(0, value.length() - 2);
				}
				
				date = value;
			}
		}
		
		if (date == null) {
			String controlField008 = this.controlFields.get("008");
			if (controlField008 != null) {
				Pattern r = Pattern.compile(".{6}([0123456789].{3})");
				Matcher m = r.matcher(controlField008);
				if (m.find()) {
					if (m.groupCount() > 0) {
						// Example: Group 0 = 80502s2010, Group 1 = 2010
						date = m.group(1);
					}
				}
			}
		}
		
		
		return date;
	}
	
	//================================================================================
	//================================================================================

	
	// Probably depreciated, because Publisher is taken from MODSMD_PRINT by default
	public String getPublisherFromMarc() {
		List<String> publishers = new ArrayList<>();
		
		// 260, a,b
		List<DataField> dataFields = this.dataFieldsMap.get( MetsConstant.ATTR_MARC_TAG_DATE_260 );
		if (dataFields != null && !dataFields.isEmpty()) {
			// TODO: SUPPORT LOOP
			DataField df = dataFields.get(0);
			String p = this.getTransformedAB(df);
			if (p.length() > 0){
				publishers.add( p );
			}
		}
		
		// 502, a
		dataFields = this.dataFieldsMap.get( "502" );
		if (dataFields != null && !dataFields.isEmpty()) {
			// TODO: SUPPORT LOOP
			DataField df = dataFields.get(0);
			
			if (df.containsKey("a")) {
				publishers.add(df.get("a"));
			}
			
		}
		
		// 880, a, b
		dataFields = this.dataFieldsMap.get( "880" );
		if (dataFields != null && !dataFields.isEmpty()) {
			// TODO: SUPPORT LOOP
			DataField df = dataFields.get(0);
			
			if (df.containsKey("6")) {
				
				if (df.get("6").equalsIgnoreCase("502")) {
					
					if (df.containsKey("a")) {
						publishers.add(df.get("a"));
					}
				}
				
				if (df.get("6").equalsIgnoreCase("260")) {
					String p = this.getTransformedAB(df);
					if (p.length() > 0){
						publishers.add( p );
					}
				}
			}

		}
		
		return String.join(", ", publishers);
	}
	
	//================================================================================
	//================================================================================
	
	public List<String> getLanguages() {
		if ( MetsConstant.ATTR_DMDSEC_MDTYPE_MARC.equalsIgnoreCase(this.mdType) ) {
			return this.getLanguagesFromMarc();
		}

		return this.languages.stream()
				.map(lang -> getAlpha2LanguageCode(lang))
				.collect(Collectors.toList());
	}
	
	/** Return a list of unique languages for this DmdSection
	 * 
	 * @return List of Strings where each String is an ISO 639-1 Alpha2 Language code.
	 */
	public List<String> getLanguagesFromMarc() {
		List<String> alpha2languages = new ArrayList<>();
		
		// TAKE 35@@3, ([azAZ]{3}), zxx
		String string008 = this.controlFields.get( "008" );
		if (string008 != null) {
			Pattern r = Pattern.compile(".{35}([a-z]{3})");
			Matcher m = r.matcher(string008);
			if (m.find()) {
				if (m.groupCount() > 0) {
					alpha2languages.add( this.getAlpha2LanguageCode( m.group(1) ) ); 
				}
			}
		}
		
		List<DataField> dataFields = this.dataFieldsMap.get( "041" );
		if (dataFields != null && !dataFields.isEmpty()) {
			for (DataField df : dataFields) {
		
				ArrayList<String> subfields = new ArrayList<>(Arrays.asList( "a", "d", "e" ));
				for (String s : subfields){
					if (df.containsKey(s)) { 
						Pattern r = Pattern.compile("[a-zA-Z]{3}");
						Matcher m = r.matcher( df.get(s) );
						if (m.find()) {
							for (int i = 0; i < m.groupCount(); i++) {
								String code = m.group(i);
								alpha2languages.add( this.getAlpha2LanguageCode(code) );
							}
							
						}
					}
				}
				
			} // end for dataFields
		
		}
		
		return alpha2languages;
	}
	
	
	
	//================================================================================
	//================================================================================	
	
	public String getAlephSystemNumber() {
		String alephSystemNumber = null;

		String controlField001 = this.controlFields.get( MetsConstant.ATTR_CONTROLFIELD_TAG_001 );
		
		// Example: <controlfield tag="001">001338587:LUX01</controlfield>
		if (controlField001 != null) {
	
			String[] parts = controlField001.split(":");
			if ( parts.length == 2 ) {
				String number = parts[0]; // This is the actual system number.
				String datasource = parts[1]; // This is actually the PRIMO identifier.
				
				alephSystemNumber = String.format("ALEPH_%s%s", datasource, number);
			}
			
		}
		
		return alephSystemNumber;
	}

	private List<String> getSubFieldsValueForMarc(String datafieldTag, String[] codes) {
		List<String> subfields = new ArrayList<>( Arrays.asList( codes ) );
		
		List<String> values = new ArrayList<>();
		
		List<DataField> dataFields = this.dataFieldsMap.get( datafieldTag );
		if (dataFields != null && !dataFields.isEmpty()) {
			// TODO: SUPPORT LOOP
			DataField df = dataFields.get(0);
		
			subfields.forEach(tag -> {
				if ( df.containsKey(tag) ) {
					values.add( df.get(tag) );
				}
			});
		}
		
		return values;
	}
	
	/** Converts a 3-letter language code (ISO 639-2 /T or /B) into
	 *  a 2-letter language code (ISO 639-1).
	 *  Relevant documentation:
	 *    - https://github.com/TakahikoKawasaki/nv-i18n
	 *    - https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
	 * 
	 * @param iso3LanguageCode
	 * @return
	 */
	private String getAlpha2LanguageCode(String iso3LanguageCode) {
		LanguageCode languageCodeAlpha3T = LanguageCode.getByCode(iso3LanguageCode);
		if (languageCodeAlpha3T != null) {
			return languageCodeAlpha3T.getAlpha3().getAlpha2().toString();
		}
		
		return null;
	}
	
	private String getTransformedAB(DataField df) {
		String publisher = "";
		if ( df.containsKey("a") ) { publisher += df.get("a"); }
		if ( df.containsKey("b") ) { publisher += " : " + df.get("b"); }
		
		publisher = publisher.replaceAll("\\[", "");
		publisher = publisher.replaceAll("\\]", "");
		publisher = publisher.replaceAll("\\(", "");
		publisher = publisher.replaceAll("\\)", "");
		
		if ( publisher.endsWith(",") || publisher.endsWith(":") || publisher.endsWith("=") || publisher.endsWith(";")) {
			publisher = publisher.substring(0, publisher.length() - 2);
		}
		
		return publisher;
	}
	
	
	//================================================================================
	//================================================================================	
	
	/** Return the local itentifier, otherwise the barcode otherwise null.
	 * 
	 * @return String or null
	 */
	public String getIdentifier() {
		if (this.identifierLocal != null) {
			return this.identifierLocal;
		}
		
		if (this.identifierBarcode != null) {
			return this.identifierBarcode;
		}
		
		return null;
	}
	
	public String getIdentifierLocal() {
		return identifierLocal;
	}

	public void setIdentifierLocal(String identifierLocal) {
		this.identifierLocal = identifierLocal;
	}

	public String getIdentifierBarcode() {
		return identifierBarcode;
	}

	public void setIdentifierBarcode(String identifierBarcode) {
		this.identifierBarcode = identifierBarcode;
	}

	public String getPrinter() {
		return printer;
	}

	public void setPrinter(String printer) {
		this.printer = printer;
	}

	public String getDateIssued() {
		if ( MetsConstant.ATTR_DMDSEC_MDTYPE_MARC.equalsIgnoreCase(this.mdType) ) {
			return getDateFromMarc();
		}
		
		return dateIssued;
	}

	public void setDateIssued(String dateIssued) {
		this.dateIssued = dateIssued;
	}

	public String getRecordIdentifer() {
		return this.recordIdentifer;
	}

	public void setRecordIdentifer(String recordIdentifer) {
		this.recordIdentifer = recordIdentifer;
	}

	public String getPublisher() {
		if ( MetsConstant.ATTR_DMDSEC_MDTYPE_MARC.equalsIgnoreCase(this.mdType) ) {
			return this.getPublisherFromMarc();
		}
		
		return this.publisher;
	}
	
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	//================================================================================
	//================================================================================
	
}
