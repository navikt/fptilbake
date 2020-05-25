ARG VERSION=latest
ARG REPO=docker.pkg.github.com/navikt/fptilbake/fptilbake
FROM $REPO:$VERSION

# Prep for running in VTP environment, correct log format
RUN mkdir /app/vtp-lib
COPY web/target/test-classes/logback-dev.xml /app/vtp-lib/logback-test.xml

# Hack to temporarily bypass MQ, will fail build as it will fail autotests
ARG DOWNLOAD_SCRIPT=getmqclients.sh
ARG TOKEN_CARRIER=""
COPY vtp/$DOWNLOAD_SCRIPT /
RUN chmod +x /$DOWNLOAD_SCRIPT && /$DOWNLOAD_SCRIPT $TOKEN_CARRIER
RUN mv felles-integrasjon-jms.jar /app/vtp-lib
