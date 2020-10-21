FROM navikt/java:11-appdynamics
ENV APPD_ENABLED=true
ENV TZ=Europe/Oslo

RUN mkdir /app/lib
RUN mkdir /app/conf

# lag en gruppe og en sysembruker (-r) uten passord, uten hjemme-katalog, uten shell.
# Endre så eier til appdynamics-loggene slik at de kan skrives til
RUN groupadd -r applikasjon \
 && useradd -r -s /bin/false -g applikasjon applikasjon \
 && chown applikasjon  /opt/appdynamics/ver*/logs

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

USER applikasjon
