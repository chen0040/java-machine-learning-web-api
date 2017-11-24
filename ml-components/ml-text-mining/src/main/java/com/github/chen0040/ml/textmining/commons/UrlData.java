package com.github.chen0040.ml.textmining.commons;

import java.io.Serializable;

/**
 * Created by root on 10/27/15.
 * Full documentation of the features can be found at http://eprints.hud.ac.uk/24330/6/MohammadPhishing14July2015.pdf
 */
public class UrlData implements Serializable {
    private String urlAddress;

    private String id;


    // 1: phishing
    // -1: normal
    private int result;

    // -1: does not have IP address => legitimate
    // 1: has IP address => phishing
    private int hasIPAddress=-1;

    // -1: URL length < 54 => legitimate
    // 0: URL length >= 54 && URL length <= 75 => suspicious
    // 1: URL length > 75 => phishing
    private int urlLength=1;

    // -1: Not a TinyURL address => legitimate
    // 1: TinyURL address (e.g. domain name starts with "bit.ly" or "tinyurl.com" => phishing
    // list of tiny url sites can be found at http://bit.do/list-of-url-shorteners.php
    private int useShortiningService=-1;

    // -1: URL does not contain "@" symbol => legitimate
    // 1: URL contains "@" symbol => phishing
    private int hasAtSymbol=-1;

    // -1: does not contain redirect using "//" => legitimate
    // 1: URL is "HTTP" and contains redirect using "//" (e.g. "http://www.legitimate.com//http://www.phishing.com") => phishing
    // 1: URL is "HTTPS" but the position of the last occurrence of "//" in the URL > 7 => phishing
    // Note that if the URL starts with "HTTPS", then "//" should appear in seventh position
    private int doubleSlashRedirecting=-1;

    // 1: domain name part includes (-) symbol => phishing
    // -1: otherwise => legitimate
    private int prefixSuffix=-1;

    // -1: Dots in Domain Part = 1 => legitimate
    // 0: Dots in the Domain Part = 2 => suspicious
    // 1: otherwise => phishing
    private int hasSubDomain=-1;
    // -1: Use https and issuer is a trusted and age of certificate >= 1 year => legitimate
    // 0: Using https and Issuer is Not Trusted => suspicious
    // 1: otherwise => phishing
    private int sslFinalState=-1;
    // 1: Domains expires in <= 1 years => phishing
    // -1 otherwise => legitimate

    private int domainRegistrationLength=-1;
    // 1: Port # of the preferred status  => phishing
    // -1: otherwise => legitimate
    // Port # of the preferred status (close): 21, 22, 23, 445, 1433, 1521, 3306, 3389
    private int useStdPort=-1;
    // 1: Using HTTPS token in domain part of the url (e.g., http://https-www-paypal-it-webapps-mpp-home.soft-hair.com) => phishing
    // -1: otherwise => legitimate
    private int useHTTPSToken = -1;

    public double[] attributeArray(){
        return new double[]{
        hasIPAddress,
        urlLength,
        useShortiningService,
        hasAtSymbol,
        doubleSlashRedirecting,
        prefixSuffix,
        hasSubDomain,
        sslFinalState,
        domainRegistrationLength,
        useStdPort,
        useHTTPSToken
        };
    }

    private long timestamp;

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }


    public int getHasIPAddress() {
        return hasIPAddress;
    }

    public void setHasIPAddress(int hasIPAddress) {
        this.hasIPAddress = hasIPAddress;
    }

    public int getUrlLength() {
        return urlLength;
    }

    public void setUrlLength(int urlLength) {
        this.urlLength = urlLength;
    }

    public int getUseShortiningService() {
        return useShortiningService;
    }

    public void setUseShortiningService(int useShortiningService) {
        this.useShortiningService = useShortiningService;
    }

    public int getHasAtSymbol() {
        return hasAtSymbol;
    }

    public void setHasAtSymbol(int hasAtSymbol) {
        this.hasAtSymbol = hasAtSymbol;
    }

    public int getDoubleSlashRedirecting() {
        return doubleSlashRedirecting;
    }

    public void setDoubleSlashRedirecting(int doubleSlashRedirecting) {
        this.doubleSlashRedirecting = doubleSlashRedirecting;
    }

    public int getPrefixSuffix() {
        return prefixSuffix;
    }

    public void setPrefixSuffix(int prefixSuffix) {
        this.prefixSuffix = prefixSuffix;
    }

    public int getHasSubDomain() {
        return hasSubDomain;
    }

    public void setHasSubDomain(int hasSubDomain) {
        this.hasSubDomain = hasSubDomain;
    }

    public int getSslFinalState() {
        return sslFinalState;
    }

    public void setSslFinalState(int sslFinalState) {
        this.sslFinalState = sslFinalState;
    }

    public int getDomainRegistrationLength() {
        return domainRegistrationLength;
    }

    public void setDomainRegistrationLength(int domainRegistrationLength) {
        this.domainRegistrationLength = domainRegistrationLength;
    }

    public int getUseStdPort() {
        return useStdPort;
    }

    public void setUseStdPort(int useStdPort) {
        this.useStdPort = useStdPort;
    }

    public int getUseHTTPSToken() {
        return useHTTPSToken;
    }

    public void setUseHTTPSToken(int useHTTPSToken) {
        this.useHTTPSToken = useHTTPSToken;
    }

    public String getUrlAddress(){
        return urlAddress;
    }

    public void setUrlAddress(String urlAddress){
        this.urlAddress = urlAddress;
    }


    public int getResult(){
        return result;
    }

    public void setResult(int result){
        this.result = result;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

}
