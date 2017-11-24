package com.github.chen0040.sk.dom.xmldom;

import com.github.chen0040.sk.dom.basic.DomElement;
import com.github.chen0040.sk.dom.basic.DomService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by memeanalytics on 22/8/15.
 */
public class XmlService extends DomService {
    private static XmlService instance;
    public static XmlService getInstance(){
        if(instance == null){
            instance = new XmlService();
        }
        return instance;
    }

    @Override
    public boolean readDoc(File file, String cvsSplitBy, boolean hasHeader, Function<DomElement, Boolean> onLineReady, Consumer<Exception> onFailed) {

        Document doc=null;
        try{

            DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
            DocumentBuilder db=dbf.newDocumentBuilder();
            doc=db.parse(file);
        }catch(IOException ioe)
        {
            if(onFailed != null) onFailed.accept(ioe);
            else ioe.printStackTrace();
        }catch(Exception e)
        {
            if(onFailed != null) onFailed.accept(e);
            else e.printStackTrace();
        }

        boolean success = true;
        Element xml_root=doc.getDocumentElement();

        int line_index = 0;
        for(Node xml_level1=xml_root.getFirstChild(); xml_level1 != null; xml_level1=xml_level1.getNextSibling())
        {
            if(xml_level1.getNodeName().equals("observation"))
            {
                List<String> line = new ArrayList<>();
                for(Node xml_level2=xml_level1.getFirstChild(); xml_level2 != null; xml_level2=xml_level2.getNextSibling())
                {
                    if(xml_level2.getNodeName().equals("entry"))
                    {
                        line.add(((Element)xml_level2).getAttribute("value"));
                    }
                }



                DomElement element = new DomElement(line, line_index++);

                if(onLineReady != null){
                    success = onLineReady.apply(element);
                }
            }
        }

        return success;


    }
}
