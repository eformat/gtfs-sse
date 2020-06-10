package com.redhat.kafkaregistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class GetGtfs {

    private static final Logger log = LoggerFactory.getLogger(GetGtfs.class);

    public static InputStream feedUrlStream(String optApiKey, String optFeed, String optUrl)
            throws IOException {
        /*String key = optApiKey;
        if (optApiKey == null) {
            key = Props.getKey("gtfsToJson");
        }
        if (!"1".equals(optFeed) && !"2".equals(optFeed) && !"11".equals(optFeed)) {
            SharedStderrLog.die("Error: Value of '--feed' or '-f' flag was invalid: %s\n" +
                    "\tPlease use:\n\t\t'1' for the 1,2,3,4,5,6,S trains,\n" +
                    "\t\t'2' for the L train, and\n\t\tand '11' for the SIR", optFeed);
        }
        int paramFeed = optUrl.lastIndexOf("=1&");
        if (paramFeed > -1) {
            optUrl = optUrl.substring(0, paramFeed + 1) + optFeed +
                    optUrl.substring(paramFeed + 2) + key;
        } else {
            SharedStderrLog.die("Error: Uri given to '--url' or '-u' did not contain '=1&' " +
                    "for feed substitution. Uri was: %s", optUrl);
        }*/
        log.debug("Info: Fetching " + optUrl);
        URL url = new URL(optUrl);
        URLConnection conn = url.openConnection();
        log.debug("Info: " + optUrl + " Header Fields: " + conn.getHeaderFields().toString());
        InputStream in = conn.getInputStream();
        return in;
    }
}
