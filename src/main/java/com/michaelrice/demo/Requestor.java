package com.michaelrice.demo;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;
import java.util.Random;

public class Requestor {
    public static void main(String... args) throws Exception {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = factory.createConnection("admin", "admin");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destination = new ActiveMQQueue("request-reply-demo");
        MessageProducer producer = session.createProducer(destination);

        //need to ask activemq to create a temporary queue to hold replies
        Destination replyDestination = session.createTemporaryQueue();

        //now let's construct the message with a simple payload
        TextMessage message = session.createTextMessage("michael");
        //load up the message with instructions on how to get back here (JMS Header)
        message.setJMSReplyTo(replyDestination);
        //give it a correlation ID to match (JMS Header)
        message.setJMSCorrelationID(Long.toHexString(new Random(System.currentTimeMillis()).nextLong()));
        producer.send(message);

        //now let's wait for replies
        MessageConsumer consumer = session.createConsumer(replyDestination);
        TextMessage reply = (TextMessage)consumer.receive();
        System.out.println("RECEIVED: "+reply.getText());

        session.close();
        connection.close();
    }
}
