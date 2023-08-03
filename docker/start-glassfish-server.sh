#!/bin/sh

$GLASSFISH_HOME/asadmin start-domain domain1

$GLASSFISH_HOME/asadmin deploy --force=true $SERVICE_HOME/target/ElegantAccelerationService-1.0-SNAPSHOT.war 

bash
