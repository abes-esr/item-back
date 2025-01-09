# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchTraiterLigneFichierExemp.sh > /dev/null 2>&1
urlIsEnAttente="${URL_BACK}/demandes/en-attente/EXEMP"
urlIsEnAttenteBigVolume="$urlIsEnAttente?bigVolume=true"
urlIsEnAttenteSmallVolume="$urlIsEnAttente?bigVolume=false"

isEnAttente=$(curl -s $urlIsEnAttente)

LANG=fr_FR.UTF-8
if [[ $isEnAttente == 'true' ]];
then
  isEnAttenteBigVolume=$(curl -s $urlIsEnAttenteBigVolume)
  isEnAttenteSmallVolume=$(curl -s $urlIsEnAttenteSmallVolume)
  if [[ $isEnAttenteBigVolume == 'true' && $(pgrep -cf "traiterLigneFichierExemp --bigVolume=true") < 1 ]];
  then
   java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=traiterLigneFichierExemp --bigVolume=true --server.port=0 &
  fi
  if [[ $isEnAttenteSmallVolume == 'true' && $(pgrep -cf "traiterLigneFichierExemp --bigVolume=false") < 1 ]];
  then
   java -XX:MaxRAMPercentage=80 org.springframework.boot.loader.JarLauncher --spring.batch.job.name=traiterLigneFichierExemp --bigVolume=false --server.port=0 &
  fi
fi
