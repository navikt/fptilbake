# Hack for å omgåes MQ når man kjører mot VTP.

## Bygging av docker image for å teste på egen laptop
Dette er ikke mulig denne saken må testet fra sikker sone. Først bygge applikasjonen, så kjøre
docker build kommandoen med CURL som downloadscript. Dette fordi WGET ikke støtter socks5 ut av 
boksen og CURL ikke er installert på utviklerimaget. Tungvint, men da slipper vi å endre pipeline.
```bash
mvnk -B -Dfile.encoding=UTF-8 -DinstallAtEnd=true -DdeployAtEnd=true  -DskipTests clean install

docker build -t fptilbake .
```

## Kjøre opp FPTILBAKE med VTP-vennlige biblioteker
Dette gjøres med å sette environment-variabel $EXTRA_CLASS_PATH med mappen der artifaktene som
overstyrer MQ ligger. 
```yaml
      - EXTRA_CLASS_PATH=:vtp-lib/*
```

