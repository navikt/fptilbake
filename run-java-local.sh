#!/usr/bin/env sh

set -eu

## fix classpath ifht. hvordan det bygges av Maven
export PATH_SEP=":"
[[ -z $WINDIR ]] || export PATH_SEP=";"
export CLASSPATH="$(echo $(ls web/webapp/target/app.jar))${PATH_SEP?}web/webapp/target/lib/*"

export WEBAPP="$(echo web/webapp/target/webapp/)"
export I18N="$(echo i18n/src/main/resources/META-INF/resources)"

LOGBACK_CONFIG="web/webapp/src/test/resources/logback-dev.xml"
export LOGBACK_CONFIG=${LOGBACK_CONFIG:-}

export APP_CONFDIR="web/webapp/src/main/resources/jetty/"

## export app.properties til environment
PROP_FILE="web/webapp/app.properties"
PROP_FILE_LOCAL="web/webapp/app-local.properties"
PROP_FILE_TEMPLATE="web/webapp/src/test/resources/app-dev.properties"
[[ ! -f "${PROP_FILE?}" ]] && cp ${PROP_FILE_TEMPLATE?} ${PROP_FILE} && echo "Oppdater passwords i ${PROP_FILE_LOCAL}" && exit 1
export SYSTEM_PROPERTIES="$( grep -v "^#" $PROP_FILE | grep -v '^[[:space:]]*$' | grep -v ' ' | sed -e 's/^/ -D/g' | tr '\n' ' ')"

## export app-local.properties også til env (inneholder hemmeligheter, eks. passord)
[[ -f "${PROP_FILE_LOCAL}" ]] && export SYSTEM_PROPERTIES_LOCAL="$( grep -v "^#" $PROP_FILE_LOCAL | grep -v '^[[:space:]]*$' | grep -v ' ' | sed -e 's/^/ -D/g' | tr '\n' ' ')"

## TODO (Frode M): usikker på om truststore eksponering fremdeles er nødvendig?
## Eksponer TRUSTSTROE til run-java.sh
#export NAV_TRUSTSTORE_PATH="web/webapp/truststore.jts"
#if ! test -r "${NAV_TRUSTSTORE_PATH}";
#then
#    echo "Kjør JettyDevServer en gang for å kopiere ut truststore (n.n.f.f.w.s.t.JettyDevServer#setupSikkerhetLokalt())";
#    exit 1;
#fi
## Eksponerer bare det som allerede ligger i no.nav.modig.testcertificates.TestCertificates og det er kun for testsystemer
#export NAV_TRUSTSTORE_PASSWORD="changeit"

## Overstyr port for lokal kjøring
export SERVER_PORT=8030

## Sett opp samme struktur som i Dockerfile
DIR="conf"
if [ ! -d "${DIR}" ]; then
    mkdir "${DIR}"
fi

cp web/webapp/target/classes/jetty/jaspi-conf.xml conf/
cp web/webapp/target/test-classes/logback-dev.xml conf/logback.xml

## Sample JPDA settings for remote socket debugging
#JAVA_OPTS="${JAVA_OPTS:-} -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"

export APPDYNAMICS=" "
export JAVA_OPTS="${JAVA_OPTS:-} ${SYSTEM_PROPERTIES_LOCAL?} ${SYSTEM_PROPERTIES:-}"

set -eu

hostname=$(hostname)

export JAVA_OPTS="${JAVA_OPTS:-} -Xmx512m -Xms128m -Djava.security.egd=file:/dev/./urandom"

if test -r "${NAV_TRUSTSTORE_PATH:-}";
then
    if ! echo "${NAV_TRUSTSTORE_PASSWORD}" | keytool -list -keystore ${NAV_TRUSTSTORE_PATH} > /dev/null;
    then
        echo Truststore is corrupt, or bad password
        exit 1
    fi

    JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.trustStore=${NAV_TRUSTSTORE_PATH}"
    JAVA_OPTS="${JAVA_OPTS} -Djavax.net.ssl.trustStorePassword=${NAV_TRUSTSTORE_PASSWORD}"
fi

# hvor skal gc log, heap dump etc kunne skrives til med Docker?
export todo_JAVA_OPTS="${JAVA_OPTS} -XX:ErrorFile=./hs_err_pid<pid>.log -XX:HeapDumpPath=./java_pid<pid>.hprof -XX:-HeapDumpOnOutOfMemoryError -Xloggc:<filename>"

export JAVA_OPTS="${JAVA_OPTS} -Djavax.xml.soap.SAAJMetaFactory=com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl"

export STARTUP_CLASS=${STARTUP_CLASS:-"no.nav.foreldrepenger.tilbakekreving.web.server.jetty.JettyServer"}
export LOGBACK_CONFIG=${LOGBACK_CONFIG:-"./conf/logback.xml"}
#export CLASSPATH="app.jar:lib/*"

exec java -cp ${CLASSPATH?} ${DEFAULT_JAVA_OPTS:-} ${JAVA_OPTS} -Dlogback.configurationFile=${LOGBACK_CONFIG?} -Dwebapp=${WEBAPP:-"./webapp"} -Di18n=${I18N:-"./i18n"} -Dapplication.name=FPTILBAKE ${STARTUP_CLASS?} ${SERVER_PORT:-8080} $@
