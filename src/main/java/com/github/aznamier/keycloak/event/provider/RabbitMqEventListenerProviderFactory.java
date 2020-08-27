package com.github.aznamier.keycloak.event.provider;

import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class RabbitMqEventListenerProviderFactory implements EventListenerProviderFactory {

	private RabbitMqConfig cfg;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		return new RabbitMqEventListenerProvider(cfg);
	}

	@Override
	public void init(Scope config) {
		cfg = RabbitMqConfig.createFromScope(config);
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {

	}

	@Override
	public void close() {

	}

	@Override
	public String getId() {
		return "keycloak-to-rabbitmq";
	}

}
