package com.github.chen0040.ml.tests.textmining.malicious.url;

import com.github.chen0040.ml.tests.textmining.utils.FileUtils;
import com.github.chen0040.ml.textmining.commons.parsers.ARFF;
import com.github.chen0040.ml.textmining.commons.parsers.ARFFReader;
import com.github.chen0040.ml.textmining.commons.UrlData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class ARFFUtils {

    public static List<UrlData> loadSample(){
        List<ARFF> sources = new ArrayList<>();

        String[] filenames = new String[]{
                "Mohammad14JulyDS_1.arff",
                "Mohammad14JulyDS_2.arff"
        };

        for(int i=0; i < filenames.length; ++i){
            String filename = filenames[i];
            ARFF arff = ARFFReader.parse(FileUtils.getResourceFile(filename));
            sources.add(arff);
        }

        List<UrlData> dataSet = readARFF(sources);

        return dataSet;
    }

    public static List<UrlData> readARFF(List<ARFF> sources){

        List<UrlData> dataSet = new ArrayList<>();

        for(int i=0; i < sources.size(); ++i){
            ARFF arff = sources.get(i);

            int lineCount = arff.getLineCount();

            for(int j=0; j < lineCount; ++j){
                UrlData urlData = new UrlData();
                urlData.setId(UUID.randomUUID().toString());

                int having_IP_Address = arff.get(j, "having_IP_Address");
                urlData.setHasIPAddress(having_IP_Address);

                int URL_Length = arff.get(j, "URL_Length");
                urlData.setUrlLength(URL_Length);

                int Shortining_Service = arff.get(j, "Shortining_Service");
                urlData.setUseShortiningService(Shortining_Service);

                int having_At_Symbol = arff.get(j, "having_At_Symbol");
                urlData.setHasAtSymbol(having_At_Symbol);

                int double_slash_redirecting = arff.get(j, "double_slash_redirecting");
                urlData.setDoubleSlashRedirecting(double_slash_redirecting);

                int Prefix_Suffix = arff.get(j, "Prefix_Suffix");
                urlData.setPrefixSuffix(Prefix_Suffix);

                int having_Sub_Domain = arff.get(j, "having_Sub_Domain");
                urlData.setHasSubDomain(having_Sub_Domain);

                int SSLfinal_State = arff.get(j, "SSLfinal_State");
                urlData.setSslFinalState(SSLfinal_State);

                int Domain_registeration_length = arff.get(j, "Domain_registeration_length");
                urlData.setDomainRegistrationLength(Domain_registeration_length);

                int port = arff.get(j, "port");
                urlData.setUseStdPort(port);

                int HTTPS_token = arff.get(j, "HTTPS_token");
                urlData.setUseHTTPSToken(HTTPS_token);



                int Result = arff.get(j, "Result");
                urlData.setResult(Result);

                dataSet.add(urlData);
            }
        }

        return dataSet;
    }
}
