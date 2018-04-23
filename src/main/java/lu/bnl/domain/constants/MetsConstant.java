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
package lu.bnl.domain.constants;

public class MetsConstant {

	// Mets Types
	//================================================================================
	
	public static final String METS_TYPE_NEWSPAPER 	 = "Newspaper";
	
	public static final String METS_TYPE_SERIAL		 = "Serial";
	
	public static final String METS_TYPE_MANUSCRIPT	 = "Manuscript";
	
	public static final String METS_TYPE_MONOGRAPH	 = "Monograph";
	
	public static final String METS_TYPE_POSTER		 = "Poster";

	
	// Tags
	//================================================================================
	
	public static final String TAG_STRUCTURAL_MAP 	= "structMap";
	
	public static final String TAG_DIV				= "div";
	
	public static final String TAG_AREA				= "area";
	
	public static final String TAG_FILEGRP			= "fileGrp";
	
	public static final String TAG_DMDSEC			= "dmdSec";
	
	public static final String TAG_FILE				= "file";
	
	public static final String TAG_FLOCAT			= "FLocat";
	
	// DMDSEC
	
	public static final String TAG_DMDSEC_MDWRAP	= "mdWrap"; // <mdWrap MIMETYPE="text/xml" MDTYPE="MARC"> 
	
	// MODS
	
	public static final String TAG_MODS_IDENTIFIER	= "MODS:identifer"; // Technical Requirements of 2018
	
	public static final String TAG_MODS_TITLEINFO	= "MODS:titleInfo";
	
	public static final String TAG_MODS_NONSORT		= "MODS:nonSort";
	
	public static final String TAG_MODS_PARTNUMBER	= "MODS:partNumber";
	
	public static final String TAG_MODS_PARTNAME	= "MODS:partName";
	
	public static final String TAG_MODS_TITLE		= "MODS:title";
	
	public static final String TAG_MODS_SUBTITLE	= "MODS:subTitle";
	
	public static final String TAG_MODS_RECORDINFO	= "MODS:recordInfo";
	
	public static final String TAG_MODS_LANGUAGETERM		= "MODS:languageTerm";
	
	public static final String TAG_MODS_ORIGININFO			= "MODS:originInfo"; // Technical Requirements of 2018
	
	public static final String TAG_MODS_PUBLISHER			= "MODS:publisher"; // Technical Requirements of 2018
	
	public static final String TAG_MODS_RECORDIDENTIFIER 	= "MODS:recordIdentifier"; // Technical Requirements of 2018
	
	public static final String TAG_MODS_DATEISSUED			= "MODS:dateIssued"; // Technical Requirements of 2018
	
	// MODS - AUTHOR / CREATOR
	
	public static final String TAG_MODS_NAME		= "MODS:name";
	
	public static final String TAG_MODS_NAMEPART	= "MODS:namePart";
	
	// MARC
	
	public static final String TAG_MARC_CONTROLFIELD = "controlfield"; // <controlfield tag="001">000107279:LUX01</controlfield>
	
	public static final String TAG_MARC_DATAFIELD	 = "datafield"; // <datafield ind1=" " ind2=" " tag="019">
	
	public static final String TAG_MARC_SUBFIELD	 = "subfield"; // <subfield code="5">03.10.1995</subfield>
	
	// Attribute Names
	//================================================================================
	
	public static final String ATTR_NAME_TYPE			= "TYPE";
	
	public static final String ATTR_NAME_MDTYPE			= "MDTYPE";
	
	public static final String ATTR_NAME_ID				= "ID";
	
	public static final String ATTR_NAME_DMDID			= "DMDID";
	
	public static final String ATTR_NAME_LABEL			= "LABEL";
	
	public static final String ATTR_NAME_BEGIN			= "BEGIN";
	
	public static final String ATTR_NAME_FILEID			= "FILEID";
	
	public static final String ATTR_XLINKHERF 			= "xlink:href";
	
	public static final String ATTR_MODS_TYPE			= "type";
	
	public static final String ATTR_MODS_ORIGININFO_DISPLAYLABEL = "displayLabel";
	
	public static final String ATTR_MARC_TAG	= "tag";
	
	public static final String ATTR_MARC_IND1	= "ind1";
	
	public static final String ATTR_MARC_IND2	= "ind2";

	public static final String ATTR_MARC_CODE	= "code";
	
	
	// Attribute Values
	//================================================================================
	
	public static final String ATTR_TYPE_LOGICAL		= "LOGICAL";
	
	public static final String ATTR_TYPE_PHYSICAL		= "PHYSICAL";
	
	public static final String ATTR_TYPE_ISSUE			= "ISSUE";
	
	public static final String ATTR_FILEGRP_ID_ALTOGRP 	= "ALTOGRP";
	
	public static final String ATTR_PRINT_ID					= "MODSMD_PRINT";
	
	public static final String ATTR_COLLECTION_ID				= "MODSMD_COLLECTION"; // Technical Requirements of 2018
	
	
	public static final String ATTR_IDENTIFIER_TYPE_LOCAL		= "local"; // Technical Requirements of 2018
	
	public static final String ATTR_IDENTIFIER_TYPE_BARCODE		= "barcode"; // Technical Requirements of 2018
	
	
	public static final String ATTR_ORIGININFO_DISPLAYLABEL_MANUFACTURER = "manufacturer"; // Technical Requirements of 2018
	
	
	public static final String ATTR_NAMEPART_TYPE_GIVEN  = "given";
	
	public static final String ATTR_NAMEPART_TYPE_FAMILY = "family";
	
	public static final String ATTR_NAMEPART_TYPE_DATE 	 = "date";
	
	
	public static final String ATTR_DMDSEC_MDTYPE_MODS	 = "MODS"; // <mdWrap MIMETYPE="text/xml" MDTYPE="MODS">
	
	public static final String ATTR_DMDSEC_MDTYPE_MARC	 = "MARC"; // <mdWrap MIMETYPE="text/xml" MDTYPE="MARC">
	
	
	public static final String ATTR_CONTROLFIELD_TAG_001 = "001";
	
	public static final String ATTR_MARC_TAG_TITLE_245	 = "245";
	
	public static final String ATTR_MARC_TAG_CREATOR_100 = "100";
	
	public static final String ATTR_MARC_TAG_DATE_260	 = "260";
	
	//================================================================================
	
	private MetsConstant() {}
	
}
