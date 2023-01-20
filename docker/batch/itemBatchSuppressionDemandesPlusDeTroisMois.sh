#!/usr/bin/env bash

# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchSuppressionDemandesPlusDeTroisMois.sh > /dev/null 2>&1

cd /home/batch/item/current
LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "itemBatch.jar --spring.batch.job.names=suppressionDemandesPlusDeTroisMois") < 1 ]];
then
    echo "launch batch" > /home/batch/item/logs/item_batch_suppression_demandes_last_launch.log
    /usr/java/jdk11/bin/java -Djava.security.egd=file:///dev/urandom -jar itemBatch.jar --spring.batch.job.names=suppressionDemandesPlusDeTroisMois >/dev/null 2>/home/batch/item/logs/item_batch_suppression_demandes_error.log
fi
