# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchStatutSupprimeDemandesPlusDeTroisMois.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "statutSupprimeDemandesPlusDeTroisMois") < 1 ]];
then
    java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=statutSupprimeDemandesPlusDeTroisMois
fi