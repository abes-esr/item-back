#!/usr/bin/env bash

# dans la cron tab :
# 30 1 1 * * /home/batch/Kopya/current/bin/itemBatchExportStatistiques.sh > /dev/null 2>&1

cd /home/batch/item/current
LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "itemBatch.jar --spring.batch.job.names=exportStatistiques") = 0 ]];
then
    echo "launch batch" > /home/batch/item/logs/item_batch_export_statistiques_last_launch.log
    /usr/java/jdk11/bin/java -Djava.security.egd=file:///dev/urandom -jar itemBatch.jar --spring.batch.job.names=exportStatistiques --server.port=8081 >/dev/null 2>/home/batch/item/logs/item_batch_export_statistiques_error.log
fi
