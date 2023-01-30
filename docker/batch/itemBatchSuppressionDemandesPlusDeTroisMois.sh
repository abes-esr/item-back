# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchSuppressionDemandesPlusDeTroisMois.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "item-batch.jar --spring.batch.job.names=suppressionDemandesPlusDeTroisMois") < 1 ]];
then
    java -jar /scripts/item-batch.jar --spring.batch.job.names=suppressionDemandesPlusDeTroisMois
fi
