echo "Henter og starter Oracle image docker.pkg.github.com/navikt/fpsak-autotest/oracle-flattened"
docker-compose -f oracle/docker-compose.yml pull
rm -f nohup.out
docker-compose -f oracle/docker-compose.yml up oracle >nohup.out 2>&1 &
sh -c 'tail -n +0 -f nohup.out | { sed "/Disconnected/q" && kill $$; }' || true
echo "finished" > "start-oracle.status"
