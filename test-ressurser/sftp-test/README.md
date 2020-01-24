# Ressurser for testing og utvikling av sftp

## Docker container
Det er definert en docker-kontainer i docker-compose.yml, som kan brukes under testing og utvikling mot sftp-tjenester.

Det brukes et image atmoz/sftp, som er et docker-image som bare eksponerer en sftp-tjeneste. 

### Kjøring
```
docker-compose up -d
```

### Konfigurering
Brukere er definert i users.conf (og en siste "last" i docker-compose.yml). Avhengig av test-scenario, så må brukerene få mountet opp skrivbart katalog og ssh-nøkler i seksjonen volumes i docker-compose.yml-filen.

SSH-nøkler for kontaineren ligger i keys-katalogen. Disse er ssh_host_ed25519_key og ssh_host_rsa_key.

Skrivbare mounts, som brukes under testing, ligger i katalogen tmp.

### Genering av nøkler
Nye nøkler kan genereres med kommandoen:
```
ssh-keygen -t rsa -b 4096 -f <filnavn key> -C "<kommentar i pub-nøkkelen>"
```
Dersom nyere OpenSSH, så må nøkkelformat spesifieseres til pem. Det nyere format RFC4716 er ikke støttet av biblioteket JSCH.
``` 
ssh-keygen -t rsa -b 4096 -f <filnavn key> -m pem -C "<kommentar i pub-nøkkelen>"
```
