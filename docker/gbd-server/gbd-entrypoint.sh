#!/bin/sh

/usr/bin/tor -f /etc/tor/torrc > /logs/tor.log &

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -XX:+UseContainerSupport -Xmx256m -Xss512k -XX:MetaspaceSize=100m -jar /apps/gbd-server.jar > /logs/gbd-server.log &

sleep 1d