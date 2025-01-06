# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierExemp.sh > /dev/null 2>&1
url="${URL_BACK}/demandes/en-attente/EXEMP"

isEnAttente=$(curl -s $url)

LANG=fr_FR.UTF-8
if [[ $isEnAttente == 'true' &&  $(pgrep -cf "traiterLigneFichierExemp") < 2 ]];
then
   java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=traiterLigneFichierExemp --server.port=0
fi
