IP="217.147.30.214"
for host in api.tennisi.bet tennisi.bet int.maxmin.ru api.int.maxmin.ru tennisi.int.maxmin.ru api-tennisi.int.maxmin.ru; do
  for path in /api/live /api/prematch /api/tennisi/live /api/tennisi/prematch /api/v1/live /api/v1/prematch /api/v1/tennisi/live /api/v1/tennisi/prematch /live /prematch; do
    res=$(curl -k -s -o /dev/null -w "%{http_code}" --resolve "$host:443:$IP" "https://$host$path")
    if [ "$res" != "404" ] && [ "$res" != "000" ]; then
      echo "FOUND: https://$host$path -> $res"
    fi
  done
done
echo "Done."
