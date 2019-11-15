package lu.bnl.util;

import lu.bnl.configuration.AppConfigurationManager;

public class ArkUtils {

    public static String getArkForArticle(String documentID, String articleType, String articleId) {
        String ark = null;
        if (AppConfigurationManager.getInstance().getExportConfig().ark.useIdAsArk == true) {
			// Base
			ark = documentID;

			// Prefix
			String prefix = AppConfigurationManager.getInstance().getExportConfig().ark.prefix;
			ark = prefix + ark;

			// Qualifier
			if (articleId != null) {
				if (articleType.equalsIgnoreCase("ISSUE") || articleType.equalsIgnoreCase("VOLUME")) {
					// Issue/Volume level. Do nothing.
				} else {
					// Any Article level
					ark += String.format("/articles/%s", articleId);
				}
			}
			//System.out.println("ARK: " + ark); // Debug
        }
        return ark;
    }
    
}