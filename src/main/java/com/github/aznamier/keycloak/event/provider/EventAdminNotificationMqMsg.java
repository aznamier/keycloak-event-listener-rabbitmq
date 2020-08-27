package com.github.aznamier.keycloak.event.provider;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.keycloak.events.admin.AdminEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@JsonTypeInfo(use = Id.CLASS)
public class EventAdminNotificationMqMsg extends AdminEvent implements Serializable  {

	private static final long serialVersionUID = -7367949289101799624L;

	public static EventAdminNotificationMqMsg create(AdminEvent adminEvent) {
		EventAdminNotificationMqMsg msg = new EventAdminNotificationMqMsg();
		msg.setAuthDetails(adminEvent.getAuthDetails());
		msg.setError(adminEvent.getError());
		msg.setOperationType(adminEvent.getOperationType());
		msg.setRealmId(adminEvent.getRealmId());
		msg.setRepresentation(adminEvent.getRepresentation());
		msg.setResourcePath(adminEvent.getResourcePath());
		msg.setResourceType(adminEvent.getResourceType());
		msg.setResourceTypeAsString(adminEvent.getResourceTypeAsString());
		msg.setTime(adminEvent.getTime());
		return msg;
	}

	
}
