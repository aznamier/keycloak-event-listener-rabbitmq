package com.github.aznamier.keycloak.event.provider;


import org.keycloak.Config.Scope;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class RabbitMqConfig {
	
	public static final ObjectMapper rabbitMqObjectMapper = new ObjectMapper();
	public static String ROUTING_KEY_PREFIX = "KK.EVENT";

	private String hostUrl;
	private Integer port;
	private String username;
	private String password;
	private String vhost;
	
	private String exchange;
	
	public static String calculateRoutingKey(AdminEvent adminEvent) {
		//KK.EVENT.ADMIN.<REALM>.<RESULT>.<RESOURCE_TYPE>.<OPERATION>
		String routingKey = ROUTING_KEY_PREFIX
				+ ".ADMIN"
				+ "." + adminEvent.getRealmId()
				+ "." + (adminEvent.getError() != null ? "ERROR" : "SUCCESS")
				+ "." + adminEvent.getResourceTypeAsString()
				+ "." + adminEvent.getOperationType().toString()
				
				;
		return normalizeKey(routingKey);
	}
	
	public static String calculateRoutingKey(Event event) {
		//KK.EVENT.CLIENT.<REALM>.<RESULT>.<CLIENT>.<EVENT_TYPE>
		String routingKey = ROUTING_KEY_PREFIX
					+ ".CLIENT"
					+ "." + event.getRealmId()
					+ "." + (event.getError() != null ? "ERROR" : "SUCCESS")
					+ "." + event.getClientId()
					+ "." + event.getType();
		
		return normalizeKey(routingKey);
	}
	
	//Remove all characters apart a-z, A-Z, 0-9, space, underscore, eplace all spaces and hyphens with underscore
	public static final String normalizeKey(String stringToNormalize) {
		return stringToNormalize.replaceAll("[^\\*#a-zA-Z0-9 _.-]", "").
				replaceAll(" ", "_");
	}
	
	public static String writeAsJson(Object object, boolean isPretty) {
		String messageAsJson = "unparsable";
		try {
			if(isPretty) {
				messageAsJson = RabbitMqConfig.rabbitMqObjectMapper
						.writerWithDefaultPrettyPrinter().writeValueAsString(object);
			} else {
				messageAsJson = RabbitMqConfig.rabbitMqObjectMapper.writeValueAsString(object);
			}
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return messageAsJson;
	}
	
	
	public static RabbitMqConfig createFromScope(Scope config) {
		RabbitMqConfig cfg = new RabbitMqConfig();
		
		cfg.hostUrl = resolveConfigVar(config, "url", "localhost");
		cfg.port = Integer.valueOf(resolveConfigVar(config, "port", "5672"));
		cfg.username = resolveConfigVar(config, "username", "admin");
		cfg.password = resolveConfigVar(config, "password", "admin");
		cfg.vhost = resolveConfigVar(config, "vhost", "");
        
		cfg.exchange = resolveConfigVar(config, "exchange", "amq.topic");
		return cfg;
		
	}
	
	private static String resolveConfigVar(Scope config, String variableName, String defaultValue) {
		
		String value = defaultValue;
		if(config != null && config.get(variableName) != null) {
			value = config.get(variableName);
		} else {
			//try from env variables eg: KK_TO_RMQ_URL:
			String envVariableName = "KK_TO_RMQ_" + variableName.toUpperCase();
			if(System.getenv(envVariableName) != null) {
				value = System.getenv(envVariableName);
			}
		}
		System.out.println("keycloak-to-rabbitmq configuration: " + variableName + "=" + value);
		return value;
		
	}
	
	
	public String getHostUrl() {
		return hostUrl;
	}
	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getVhost() {
		return vhost;
	}
	public void setVhost(String vhost) {
		this.vhost = vhost;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

}
