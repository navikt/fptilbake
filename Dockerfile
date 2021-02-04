FROM navikt/java:11-appdynamics
ENV APPD_ENABLED=true
ENV TZ=Europe/Oslo

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Export vault properties
COPY export-vault.sh /init-scripts/export-vault.sh

# Application Container (Jetty)
COPY web/target/lib/*.jar /app/lib/
COPY web/target/app.jar /app/

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Europe/Oslo -Dapplication.name=${APP_NAME} "
