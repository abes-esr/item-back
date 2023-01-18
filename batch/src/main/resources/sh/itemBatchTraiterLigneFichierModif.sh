#!/usr/bin/env bash

# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierModif.sh > /dev/null 2>&1

cd /home/batch/item/current
LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "itemBatch.jar --spring.batch.job.names=traiterLigneFichierModif") < 1 ]];
then
    echo "launch batch" > /home/batch/item/logs/item_batch_modif_last_launch.log
    /usr/java/jdk11/bin/java -Djava.security.egd=file:///dev/urandom -jar itemBatch.jar --spring.batch.job.names=traiterLigneFichierModif >/dev/null 2>/home/batch/item/logs/item_batch_modif_error.log
fi
