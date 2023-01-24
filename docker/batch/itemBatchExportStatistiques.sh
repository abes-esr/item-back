# dans la cron tab :
# 30 1 1 * * /home/batch/Kopya/current/bin/itemBatchExportStatistiques.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "itemBatch.jar --spring.batch.job.names=exportStatistiques") = 0 ]];
then
    java -jar itemBatch.jar --spring.batch.job.names=exportStatistiques
fi
