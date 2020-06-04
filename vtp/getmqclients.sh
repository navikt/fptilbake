export LC_ALL=en_US.UTF-8

token="$1"

if [ -z "$token" ]
then
  echo "Token er NULL!"
  echo "Autentisering mot github vil feile og wget vil returnere 400: Bad Response"
else
  echo "Token er sendt ved. Prøver å hente resursene."
  echo "Henter siste test-jars for okonomistotte-klienten og felles-integrasjon-jms-klienten"
fi


wget --no-check-certificate --method GET --timeout=0 --header "Authorization: Bearer $token" \
   'https://maven.pkg.github.com/navikt/fp-felles/no/nav/foreldrepenger/felles/integrasjon/felles-integrasjon-jms/maven-metadata.xml' \
   -O maven-metadata-jms.xml -nv -t 2
jmsversion=$(grep "<latest>" maven-metadata-jms.xml | sed -e 's/.*<latest>\(.*\)<\/latest>.*/\1/' | cut -c1,2,3,6-)
echo "Siste versjon felles-integrasjon-jms er $jmsversion"
rm maven-metadata-jms.xml

echo "Henterfelles-integrasjon-jms..."
wget --user=x-token --password=$token --content-on-error "https://maven.pkg.github.com/navikt/fp-felles/no/nav/foreldrepenger/felles/integrasjon/felles-integrasjon-jms/$jmsversion/felles-integrasjon-jms-$jmsversion-tests.jar" \
    -O felles-integrasjon-jms.jar -nv -t 2
