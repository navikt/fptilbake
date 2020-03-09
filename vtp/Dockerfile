ARG VERSION=latest
ARG REPO=docker.pkg.github.com/navikt/fptilbake/fptilbake
FROM $REPO:$VERSION

# Prep for running in VTP environment, correct log format
RUN mkdir /app/vtp-lib
COPY web/target/test-classes/logback-dev.xml /app/vtp-lib/logback-test.xml
