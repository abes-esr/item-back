# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierExemp.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "item-batch.jar --spring.batch.job.name=traiterLigneFichierExemp") < 1 ]];
then
   java -jar -XX:MaxRAMPercentage=80 -XX:+UseG1GC -Xshare:on -XX:+UseCompressedOops -Xmx1024m /scripts/item-batch.jar --spring.batch.job.name=traiterLigneFichierExemp --server.port=8082
fi
