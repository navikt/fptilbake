FROM navikt/java:11-appdynamics
ENV APPD_ENABLED=true
ENV TZ=Europe/Oslo

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/logback.xml /app/conf/
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Application Start command
COPY run-java.sh /
RUN chmod +x /run-java.sh

# Export vault properties
COPY export-vault.sh /init-scripts/export-vault.sh

# Application Container (Jetty)
COPY web/target/lib/*.jar /app/lib/
COPY web/target/app.jar /app/

