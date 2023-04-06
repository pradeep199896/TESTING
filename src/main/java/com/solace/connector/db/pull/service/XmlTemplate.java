package com.solace.connector.db.pull.service;

import java.util.Map;

public class XmlTemplate {
    String pattern;
    String[] xmlPrefix;

    public XmlTemplate(String pattern){
        this.pattern = pattern;
        xmlPrefix= pattern.split("\\{") ;
        for(int i =0 ;i<xmlPrefix.length ;i++){
            String s = xmlPrefix[i];
            if(s.contains("}")){
                xmlPrefix[i] =s.substring(s.lastIndexOf("}")+1);
            }

        }
    }

    public String translate(Map<String ,Object > values,Map<String ,String> mapping){
        StringBuilder sb = new StringBuilder();

        int i=0;
        for(String key:mapping.keySet()){
            if(mapping.get(key).contains("[")){
                key = key+"_list";
            }
            sb.append(xmlPrefix[i++]).append(values.get(key));
        }
        sb.append(xmlPrefix[i]);
        return sb.toString();
    }
}
