FROM tomcat
ARG WAR_FILE=target/*.war
RUN rm -rf /usr/local/tomcat/webapps/*
COPY ${WAR_FILE} /usr/local/tomcat/webapps/app.war
RUN sh -c 'touch /usr/local/tomcat/webapps/app.war'
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /usr/local/tomcat/webapps/app.war" ]

#FROM adoptopenjdk/openjdk11
#EXPOSE 8288
#ARG WAR_FILE=target/*.war
#COPY ${WAR_FILE} docker_app.war
#ENTRYPOINT ["java","-jar","docker_app.war"]