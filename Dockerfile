FROM tomcat:latest
WORKDIR .
ENV CATALINA_HOME /usr/local/tomcat
COPY ./target/*.war /usr/local/tomcat/webapps/
EXPOSE 8080
CMD ["catalina.sh", "run"]