# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchStatutSupprimeDemandesPlusDeTroisMois.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "item-batch.jar --spring.batch.job.names=statutSupprimeDemandesPlusDeTroisMois") < 1 ]];
then
    java -jar /scripts/item-batch.jar --spring.batch.job.names=statutSupprimeDemandesPlusDeTroisMois
fi