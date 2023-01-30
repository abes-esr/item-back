# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierExemp.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "item-batch.jar --spring.batch.job.names=traiterLigneFichierExemp") < 1 ]];
then
   java -jar /scripts/item-batch.jar --spring.batch.job.names=traiterLigneFichierExemp
fi
