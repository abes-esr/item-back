#!/usr/bin/env bash

# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierExemp.sh > /dev/null 2>&1

cd /home/batch/item/current
LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "itemBatch.jar --spring.batch.job.names=traiterLigneFichierExemp") < 1 ]];
then
    echo "launch batch" > /home/batch/item/logs/item_batch_exemp_last_launch.log
    /usr/java/jdk11/bin/java -Djava.security.egd=file:///dev/urandom -jar itemBatch.jar --spring.batch.job.names=traiterLigneFichierExemp >/dev/null 2>/home/batch/item/logs/item_batch_exemp_error.log
fi
