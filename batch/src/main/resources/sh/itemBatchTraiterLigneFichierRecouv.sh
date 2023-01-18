#!/usr/bin/env bash

# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierRecouv.sh > /dev/null 2>&1

cd /home/batch/item/current
LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "itemBatch.jar --spring.batch.job.names=traiterLigneFichierRecouv") < 1 ]];
then
    echo "launch batch" > /home/batch/item/logs/item_batch_recouv_last_launch.log
    /usr/java/jdk11/bin/java -Djava.security.egd=file:///dev/urandom -jar itemBatch.jar --spring.batch.job.names=traiterLigneFichierRecouv >/dev/null 2>/home/batch/item/logs/item_batch_recouv_error.log
fi
