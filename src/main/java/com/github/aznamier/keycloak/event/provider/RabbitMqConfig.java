package com.github.aznamier.keycloak.event.provider;


import java.util.Locale;
import java.util.regex.Pattern;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.util.JsonSerialization;


public class RabbitMqConfig {

	private static final Logger log = Logger.getLogger(RabbitMqConfig.class);
	public static final String ROUTING_KEY_PREFIX = "KK.EVENT";
	private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[^*#a-zA-Z0-9 _.-]");
	private static final Pattern SPACE = Pattern.compile(" ");
	private static final Pattern DOT = Pattern.compile("\\.");

	private String hostUrl;
	private Integer port;
	private String username;
	private String password;
	private String vhost;
	private Boolean useTls;

	private String exchange;
	
	public static String calculateRoutingKey(AdminEvent adminEvent) {
		//KK.EVENT.ADMIN.<REALM>.<RESULT>.<RESOURCE_TYPE>.<OPERATION>
		String routingKey = ROUTING_KEY_PREFIX
				+ ".ADMIN"
				+ "." + removeDots(adminEvent.getRealmId())
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
					+ "." + removeDots(event.getRealmId())
					+ "." + (event.getError() != null ? "ERROR" : "SUCCESS")
					+ "." + removeDots(event.getClientId())
					+ "." + event.getType();
		
		return normalizeKey(routingKey);
	}

	//Remove all characters apart a-z, A-Z, 0-9, space, underscore, eplace all spaces and hyphens with underscore
	public static String normalizeKey(CharSequence stringToNormalize) {
		return SPACE.matcher(SPECIAL_CHARACTERS.matcher(stringToNormalize).replaceAll(""))
				.replaceAll("_");
	}
	
	public static String removeDots(String stringToNormalize) {
		if(stringToNormalize != null) {
			return DOT.matcher(stringToNormalize).replaceAll("");
		}
		return stringToNormalize;
	}
	
	public static String writeAsJson(Object object, boolean isPretty) {
		try {
			if(isPretty) {
				return JsonSerialization.writeValueAsPrettyString(object);
			}
			return JsonSerialization.writeValueAsString(object);

		} catch (Exception e) {
			log.error("Could not serialize to JSON", e);
		}
		return "unparsable";
	}
	
	
	public static RabbitMqConfig createFromScope(Scope config) {
		RabbitMqConfig cfg = new RabbitMqConfig();
		
		cfg.hostUrl = resolveConfigVar(config, "url", "localhost");
		cfg.port = Integer.valueOf(resolveConfigVar(config, "port", "5672"));
		cfg.username = resolveConfigVar(config, "username", "admin");
		cfg.password = resolveConfigVar(config, "password", "admin");
		cfg.vhost = resolveConfigVar(config, "vhost", "");
		cfg.useTls = Boolean.valueOf(resolveConfigVar(config, "use_tls", "false"));

		cfg.exchange = resolveConfigVar(config, "exchange", "amq.topic");
		return cfg;
		
	}
	
	private static String resolveConfigVar(Scope config, String variableName, String defaultValue) {
		
		String value = defaultValue;
		if(config != null && config.get(variableName) != null) {
			value = config.get(variableName);
		} else {
			//try from env variables eg: KK_TO_RMQ_URL:
			String envVariableName = "KK_TO_RMQ_" + variableName.toUpperCase(Locale.ENGLISH);
			String env = System.getenv(envVariableName);
			if(env != null) {
				value = env;
			}
		}
		if (!"password".equals(variableName)) {
			log.infof("keycloak-to-rabbitmq configuration: %s=%s%n", variableName, value);
		}
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
	public Boolean getUseTls() {
		return useTls;
	}
	public void setUseTls(Boolean useTls) {
		this.useTls = useTls;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

}
