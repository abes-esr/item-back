# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierSupp.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "item-batch.jar --spring.batch.job.names=traiterLigneFichierSupp") < 1 ]];
then
    java -jar -XX:MaxRAMPercentage=95 /scripts/item-batch.jar --spring.batch.job.names=traiterLigneFichieSupp --server.port=8085
fi
