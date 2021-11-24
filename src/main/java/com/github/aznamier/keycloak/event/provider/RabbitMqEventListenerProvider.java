package com.github.aznamier.keycloak.event.provider;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqEventListenerProvider implements EventListenerProvider {

	private RabbitMqConfig cfg;
	private ConnectionFactory factory;
	
    private KeycloakSession session;
    
	private EventListenerTransaction tx = new EventListenerTransaction(this::publishAdminEvent, this::publishEvent);

	public RabbitMqEventListenerProvider(RabbitMqConfig cfg, KeycloakSession session) {
		this.cfg = cfg;
		
		this.factory = new ConnectionFactory();

		this.factory.setUsername(cfg.getUsername());
		this.factory.setPassword(cfg.getPassword());
		this.factory.setVirtualHost(cfg.getVhost());
		this.factory.setHost(cfg.getHostUrl());
		this.factory.setPort(cfg.getPort());

		if(cfg.getUseTls()) {
			try {
				this.factory.useSslProtocol();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		this.session = session;
		this.session.getTransactionManager().enlistAfterCompletion(tx);
		
	}

	@Override
	public void close() {

	}

	@Override
	public void onEvent(Event event) {
		tx.addEvent(event);
	}

	@Override
	public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
		tx.addAdminEvent(adminEvent, includeRepresentation);
	}
	
	private void publishEvent(Event event) {
		EventClientNotificationMqMsg msg = EventClientNotificationMqMsg.create(event);
		String routingKey = RabbitMqConfig.calculateRoutingKey(event);
		String messageString = RabbitMqConfig.writeAsJson(msg, true);
		
		BasicProperties msgProps = this.getMessageProps(EventClientNotificationMqMsg.class.getName());
		this.publishNotification(messageString, msgProps, routingKey);
	}
	
	private void publishAdminEvent(AdminEvent adminEvent, boolean includeRepresentation) {
		EventAdminNotificationMqMsg msg = EventAdminNotificationMqMsg.create(adminEvent);
		String routingKey = RabbitMqConfig.calculateRoutingKey(adminEvent);
		String messageString = RabbitMqConfig.writeAsJson(msg, true);
		BasicProperties msgProps = this.getMessageProps(EventAdminNotificationMqMsg.class.getName());
		this.publishNotification(messageString,msgProps, routingKey);
	}
	
	private BasicProperties getMessageProps(String className) {
		
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("__TypeId__", className);
		
		Builder propsBuilder = new AMQP.BasicProperties.Builder()
				.appId("Keycloak")
				.headers(headers)
				.contentType("application/json")
				.contentEncoding("UTF-8");
		return propsBuilder.build();
	}
	

	private void publishNotification(String messageString, BasicProperties props, String routingKey) {

		

		try {
			Connection conn = factory.newConnection();
			Channel channel = conn.createChannel();
			
			channel.basicPublish(cfg.getExchange(), routingKey, props, messageString.getBytes());
			System.out.println("keycloak-to-rabbitmq SUCCESS sending message: " + routingKey);
			channel.close();
			conn.close();

		} catch (Exception ex) {
			System.err.println("keycloak-to-rabbitmq ERROR sending message: " + routingKey);
			ex.printStackTrace();
		}
	}

}
