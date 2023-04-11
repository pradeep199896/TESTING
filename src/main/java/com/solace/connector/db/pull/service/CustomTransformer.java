package com.solace.connector.db.pull.service;

import com.solace.connector.core.function.TransformMessageFunctions;
import com.solace.connector.db.pull.lock.SynLock;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomTransformer implements TransformMessageFunctions.BinderAwareMessageTransformer {
    public CustomTransformer() {
    }

    public boolean supports(Set<String> inputBinders, Set<String> outputBinders) {
        return true;
    }

    //handle single entity is ok
    public Map<String, Object> transform(Object messagePayload) {

        Map<String, Object> _payloadAsMap = new HashMap();
        Map<String, Object> children = new HashMap();
        children.put("id", "123");
        children.put("firstName", "Sravan");
        children.put("lastName", "Thotakura");
        _payloadAsMap.put("employee", children);
        return _payloadAsMap;
    }

    //need calling a method like this to handle multiple db rows
    //transformed xml list should be passed to the next sending method
    public List<Map<String, Object>> transformMultiple(Object messagePayload){
        List<Object> list = (List<Object>)messagePayload;

        List<Map<String, Object>> ret=new ArrayList<>();
        for(Object o : list){
            ret.add(transform(o));
        }
        return ret;
    }
}
