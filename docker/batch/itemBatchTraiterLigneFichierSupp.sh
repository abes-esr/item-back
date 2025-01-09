# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierSupp.sh > /dev/null 2>&1

urlIsEnAttente="${URL_BACK}/demandes/en-attente/SUPP"
urlIsEnAttenteBigVolume="$urlIsEnAttente?bigVolume=true"
urlIsEnAttenteSmallVolume="$urlIsEnAttente?bigVolume=false"

isEnAttente=$(curl -s $urlIsEnAttente)

LANG=fr_FR.UTF-8
if [[ $isEnAttente == 'true' ]];
then
  isEnAttenteBigVolume=$(curl -s $urlIsEnAttenteBigVolume)
  isEnAttenteSmallVolume=$(curl -s $urlIsEnAttenteSmallVolume)
  if [[ $isEnAttenteBigVolume == 'true' && $(pgrep -cf "traiterLigneFichierSupp -DbigVolume=true") < 1 ]];
  then
   java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=traiterLigneFichierSupp -DbigVolume=true --server.port=0
  fi
  if [[ $isEnAttenteSmallVolume == 'true' && $(pgrep -cf "traiterLigneFichierSupp -DbigVolume=false") < 1 ]];
  then
   java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=traiterLigneFichierSupp -DbigVolume=false --server.port=0
  fi
fi
