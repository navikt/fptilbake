ventet_sekunder=0
until [[ -f start-oracle.status ]] || [[ $ventet_sekunder -eq 180 ]]
do
    echo "Oracle er ikke oppe enda, har nå ventet i $(ventet_sekunder)..."
    sleep 1
    ventet_sekunder=$((ventet_sekunder+1))
done
if [ -f start-oracle.status ]
then
    cat start-oracle.status
    echo "Oracle er ferdig med oppstart"
else
    echo "Timout ved venting på Oracle"
fi
