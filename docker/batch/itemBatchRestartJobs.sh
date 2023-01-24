# dans la cron tab :
# * * * * * /home/batch/item/current/bin/itemBatchRestartJobs.sh > /dev/null 2>&1

LANG=fr_FR.UTF-8
if [[ $(pgrep -cf "item-batch.jar --spring.batch.job.names=redemarrageJobsUnknown") < 1 ]];
then
    java -jar item-batch.jar --spring.batch.job.names=redemarrageJobsUnknown
fi
