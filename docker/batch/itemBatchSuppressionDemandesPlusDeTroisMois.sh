# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchSuppressionDemandesPlusDeTroisMois.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "suppressionDemandesPlusDeTroisMois") < 1 ]];
then
    java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=suppressionDemandesPlusDeTroisMois
fi
