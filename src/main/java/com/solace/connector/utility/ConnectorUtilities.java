package com.solace.connector.utility;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.solace.connector.db.config.DbTableIndicatorConfig;
import com.solace.connector.db.pull.service.XmlTemplate;
import com.solace.connector.exception.InvalidPropertyException;


import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.persistence.EmbeddedId;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.solace.connector.db.config.DbTableIndicatorConfig.*;
import static com.solace.connector.db.pull.service.DataCollecttingService.ready;

@Slf4j
public class ConnectorUtilities {

    public static final String SLASH = "/";
    //XML READ
    static DocumentBuilderFactory factory = null;
    static DocumentBuilder builder = null;
    static TransformerFactory tf = null;
    static Transformer t = null;
    public static String exitMessage = null;

    static {
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }

    static public void leaveExitMessage(String message) {
        ready = new AtomicBoolean(false);
        exitMessage = message;
    }

    static public byte[] getPropertiesStream(String source) {
        OrderedProperties prop = new OrderedProperties();
        Map<String, String> map = new LinkedHashMap<>();
        InputStream is = null;
        byte[] ret =null;
        try {
            is = ConnectorUtilities.class.getResourceAsStream(source);
            //try filelocation
            if (is == null) {
                String projectPath = System.getProperty("user.dir");
                log.info("projectPath : {}", projectPath);
                is = new FileInputStream(projectPath + "/config" + source);
            }
            if(is!=null){
                ret=is.readAllBytes();
            }
            prop.load(is);
            is.close();
        } catch (IOException | NullPointerException e) {
            leaveExitMessage("init mapping configuration failed : " + source);
        }

        return ret;
    }

    static public Map<String, String> getPropertiesMapping(String source) {
        OrderedProperties prop = new OrderedProperties();
        Map<String, String> map = new LinkedHashMap<>();
        InputStream is = null;
        try {
            is = ConnectorUtilities.class.getResourceAsStream(source);
            //try filelocation
            if (is == null) {
                String projectPath = System.getProperty("user.dir");
                log.info("projectPath : {}", projectPath);
                is = new FileInputStream(projectPath + "/config" + source);
            }
            prop.load(is);
            is.close();
        } catch (IOException | NullPointerException e) {
            leaveExitMessage("init mapping properties failed : " + source);
        }

        prop.entrySet().forEach((p) -> {
            String key = p.getKey().toString();
            // replace {db_col} with the key
            String value = p.getValue().toString().replaceAll("\\{db_col\\}", key);
            map.put(key, value);
        });

        return map;
    }

    static void errorInConnectorConfig(String value) {
        log.error("\n============\nEXIT REASON:  Property {} NOT found in the connector_config.properties file ,Check the configuration !\n============", value);
        leaveExitMessage("CheckIndicatorsFailed ! Connector Quited before Launch !");
    }

    static public void checkIndicators(DbTableIndicatorConfig config) {
        //connectorConfig
        if (StrUtil.isEmpty(config.getBinding())) {
            errorInConnectorConfig(BINDING);
        }
        if (StrUtil.isEmpty(config.getTopic())) {
            errorInConnectorConfig(TOPIC);
        }
        if (StrUtil.isEmpty(config.getLvqTopic())) {
            errorInConnectorConfig(LVQ_TOPIC);
        }
        if (StrUtil.isEmpty(config.getFlagIndicator())) {
            errorInConnectorConfig(DB_READ_INDICATOR_COLUMN);
        }
        if (StrUtil.isEmpty(config.getTrackingIdIndicator())) {
            errorInConnectorConfig(DB_TRACKING_ID);
        }
        if (StrUtil.isEmpty(config.getReadTimeIndicator())) {
            errorInConnectorConfig(DB_READ_TIME);
        }
        if (StrUtil.isEmpty(config.getFlagIndicatorProcessingValue())) {
            errorInConnectorConfig(DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_INPROGRESS);
        }
        if (StrUtil.isEmpty(config.getFlagIndicatorProcessedValue())) {
            errorInConnectorConfig(DB_READ_INDICATOR_COLUMN_UPDATE_VALUE_PROCESSED);
        }
        if (StrUtil.isEmpty(config.getFlagIndicatorDefaultValue())) {
            errorInConnectorConfig(DB_READ_INDICATOR_COLUMN_DEFAULT_VALUE);
        }
        if (StrUtil.isEmpty(config.getFlagIndicatorFailedValue())) {
            errorInConnectorConfig(DB_READ_INDICATOR_COLUMN_FAILED_VALUE);
        }
        if (StrUtil.isEmpty(config.getReadIndicatorOrderBy())) {
            errorInConnectorConfig(DB_READ_INDICATOR_ORDERBY);
        }
        if (StrUtil.isEmpty(config.getReadIndicatorOrder())) {
            errorInConnectorConfig(DB_READ_RECORD_SEQUENTIAL_ORDER);
        }

    }

    static public void checkEntity(String entityName) {
        if (!StrUtil.isEmpty(entityName)) {
            try {
                Object o = Class.forName(entityName).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error("\n============\nEXIT REASON:  EntityName {} Defined in connectorConfig.properties ClassNotFound , Check the configuration !\n============", entityName);
                leaveExitMessage("CheckIndicatorsFailed ! Connector Quited before Launch !");
            }
        } else {
            log.error("\n============\nEXIT REASON:  EntityName {} Defined in connectorConfig.properties is Empty ,Check the configuration !\n============", entityName);
            leaveExitMessage("CheckIndicatorsFailed ! Connector Quited before Launch !");
        }
    }

    static public void checkPropertiesMap(String tableName, String entityClassName, Map<String, String> trgtMapping) {
        if (trgtMapping.isEmpty()) {
            log.error("\n============\nEXIT REASON:  Check if the properties file {}_trgt_schema_mapper.properties exists and is well formed !\n============", tableName );
        }

        for (String key : trgtMapping.keySet()) {
            try {
                checkProperty(entityClassName, key);
            } catch (ClassNotFoundException | InvalidPropertyException | IntrospectionException e) {
                leaveExitMessage("CheckPropertiesMap Failed ! Connector Quited before Launch !");
            }
        }
    }

    static public void checkEntityIndicators(String entityClassName,DbTableIndicatorConfig config) {
        try {
            Class entityClass = Class.forName(entityClassName);
            checkProperty(entityClassName, config.getFlagIndicator());
            checkProperty(entityClassName, config.getTrackingIdIndicator());
            for (String orderBy : config.getReadIndicatorOrderBy().split(",")) {
                if (orderBy.contains(".")) {
                    checkProperty(entityClassName, orderBy.split("\\.")[1]);
                } else {
                    checkProperty(entityClassName, orderBy);
                }
            }
        } catch (ClassNotFoundException | IntrospectionException e) {
            log.error("\n============\nEXIT REASON:  ERROR in checkEntityIndicators,ClassNotFound :{}!\n============", e.getMessage());
        } catch (InvalidPropertyException e) {
            leaveExitMessage("CheckEntityIndicators Failed ! Connector Quited before Launch !");
        }

    }

    static private void checkProperty(String entityClassName, String property) throws ClassNotFoundException, InvalidPropertyException, IntrospectionException {
        Class entityClass = Class.forName(entityClassName);
        Field field = ClassUtil.getDeclaredField(entityClass, "id");
        if (hasEmbeddedId(entityClassName)) {
            Class idClass = Class.forName(entityClassName + "Id");
            if (ClassUtil.getDeclaredField(entityClass, property) == null) {
                if (ClassUtil.getDeclaredField(entityClass, "id") != null) {
                    if (ClassUtil.getDeclaredField(idClass, property) == null) {
                        log.error("\n============\nEXIT REASON:  ERROR in checkProperties, [{}] NOT in entity or embedded id {}!\n============", property, entityClass.getName());
                        throw new InvalidPropertyException(property + " of " + idClass.getName());
                    }
                } else {
                    log.error("\n============\nEXIT REASON:  ERROR in checkProperties, [{}] NOT in entity {}!\n============", property, entityClass.getName());
                    throw new InvalidPropertyException(property + " of " + entityClass.getName());
                }
            }
        } else {
            if (ClassUtil.getDeclaredField(entityClass, property) == null) {
                log.error("\n============\nEXIT REASON:  ERROR in checkProperties, [{}] NOT in entity {}!\n============", property, entityClass.getName());
                throw new InvalidPropertyException(property + " of " + entityClass.getName());
            }
        }
    }

    static private boolean hasEmbeddedId(String entityClassName) throws ClassNotFoundException, IntrospectionException {
        Class entityClass = Class.forName(entityClassName);
        Field field = ClassUtil.getDeclaredField(entityClass, "id");
        if (field != null) {
            PropertyDescriptor pd = new PropertyDescriptor("id", entityClass);
            return pd.getReadMethod().getAnnotation(EmbeddedId.class) != null || field.getAnnotation(EmbeddedId.class) != null;
        }

        return false;
    }



    static private String buildXmlDocWithTemplate(String entity,DbTableIndicatorConfig tableConfig, Map<String, String> mapping,
                                                  Map<String, Map<String, String>> subEntityMapping ,List<Map<String, Object>> rowMapList, boolean isSub) {

        Map<String, XmlTemplate> xmlTemplateMap = tableConfig.getXmlTemplateMap();
        Map<String, String> xmlSubTemplateMap = tableConfig.getXmlSubTemplateMap();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XmlTemplate template = null;
        StringBuilder sb = new StringBuilder();
        Map<String, String> childrenStrMap = new HashMap<>();

        //build single document for an entity
        for (Map<String, Object> rowMap : rowMapList) {
            try {
                synchronized (xmlTemplateMap) {
                    if(!isSub) {
                        if (xmlSubTemplateMap.isEmpty()) {
                            // read the mapping to initialize the sub entities
                            for (String k : mapping.keySet()) {
                                Object value = rowMap.get(k);
                                String[] valuePattern = mapping.get(k).split(SLASH);
                                String subKey = valuePattern[valuePattern.length - 1].replace("[", "").replace("]", "");
                                if (value instanceof List) {

                                    childrenStrMap.put(k + "_list", buildXmlDocWithTemplate(k,tableConfig, subEntityMapping.get(subKey), subEntityMapping, (List<Map<String, Object>>) value, true));
                                    xmlSubTemplateMap.put(k, subKey);
                                }
                            }
                        } else {
                            for (String k : xmlSubTemplateMap.keySet()) {
                                childrenStrMap.put(k + "_list", buildXmlDocWithTemplate(k,tableConfig, subEntityMapping.get(xmlSubTemplateMap.get(k)), subEntityMapping, (List<Map<String, Object>>) rowMap.get(k), true));
                            }
                        }
                    }
                    if (!xmlTemplateMap.containsKey(entity)) {
                        synchronized (t) {
                            Document doc = builder.newDocument();

                            for (String k : mapping.keySet()) {
                                Object value = mapping.get(k);
                                if (!(value instanceof List)) {
                                    if (rowMap.get(k) == null) {
                                        rowMap.put(k, "");
                                    }
                                    buildTemplate(doc, doc, mapping.get(k), k, rowMap.get(k), xpath, subEntityMapping);
                                } else {
                                    continue;
                                }
                            }

                            StringWriter sw = new StringWriter();
                            t.transform(new DOMSource(doc), new StreamResult(sw));
                            template = new XmlTemplate(sw.toString());

                            xmlTemplateMap.put(entity, template);
                        }
                    } else {
                        template = xmlTemplateMap.get(entity);
                    }
                }
            } catch (TransformerException e) {
                log.error("build xml template failed :", e);
            }

            rowMap.putAll(childrenStrMap);
//            sb.append(StrUtil.format(template, rowMap));
            sb.append(template.translate(rowMap,mapping));


        }

        return sb.toString();

    }

//    static public String buildXmlDocWithTemplate(String entity, DbTableIndicatorConfig tableConfig, List<Map<String, Object>> rowMapList, boolean isSub) {
//
//        Map<String, String> mapping = tableConfig.getTrgtMapping();
//        Map<String, Map<String, String>> subEntityMapping= tableConfig.getSubTrgtMapping();
//
//        return buildXmlDocWithTemplate(entity,tableConfig,mapping,subEntityMapping,rowMapList,false);
//    }

    static public String buildXmlDoc(String entity, Map<String, String> mapping, Map<String, Map<String, String>> subEntityMapping, List<Map<String, Object>> rowMapList) {

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        List<Document> docs = new ArrayList<>();
        //build single document for an entity
        for (Map<String, Object> rowMap : rowMapList) {
            Document doc = builder.newDocument();
            mapping.keySet().forEach(k -> {
                buildNode(doc, doc, mapping.get(k), k, rowMap.get(k), xpath, subEntityMapping);
            });
            docs.add(doc);
        }

        //merge the documents
        Document doc = builder.newDocument();
        for (Document d : docs) {
            if (!doc.hasChildNodes()) {
                Node root = doc.adoptNode(d.getDocumentElement());
                doc.appendChild(root);
            } else {
                Node root = doc.adoptNode(d.getDocumentElement());
                for (int i = 0; i < root.getChildNodes().getLength(); i++) {
                    doc.getDocumentElement().appendChild(root.getChildNodes().item(i));
                }
            }
        }

        StringWriter sw = new StringWriter();
        try {
            t.transform(new DOMSource(doc), new StreamResult(sw));
        } catch (TransformerException e) {
            log.error("build xml doc failed :", e);
        }

        return sw.toString();
    }

    static public Node buildNode(Document doc, Node branch, String keys, String valueKey, Object value, XPath xpath, Map<String, Map<String, String>> subEntityMapping) {
        return buildNode(doc, branch, keys, valueKey, value, xpath, subEntityMapping, false);
    }

    static public Node buildTemplate(Document doc, Node branch, String keys, String valueKey, Object value, XPath xpath, Map<String, Map<String, String>> subEntityMapping) {
        return buildNode(doc, branch, keys, valueKey, value, xpath, subEntityMapping, true);
    }

    static public Node buildNode(Document doc, Node branch, String keys, String valueKey, Object value, XPath xpath, Map<String
            , Map<String, String>> subEntityMapping, boolean isTemplate) {
        if (keys.contains(SLASH)) {
            String current = keys.substring(0, keys.indexOf(SLASH));

            String next = keys.substring(keys.indexOf(SLASH) + 1);

            Node currentNode = createOrGetNode(doc, branch, current, xpath);
            Node nextNode = buildNode(doc, currentNode, next, valueKey, value, xpath, subEntityMapping, isTemplate);
            return currentNode;
        } else {
            if (keys.matches("\\[.*\\]")) {
                if (isTemplate) {
                    branch.setTextContent("{" + valueKey + "_list}");
                    return branch;
                } else {
                    if (((List<Map<String, Object>>) value).size() == 0) {
                        return branch;
                    }
                    String k = keys.replace("[", "").replace("]", "");
                    String subs = buildXmlDoc(k, subEntityMapping.get(k), subEntityMapping, (List<Map<String, Object>>) value);
                    try {
                        Document sub = builder.parse(new InputSource(new StringReader(subs)));

                        Node newSubNode = sub.getDocumentElement();
                        newSubNode = doc.adoptNode(newSubNode);
                        branch.getParentNode().insertBefore(newSubNode, branch);
                        branch.getParentNode().removeChild(branch);

                        return newSubNode;
                    } catch (SAXException | IOException e) {
                        log.error("build xml doc failed :", e);
                        return null;
                    }
                }


            } else {
                Node currentNode = createOrGetNode(doc, branch, keys, xpath);
                if (isTemplate) {
                    currentNode.setTextContent("{" + valueKey + "}");
                } else {
                    if (value instanceof byte[]) {
                        currentNode.setTextContent(new String((byte[]) value, StandardCharsets.UTF_8));
                    } else {
                        currentNode.setTextContent(value == null ? "" : value.toString());
                    }
                }

                return currentNode;
            }
        }
    }

    static public Node createOrGetNode(Document doc, Node branch, String el, XPath xpath) {
        if (getNodes(branch, xpath, el).size() > 0) {
            return getNodes(branch, xpath, el).get(0);
        } else {
            Node n = doc.createElement(el);
            branch.appendChild(n);
            return n;
        }
    }

    static private List<Node> getNodes(Node doc, XPath xpath, String exp) {
        List<Node> list = new ArrayList<>();
        try {
            XPathExpression expr =
                    xpath.compile(exp);
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                list.add(nodes.item(i));
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return list;
    }


}
