# BnL Mets Exporter

This project is a command line tool used by the [National Library of Luxembourg (Bibliothèque nationale de Luxembourg - BnL)](http://www.bnl.public.lu/) to parse METS/ALTO documents and export them into another format. 

Note that this project is highly tailored to the BnL's METS/ALTO requirements and current workflows. 

# Requirements:

- Java 8
- Maven

# Data

This program is tailored for the data of the National Library of Luxembourg.
You can download a small dataset [here](http://downloads.bnl.lu).

# Getting Started

## 1. Download and Build 

Download or clone this repository, then go inside and build it using Maven: `mvn install`

## 2. Config

A default `config_example` folder is provided with this project.
Make a copy and rename it to `config`.

For testing purposes, you can use the config as it is, but the different URLs should be modified for your needs in the `export.yml`.

To test the configuration you can run:

	java -jar BnLMetsExporter.jar --testconfig

## 3. Run

The tool supports 2 modes:

1. Primo: Parse METS/ALTO and exports each document unit to a XML document (OAI with Dublin Core). The result is directly saved into a `tar.gz`.
2. Solr: Parse METS/ALTO and load each document unit to Solr indices.

**Note:** A document unit is for example a newspaper article, section, illustration, advertisement, ... or book chapter, ... and is defined in the `export.yml` file.


Command for exporting to `tar.gz` from a local folder (where your METS/ATLO are):

	java -jar BnLMetsExporter.jar -export primo -dir path/to/metsalto

For exporting to Solr, refer to the advanced section below.

## 4. Output

The output will be saved in: `./import-for-primo.tar.gz`.

## More Commands

For help and details on the available commands, just run:

	java -jar BnLMetsExporter.jar -h

# Build

To build, simply call `mvn install`.
The jar file will be `BnLMetsExporter.jar` in a folder named `target`.


# License

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

See `COPYING` to see the full text.

# Contributions

Contributions to this project will be evaluated on a case-by-case basis.
Some part of this software is general, but this project is highly tailored to the needs of the National Library of Luxembourg. 
If your needs are different, please fork your own version and modify it.


# Advanced

## Getting Mets files differently

The program can fetch Mets files from an URL in the following format: `http://127.0.0.1:8080/getMets?pid=`.
You have to provide a list of IDs in a line-by-line text file e.g. `-pids 10pids.lst`.
The PID will be appended to the "get Mets Url".
Note that Alto files should still be accessible via disk and so you have to provide the argument `-dir path/to/metsalto` that points to the root where alto files can be found relative to what is written in the METS file. 

**Custom**

In addition to the already implemented methods, you can write your own implementation of the abstract class `MetsGetter`.
It is located in the package `lu.bnl.reader`. 

## Getting Started with Solr Export

In case you want to do an export to Solr, the following chapters will present a guide to know how to get started with a Solr installation.

### Development Environment

The easiest way to create and manage a Solr Cloud on a development environment is using the Solr cloud example as explained here:
https://lucene.apache.org/solr/guide/6_6/getting-started-with-solrcloud.html

### Config

On the development environment, create a new configset in `solr/server/solr/configsets/`.
For this, just copy the `basic_configs/` to `my_config_sets`.
Under `my_config_set/conf/`, upload your own configuration from the folder `config_example/solr` of this project.

Then the config must be reloaded into ZooKeeper (upconfig):

	$ solr-6.6.0/server/scripts/cloud-scripts/zkcli.sh -zkhost localhost:9983 -cmd upconfig -confname my_config_set -confdir solr-6.6.0/server/solr/configsets/my_config_set/conf/

After this step. All collections must be reloaded.

**IMPORTANT: The Solr config in this repository is for development only.**

### Solr Collections / Cores

There are 2 indices:

1. Article Index: Stores the data of a single article, chapter, section, etc.
2. Page Index: Stores the data of a complete page

So, you must create two cores or collections using the configsets above. The default names are `my_articles` and `my_pages` as written in the default `export.yml`

In the config, note that `articleURL` and `pageURL` is for Solr running in single host mode (not cloud).
`articleCollection` and `pageCollection` along with `zkHost` must be specified for a Solr running in Cloud mode.

### Cleaning the Solr Indices

Use the command `-clean` to clean the Solr article and page indices. Add `-cloud` if it is a Solr Cloud environment.

	$ java -jar BnLMetsExporter.jar -clean -cloud

## Solr FAQ

### What are the key changes in the schema?

- Solr is in `ClassicIndexSchemaFactory` mode. The code `<schemaFactory class="ClassicIndexSchemaFactory"/>` has been added to the `config/solr/solrconfig.xml` file.
- There is only 1 schema. Articles and Pages use the same schema.
- Most dynamic fields are commented out.
- The custom field `text_primo` tries to follow the same analysis and query rules as the internal system of the BnL (Primo).


### How is the full text stored?

The full text is stored in 2 text fields, text_lines and text_words.

- **text_lines**: Stores the full text, which contains HTML and custom XML tags and is meant for web display to a user. 
- **text_word**: Stores each word and its coordinate on the page in a custom format (using XML tags).

### Why use HTML to store line ending information?

The current BnLViewer displays the full text and needs the line by line information.
This allows to display the full text in the same format as it was on the physical paper.

The easiest solution is to store the line ending information inside the full text directly.
HTML tags allows to have a standard way to mark this information without interfering with the analyser, because it uses an `solr.HTMLStripCharFilterFactory` (only during indexing, not during querying).

This has the advantage of being simple to parse, manipulate and to still use Solr's built-in highlight feature.

### How are line endings stored?

Line endings are stored using custom XML tags in the `text_lines` field.
At the moment, each new line is marked with a `<br>`.
Every line ending with hyphenated character is stored as `<le s="X-" e="Y">XY</le>`.
*s* contains *X-* (with hyphen), which is the start of the word.
*e* contains *Y*, which is the end of the word.
*X* is located in line *n* and *Y* is located in line *n+1*.
The word (without hyphen) *XY* is required to allow the analyser to search it fully. 
This word is also what is highlighted, meaning that some post-processing is required to recreate the correct line by line text.

### How are word coordinates stored?

Word coordinates are stored using custom XML tags in the `text_words` field.
The full text is stored as as a sequence of `<w>` tags like this:

	<w a="DTL1" b="BLOCK1" p="ALTO00001" x="300" y="1400" w="287" h="68">Hello</w>
	<w a="DTL1" b="BLOCK2" p="ALTO00001" x="600" y="1400" w="290" h="67">World</w>
	...
	
Again, using the `solr.HTMLStripCharFilterFactory` allows to ignore all tags, but still be able to search the words using the same analyser as if it were a full text. The Solr highlighting works perfectly with the correct options, such as `hl.fragsize=0` and `hl.maxAnalyzedChars=10000000`.
This solutions allows to easily retrieve the coordinates of the words for any page and article.




