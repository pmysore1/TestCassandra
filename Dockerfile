FROM tomcat:latest
WORKDIR .
ENV CATALINA_HOME /usr/local/tomcat
COPY ./target/dev.*.war /usr/local/tomcat/webapps/TestCassandra.war
EXPOSE 8080
CMD ["catalina.sh", "run"]