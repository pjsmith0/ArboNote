package com.pjs.ui.htmleditor.component;

import java.net.MalformedURLException;
import java.net.URL;

public class ValidatedURL {

    private final String url;

    public ValidatedURL(String url){
        this.url = url;
    }

    public boolean isValid(){
        if (url.isEmpty()) {
            return false;
        }

        if (url.startsWith("https://") || url.startsWith("http://") //
                || url.startsWith("ftp://") || url.startsWith("ftps://") //
                || url.startsWith("mailto:")) {
            try {
                new URL(url);
                return true;
            } catch (MalformedURLException e) {
                // Nothing to do here
            }
        }
        return false;
    }

}
