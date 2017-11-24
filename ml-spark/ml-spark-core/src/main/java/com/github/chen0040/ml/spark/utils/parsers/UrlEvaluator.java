package com.github.chen0040.ml.spark.utils.parsers;

import com.github.chen0040.ml.spark.utils.UrlData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 10/27/15.
 */
public class UrlEvaluator implements Serializable {

    private static List<String> shortenerSites = new ArrayList<>();

    public static List<String> getShortenerSites() {
        if (shortenerSites.isEmpty()) {
            shortenerSites.add("bit.ly");
            shortenerSites.add("goo.gl");
            shortenerSites.add("tinyurl.com");
            shortenerSites.add("ow.ly");
            shortenerSites.add("su.pr");
            shortenerSites.add("is.gd");
            shortenerSites.add("cli.gs");
            shortenerSites.add("tiny.cc");
            shortenerSites.add("sn.im");
            shortenerSites.add("moourl.com");
            shortenerSites.add("l.gg");
            shortenerSites.add("catchylink.com");
            shortenerSites.add("short.nr");
            shortenerSites.add("para.pt");
            shortenerSites.add("twurl.nl");
            shortenerSites.add("snipurl.com");
            shortenerSites.add("budurl.com");
            shortenerSites.add("xrl.us");
        }

        return shortenerSites;
    }

    public static boolean isValidInetAddress(final String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        if (InetAddressValidator.getInstance().isValid(address)) {
            return true;
        }

        //not an IPV4 address, could be IPV6?
        try {
            return InetAddress.getByName(address) instanceof Inet6Address;
        } catch (final UnknownHostException ex) {
            return false;
        }
    }

    public static URL getUrl(String urlAddress){
        // e.g., urlAddress = "http://example.com:80/docs/books/tutorial/index.html?name=networking#DOWNLOADING"
        String error = null;
        URL url = null;
        urlAddress = urlAddress.toLowerCase();
        try {
            url = new URL(urlAddress);
        } catch (MalformedURLException e) {
            error = e.getMessage();

            if(!urlAddress.startsWith("http://") || !urlAddress.startsWith("https://")){
                urlAddress="http://"+urlAddress;
                error = null;
                try {
                    url = new URL(urlAddress);
                } catch (MalformedURLException e2) {
                    error = e2.getMessage();
                }
            }
        }
        if(error==null && url != null) {
            return url;
        }
        return null;
    }

    public boolean pack(UrlData model){
        String urlAddress = model.getUrlAddress();

        URL url = getUrl(urlAddress);

        if (url != null) {
            String host = url.getHost().toLowerCase(); // host: example.com
            String protocol = url.getProtocol().toLowerCase(); // protocol: http
            int port = url.getPort(); // port: 80
            String path = url.getPath().toLowerCase(); // path:  /docs/books/tutorial/index.html
            String query = url.getQuery(); // query: name=networking
            String ref = url.getRef(); // ref: DOWNLOADING
            String filename = url.getFile(); // filename: /docs/books/tutorial/index.html?name=networking

            model.setHasIPAddress(evaluate_hasIPAddress(host));
            model.setUrlLength(evaluate_urlLength(urlAddress));
            model.setUseShortiningService(evaluate_useShortiningService(host));
            model.setHasAtSymbol(evaluate_hasAtSymbol(urlAddress));
            model.setDoubleSlashRedirecting(evaluate_doubleSlashRedirecting(protocol, path));
            model.setPrefixSuffix(evaluate_prefixSuffix(host));
            model.setHasSubDomain(evaluate_hasSubDomain(host));
            model.setSslFinalState(evaluate_sslFinalState(protocol));
            model.setDomainRegistrationLength(evaluate_domainRegistrationLength(host));
            model.setUseStdPort(evaluate_useStdPort(port));
            model.setUseHTTPSToken(evaluate_useHTTPSToken(host));

            return true;
        }
        return false;
    }

    public static boolean evaluate(UrlData model) {
        UrlEvaluator evaluator = new UrlEvaluator();
        return evaluator.pack(model);
    }


    // -1: does not have IP address => legitimate
    // 1: has IP address => phishing
    public static int evaluate_hasIPAddress(String host) {
        if (isValidInetAddress(host)) {
            return 1;
        }
        return -1;
    }

    // -1: URL length < 54 => legitimate
    // 0: URL length >= 54 && URL length <= 75 => suspicious
    // 1: URL length > 75 => phishing
    public static int evaluate_urlLength(String urlAddress) {
        int len = urlAddress.length();
        if (len <= 54) {
            return -1;
        } else if (len <= 75) {
            return 0;
        } else {
            return 1;
        }
    }

    // -1: Not a TinyURL address => legitimate
    // 1: TinyURL address (e.g. domain name starts with "bit.ly" or "tinyurl.com" => phishing
    // list of tiny url sites can be found at http://bit.do/list-of-url-shorteners.php
    public static int evaluate_useShortiningService(String host) {
        List<String> shorteners = getShortenerSites();
        for(int i=0; i < shorteners.size(); ++i){
            if(host.contains(shorteners.get(i))){
                return 1;
            }
        }
        return -1;
    }

    // -1: URL does not contain "@" symbol => legitimate
    // 1: URL contains "@" symbol => phishing
    public static int evaluate_hasAtSymbol(String urlAddress){
        return urlAddress.indexOf('@') != -1 ? 1 : -1;
    }

    // -1: does not contain redirect using "//" => legitimate
    // 1: URL is "HTTP" and contains redirect using "//" (e.g. "http://www.legitimate.com//http://www.phishing.com") => phishing
    // 1: URL is "HTTPS" but the position of the last occurrence of "//" in the URL > 7 => phishing
    // Note that if the URL starts with "HTTPS", then "//" should appear in seventh position
    public static int evaluate_doubleSlashRedirecting(String protocol, String path) {
        if(path.contains("//")){
            return 1;
        }
        return -1;
    }

    // 1: domain name part includes (-) symbol => phishing
    // -1: otherwise => legitimate
    public static int evaluate_prefixSuffix(String host){
        return host.indexOf('-') != -1 ? 1 : -1;
    }

    // -1: Dots in Domain Part = 1 => legitimate
    // 0: Dots in the Domain Part = 2 => suspicious
    // 1: otherwise => phishing
    public static int evaluate_hasSubDomain(String host){
        if(host.startsWith("www.")){
            host = host.substring(4);
        }

        int dotCount = getCount(host, ".");

        if(dotCount==0){
            return 1;
        } else if(dotCount==1){
            return -1;
        } else {
            return 0;
        }

    }

    public static int getCount(String str, String keyword) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(keyword, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += keyword.length();

            }
        }

        return count;
    }



    // -1: Use https and issuer is a trusted and age of certificate >= 1 year => legitimate
    // 0: Using https and Issuer is Not Trusted => suspicious
    // 1: otherwise => phishing
    public static int evaluate_sslFinalState(String protocol){
        return protocol.equalsIgnoreCase("https") ? -1 : 1;
    }


    // 1: Domains expires in <= 1 years => phishing
    // -1 otherwise => legitimate
    public static int evaluate_domainRegistrationLength(String host){
        return -1;
    }

    // 1: Port # of the preferred status  => phishing
    // -1: otherwise => legitimate
    // Port # of the preferred status (close): 21, 22, 23, 445, 1433, 1521, 3306, 3389
    public static int evaluate_useStdPort(int port){
        return port==21 || port==22 || port==23 || port==445 || port==1433 || port==1521 || port==3306 || port==3389 ? 1 : -1;
    }

    // 1: Using HTTPS token in domain part of the url (e.g., http://https-www-paypal-it-webapps-mpp-home.soft-hair.com) => phishing
    // -1: otherwise => legitimate
    public static int evaluate_useHTTPSToken(String host){
        return host.contains("https") ? 1 : -1;
    }


    public static String extractDomain(String urlAddress) {

        URL url = getUrl(urlAddress);

        if(url != null){
            String host = url.getHost().toLowerCase(); // host: example.com
            return host;
        }
        return null;
    }
}
