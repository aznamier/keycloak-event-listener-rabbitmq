# keycloak-event-listener-rabbitmq

##### A Keycloak SPI plugin that publishes events to a RabbitMq server.  

For example here is the notification of the user updated by administrator

* routing key: `KK.EVENT.ADMIN.MYREALM.SUCCESS.USER.UPDATE`  
* published to exchange: `amq.topic`
* content: 


```
{
  "@class" : "com.github.aznamier.keycloak.event.provider.EventAdminNotificationMqMsg",
  "time" : 1596951200408,
  "realmId" : "MYREALM",
  "authDetails" : {
    "realmId" : "master",
    "clientId" : "********-****-****-****-**********",
    "userId" : "********-****-****-****-**********",
    "ipAddress" : "192.168.1.1"
  },
  "resourceType" : "USER",
  "operationType" : "UPDATE",
  "resourcePath" : "users/********-****-****-****-**********",
  "representation" : "representation details here....",
  "error" : null,
  "resourceTypeAsString" : "USER"
}
```

The routing key is calculated as follows:
* admin events: `KK.EVENT.ADMIN.<REALM>.<RESULT>.<RESOURCE_TYPE>.<OPERATION>`
* client events: `KK.EVENT.CLIENT.<REALM>.<RESULT>.<CLIENT>.<EVENT_TYPE>`

And because the recommended exchange is a **TOPIC (amq.topic)**,  
therefore its easy for Rabbit client to subscribe to selective combinations eg:
* all events: `KK.EVENT.#`
* all events from my realm: `KK.EVENT.*.MYREALM.#`
* all error events from my realm: `KK.EVENT.*.MYREALM.ERROR.#`
* all user events from my-relam and my-client: `KK.EVENT.*.MY-REALM.*.MY-CLIENT.USER`

## USAGE:
1. [Download the latest jar](https://github.com/aznamier/keycloak-event-listener-rabbitmq/blob/target/keycloak-to-rabbit-1.0.jar?raw=true) or build from source: ``mvn clean install``
2. copy jar into your Keycloak `/opt/jboss/keycloak/standalone/deployments/keycloak-to-rabbit-1.0.jar`
3. Configure as described below (option 0 or 1 or 2)
4. Restart the Keycloak server
5. Enable logging in Keycloak UI by adding **keycloak-to-rabbitmq**  
 `Manage > Events > Config > Events Config > Event Listeners`

#### Configuration 
###### OPTION 1: just configure **ENVIRONMENT VARIABLES**
  - `KK_TO_RMQ_URL` - default: *localhost*
  - `KK_TO_RMQ_PORT` - default: *5672*
  - `KK_TO_RMQ_VHOST` - default: *empty*
  - `KK_TO_RMQ_EXCHANGE` - default: *amq.topic*
  - `KK_TO_RMQ_USERNAME` - default: *guest*
  - `KK_TO_RMQ_PASSWORD` - default: *guest*

###### OPTION 2: edit Keycloak subsystem of WildFly standalone.xml or standalone-ha.xml:

```xml
<spi name="eventsListener">
    <provider name="mqtt" enabled="true">
        <properties>
            <property name="url" value="${env.KK_TO_RMQ_URL:localhost}"/>
            <property name="port" value="${env.KK_TO_RMQ_PORT:5672}"/>
            <property name="vhost" value="${env.KK_TO_RMQ_VHOST:}"/>
            <property name="exchange" value="${env.KK_TO_RMQ_EXCHANGE:amq.topic}"/>
            
            <property name="username" value="${env.KK_TO_RMQ_USERNAME:guest}"/>
            <property name="password" value="${env.KK_TO_RMQ_PASSWORD:guest}"/>
        </properties>
    </provider>
</spi>
```
###### OPTION 3: same effect as OPTION 2 but programatically:
```
echo "yes" | $KEYCLOAK_HOME/bin/jboss-cli.sh --file=$KEYCLOAK_HOME/KEYCLOAK_TO_RABBIT.cli
```


