FROM tomcat:alpine
WORKDIR .
ENV CATALINA_HOME /usr/local/tomcat
ENV profile=dev
ENV JAVA_OPTS="-Dapp_env=dev"
COPY ./target/*.war /usr/local/tomcat/webapps/TestCassandra.war
COPY index.html /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]