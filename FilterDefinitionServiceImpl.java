package com.projectecho.mc;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.projectecho.et.ETClientObject;
import com.projectecho.et.ETRetrieveDataExtensionObject;
import com.projectecho.et.rest.FilterDefinitionObject;
import com.projectecho.et.rest.FilterDefinitionObject.DerivedFromObjectTypeNameEnum;
import com.projectecho.exceptions.CustomException;
import com.projectecho.mvcmodels.SnapshotFilterDefinitionObject;



//@SessionScope
@Service
public class FilterDefinitionServiceImpl extends CommonRestServiceImpl implements FilterDefinitionService {

    @Autowired
    private DataExtensionService deService;
    
    @Override
    public FilterDefinitionObject create(ETClientObject etClientObject, Integer mid, String accessToken, 
                                         String endpoint, 
                                         SnapshotFilterDefinitionObject ssfd, List<String> errorCollection) throws CustomException, Exception {
        if(ssfd.getDerivedFromObjectTypeName().equalsIgnoreCase(DerivedFromObjectTypeNameEnum.DATA_EXTENSION.value())) {
            Integer deMID = mid;
            try {
                ETRetrieveDataExtensionObject deObj = this.deService.retrieveByName(etClientObject, deMID, ssfd.getDerivedFromObjectName());
                if(deObj == null) {
                    deObj = this.deService.retrieveByName(etClientObject, null, ssfd.getDerivedFromObjectName());
                    deMID = null;
                }
                ssfd.setDerivedFromObjectId(deObj.getId());
                try {
                    String xml = ssfd.getFilterDefinitionXml();
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document xmlDoc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer trans = tf.newTransformer();
                    trans.setOutputProperty("omit-xml-declaration", "yes");
                    StringWriter sw = new StringWriter();
                    trans.transform(new DOMSource(xmlDoc), new StreamResult(sw));
                    ssfd.setFilterDefinitionXml(sw.toString());
                }catch(Exception ee) {
                    throw new CustomException("Failed to map filter conditions with Data Extension columns for DE:"+ssfd.getDerivedFromObjectName()+" for Data Filter: "+ssfd.getName());
                }
            }catch(Exception ee) {
                throw new CustomException("Failed to retrieve Data Extension named "+ssfd.getDerivedFromObjectName()+" for Data Filter: "+ssfd.getName());
            }

        }
        return result.getBody();
    }
}
