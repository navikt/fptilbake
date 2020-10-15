#!/usr/bin/env sh
set -eu

export JAVA_OPTS="${JAVA_OPTS:-} -Xmx1024m -Xms128m -Djava.security.egd=file:/dev/./urandom"

export STARTUP_CLASS=${STARTUP_CLASS:-"no.nav.foreldrepenger.tilbakekreving.web.server.jetty.JettyServer"}
export CLASSPATH="app.jar:lib/*"

exec java -cp ${CLASSPATH} ${DEFAULT_JVM_OPTS:-} ${JAVA_OPTS} -Dlogback.configurationFile="./conf/logback.xml" -Dapplication.name=${APP_NAME} ${STARTUP_CLASS?} $@
