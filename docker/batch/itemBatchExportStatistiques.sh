# dans la cron tab :
# 30 1 1 * * /home/batch/Kopya/current/bin/itemBatchExportStatistiques.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "exportStatistiques") = 0 ]];
then
    java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=exportStatistiques
fi
