package eu.fiestaiot.messagebus.dispatcher.impl.dataservices;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.fiestaiot.commons.util.PropertyManagement;





public class MessageDispatcherImpl extends Thread {

	/**
	 * The logger's initialization.
	 */
	final Logger logger = LoggerFactory.getLogger(MessageDispatcherImpl.class.getName());

	/**
	 * The name of the topic.
	 */
	String topicName;

	/**
	 * The URI of the connector. Used to establish an ActiveMQConnection.
	 */
	String endpointURI;

	/**
	 * The URI of the broker. Used to establish an ActiveMQConnection.
	 */
	String brokerURI;

	/**
	 * The clientID. Used to establish an ActiveMQConnection.
	 */
	String clientID;

	/**
	 * The connection to the topic.
	 */
	Connection connection;

	private String IoTREGISTRY;

	/**
	 * The default constructor
	 */
	public MessageDispatcherImpl() {
		PropertyManagement propertyManagement = new PropertyManagement();
		this.topicName = propertyManagement.getMessagebusDispatcherTopic();
		this.endpointURI = propertyManagement.getTpiApiMessagebusEndpointURI();
		this.clientID = propertyManagement.getDispatcherClientid();
		this.brokerURI = propertyManagement.getMessagebusDispatcherBrokerURI();
		IoTREGISTRY = propertyManagement.getIoTRegistryURI();
	}

	/**
	 * The run method. Implements the dispatcher method for getting the messages
	 * that arrive at the topic.
	 */
	public void run() {
		logger.info("Topic: " + topicName);
		BrokerService broker = new BrokerService();
		TransportConnector connector = new TransportConnector();
		try {
			connector.setUri(new URI(brokerURI));
			broker.addConnector(connector);
			broker.start();
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
		} catch (Exception e) {
			logger.info("[ERROR]: Error occured while starting the broker. " + e);
		}

		try {
			createConnection(clientID);
			Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			Topic topic = session.createTopic(topicName);
			MessageConsumer durableSubscriber = session.createConsumer(topic);
			MessageListener tl = new MessageListener() {
				public void onMessage(Message message) {
					if (message instanceof TextMessage) {
						TextMessage msg = (TextMessage) message;

//						byte[] by;
//						String text = null ;
//						try {
//							by = ((TextMessage) msg).getText().getBytes("ISO-8859-1");
//							text = new String(by,"UTF-8");
//						} catch (UnsupportedEncodingException | JMSException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						logger.info("Text Converted to UTF8:-=-=-=-=-=-=--=-=-=-=-=-=-=-=-=-> " );
//						logger.info("Text Converted to UTF8: " + text);
						
						String recievedBodyString = null;
						
//						try {
//							recievedBodyString = msg.getText().replaceAll("\\n", "");
//							recievedBodyString = recievedBodyString.replaceAll("\\", "");
//							
//						} catch (JMSException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
						
						try {
							recievedBodyString = StringEscapeUtils.unescapeJava(msg.getText());
						} catch (JMSException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						logger.debug("Message Received from Topic [" + topicName + "]");
						recievedBodyString = recievedBodyString.replaceAll("^\"|\"$", "");
						//logger.error("recieved body: " + recievedBodyString);

						try {
							String mbContentType = msg.getJMSType().split("Content-Type:")[1];
							logger.info("JMS TYPEE: " + msg.getJMSType());
							
							 final HttpClient client = HttpClients.createDefault();
							 final HttpPost request = new HttpPost(IoTREGISTRY);
							
							 StringEntity params = null;
							 try {
								params = new StringEntity(recievedBodyString);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							 request.addHeader("content-type", mbContentType);
							 request.setEntity(params);
							
							 final RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000)
							 .setConnectionRequestTimeout(30000).build();
							 request.setConfig(config);
							
							 HttpResponse response = null;
							try {
								response = client.execute(request);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							 final int sc = response.getStatusLine().getStatusCode();						 
							 
							 //Accept all 2XX responses, otherwise consider as error
							 if (sc != HttpStatus.SC_ACCEPTED && sc != HttpStatus.SC_OK && sc != HttpStatus.SC_CREATED && sc != HttpStatus.SC_MULTI_STATUS && 
									 sc != HttpStatus.SC_NO_CONTENT && sc != HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION && 
									 sc != HttpStatus.SC_PARTIAL_CONTENT && sc != HttpStatus.SC_RESET_CONTENT) {
							    logger.error("Failed to send data to the IoT Registry. [ status code: " + sc + " ].");
							 } else {
							    logger.info("Data sent successfully to the IoT Registry. [status code: " + sc + " ].");
							 }

							msg.acknowledge();
						} catch (JMSException e) {
							logger.error("[ERROR]: " + e.toString());
						}
					} else {/* do nothing */}
				}

			};

			durableSubscriber.setMessageListener(tl);

			// TODO: check if connection needs to be handled.
			// If running as daemon, no need probably.
			// connection.close();
		} catch (JMSException e1) {
			logger.info("[ERROR]: " + e1);
		}
	}

	/**
	 * Start the connection.
	 * 
	 * @param clientID
	 *            the id for this specific connection.
	 */
	public void createConnection(String clientID) {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(endpointURI);
		try {
			connection = connectionFactory.createConnection();
			connection.setClientID(clientID);
			connection.start();
		} catch (JMSException e) {
			logger.error("[ERROR]: Error occured while starting the connection to the broker. " + e);
		}
	}

	/**
	 * Close the connection.
	 * 
	 */
	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				logger.error("[ERROR]: Error occured while closing the connection to the broker. " + e);
			}
		}
	}
}
