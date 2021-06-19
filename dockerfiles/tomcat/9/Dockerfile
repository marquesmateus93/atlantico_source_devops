FROM docker.io/library/tomcat:jdk11-adoptopenjdk-hotspot

ARG WAR_NAME

ADD $WAR_NAME.war /usr/local/tomcat/webapps/$WAR_NAME.war
ADD server.xml /usr/local/tomcat/conf/server.xml
