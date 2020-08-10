package com.github.aznamier.keycloak.event.provider;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqEventListenerProvider implements EventListenerProvider {

	private RabbitMqConfig cfg;
	private ConnectionFactory factory;

	public RabbitMqEventListenerProvider(RabbitMqConfig cfg) {
		this.cfg = cfg;
		
		this.factory = new ConnectionFactory();

		this.factory.setUsername(cfg.getUsername());
		this.factory.setPassword(cfg.getPassword());
		this.factory.setVirtualHost(cfg.getVhost());
		this.factory.setHost(cfg.getHostUrl());
		this.factory.setPort(cfg.getPort());
	}

	@Override
	public void close() {

	}

	@Override
	public void onEvent(Event event) {
		EventClientNotificationMqMsg msg = EventClientNotificationMqMsg.create(event);
		String routingKey = RabbitMqConfig.calculateRoutingKey(event);
		String messageString = RabbitMqConfig.writeAsJson(msg, true);
		
		BasicProperties msgProps = this.getMessageProps(EventClientNotificationMqMsg.class.getName());
		this.publishNotification(messageString, msgProps, routingKey);
	}

	@Override
	public void onEvent(AdminEvent event, boolean includeRepresentation) {
		EventAdminNotificationMqMsg msg = EventAdminNotificationMqMsg.create(event);
		String routingKey = RabbitMqConfig.calculateRoutingKey(event);
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
