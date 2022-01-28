FROM navikt/java:17-appdynamics

ENV TZ=Europe/Oslo
ENV APPD_ENABLED=true
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true

RUN mkdir /app/lib
RUN mkdir /app/conf

# Config
COPY web/target/classes/jetty/jaspi-conf.xml /app/conf/

# Export vault properties
COPY export-vault.sh /init-scripts/export-vault.sh

# Application Container (Jetty)
COPY web/target/lib/*.jar /app/lib/
COPY web/target/app.jar /app/

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
                -Djava.security.egd=file:/dev/./urandom \
                -Duser.timezone=Europe/Oslo"
