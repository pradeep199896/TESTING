package com.solace.connector.db.pull.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;


@Service
@Slf4j
public class ClientPushService {

    @Autowired
    private SpringJCSMPFactory solaceFactory;

    private ClientMessageConsumer msgConsumer = new ClientMessageConsumer();
    private ClientPublishEventHandler pubEventHandler = new ClientPublishEventHandler();
    private JCSMPSession session;

    public void initSession() throws Exception {

        session= solaceFactory.createSession();
        session.connect();  // connect to the broker

    }

    public void closeSession(){
        log.info("Exiting.");
        session.closeSession();

    }

    public boolean sendTextMessage(String topicName , String message, MessageHeaderAccessor mha) throws Exception {
        if(session==null){
            initSession();
        }
        Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
        XMLMessageProducer prod = session.getMessageProducer(pubEventHandler);

        TextMessage jcsmpMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        jcsmpMsg.setText(message);

        prod.send(jcsmpMsg, topic);
        return true;
    }

    public boolean sendByteMessage(String topicName , Object obj, MessageHeaderAccessor mha) throws Exception {
        if(session==null){
            initSession();
        }
        Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
        XMLMessageProducer prod = session.getMessageProducer(pubEventHandler);

        byte[] payload =  SerializationUtils.serialize(obj);

        final BytesMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
        bytesMessage.setData(payload);
        prod.send(bytesMessage, topic);
        return true;
    }

    public boolean sendJsonMessage(String topicName , Object obj, MessageHeaderAccessor mha) throws Exception {
        if(session==null){
            initSession();
        }
        Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
        XMLMessageProducer prod = session.getMessageProducer(pubEventHandler);

        ObjectMapper mapper = new ObjectMapper();
        TextMessage jcsmpMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        jcsmpMsg.setText(mapper.writeValueAsString(obj));

        prod.send(jcsmpMsg, topic);
        return true;
    }
}
