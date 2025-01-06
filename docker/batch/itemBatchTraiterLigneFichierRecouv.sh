# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierRecouv.sh > /dev/null 2>&1
url="${URL_BACK}/demandes/en-attente/RECOUV"

isEnAttente=$(curl -s $url)

LANG=fr_FR.UTF-8
if [[ $isEnAttente == 'true' && $(pgrep -cf "traiterLigneFichierRecouv") < 2 ]];
then
    java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=traiterLigneFichierRecouv --server.port=0
fi
