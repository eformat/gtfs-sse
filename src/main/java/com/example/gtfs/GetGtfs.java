package com.example.gtfs;

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
        log.debug("Info: Fetching " + optUrl);
        URL url = new URL(optUrl);
        URLConnection conn = url.openConnection();
        log.debug("Info: " + optUrl + " Header Fields: " + conn.getHeaderFields().toString());
        InputStream in = conn.getInputStream();
        return in;
    }
}
