package lu.bnl.custom;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.bnl.AppGlobal;
import lu.bnl.configuration.AppConfigurationManager;
import lu.bnl.files.FileFinder;
import lu.bnl.files.FileUtil;
import lu.bnl.reader.MetsGetter;

/**
 * This MetsGetter is meant to be used with the BnL Keymaps service.
 * Keymaps can return METS metadata by ARK (or PID). Part of the metadata
 * is the path of the METS file. Thus, the exporter must have physical 
 * access to the stored METS/ALTO package which is managed by Keymaps.
 * 
 */
public class KeymapsMetsGetterImpl extends MetsGetter {

    private static final Logger logger = LoggerFactory.getLogger(KeymapsMetsGetterImpl.class);

    @Override
    public String validateInput(String dir, String items) {
		Boolean hasItems	= items != null;
		Boolean isItemsOk	= FileUtil.checkFile(items) != null;

		if (!isItemsOk) {
			logger.info("Abording! Reason: You must provide a valid items file.");
		}

		if (hasItems && isItemsOk) {
			return items;
		}

		return null;
    }

    /**
     * Reads a file containing 1 ARK to process per line.
     * 
     * @param path  The path to the file.
     */
    @Override
    public void findAllMets(String path) {
        List<String> arks = null;

        try {
            arks = FileUtils.readLines(new File(path), Charset.forName("UTF-8"));
        } catch (IOException e) {
            logger.error("Failed to read ARKs file at " + path, e);
			e.printStackTrace();
        }

        this.setMetsData(arks);
    }

    /** The data must be a path to the METS file.
	 *  Return the entire METS file.
	 */
    @Override
    public String getMetsContent(String data) {
        
        String content = null;

        // Find METS
        List<File> files = FileFinder.findMetsFiles(data);
        if (files.size() > 0 ) {
            // Read File Content from first found METS. All METS/ALTO package should only have 1 METS file.
            try {
                String metsPath = files.get(0).getPath();
                content = new String (Files.readAllBytes( Paths.get(metsPath) ));
            } catch (IOException e) {
                logger.error("Failed to read METS file at " + data, e);
			    e.printStackTrace();
            }
        } else {
            logger.error("No METS file found in " + data);
        }

        return content;
    }

    /**
     * Data is ARK, so it is unique.
     */
    @Override
    public String getUniqueName(String data) {
        return data;
    }

    /**
     * Queries BnL's Keymaps service to get the path of the METS location.
     * The string "{ark}" in the getMetsURL config is replaced by the ARK.
     * 
     * @param data  ARK identifier of a document.
     */
    @Override
    public String getMetsLocation(String data) {
        String urlGetMets = AppConfigurationManager.getInstance().getExportConfig().metsGetter.url;
        urlGetMets = urlGetMets.replaceAll("{ark}", data);

        String result = null;
        try {
            URIBuilder builder = new URIBuilder(urlGetMets);
            URL url = builder.build().toURL();

            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");

            result = IOUtils.toString( httpConnection.getInputStream(), AppGlobal.ENCODING );
            
            String message = String.format("getMetsLocation: HTTP Request %s for ARK %s: %s %s", 
					url.toString() ,data, httpConnection.getResponseCode(), httpConnection.getResponseMessage());
            logger.info(message);
            
            logger.debug("getMetsLocation: Size of XML: " + result.length());

        } catch (MalformedURLException e) {
			logger.error("getMetsLocation: An error has occured while requesting the METS.", e);
		} catch (IOException e) {
			logger.error("getMetsLocation: An error has occured while requesting the METS.", e);
		} catch (URISyntaxException e) {
			logger.error("getMetsLocation: An error has occured while requesting the METS.", e);
        }
        
        return this.extractPathFromResponse(result);
    }

    /** Converts the String response to a Java Object using the GSON library in order
	 *  to extract the ARK value.
	 * 
	 * @param response
	 * @return
	 */
	private String extractPathFromResponse(String response) {
		if (response != null) {
			try {
				ResponseMessage responseMessage = new Gson().fromJson(response, ResponseMessage.class);
				return responseMessage.data.rootLocation;
			} catch (Exception e) {
				logger.error("Error during extraction of rootLocation from Response. Response:" + response);
			}
		}
		
		return null;
	}

    
}