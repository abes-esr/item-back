# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierExemp.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "traiterLigneFichierExemp") < 2 ]];
then
   java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=traiterLigneFichierExemp --server.port=0
fi
