# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchStatutSupprimeDemandesPlusDeTroisMois.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "itemBatch.jar --spring.batch.job.names=statutSupprimeDemandesPlusDeTroisMois") < 1 ]];
then
    java -jar itemBatch.jar --spring.batch.job.names=statutSupprimeDemandesPlusDeTroisMois
fi