while [ true ]
do
    SIZE=`stat --printf="%s" redischeck.log`;
    if [ $SIZE -gt 10000 ]
    then
        cp redischeck.log redischeck.$(date +"%Y_%m_%d_%H_%M").log
        echo "" > redischeck.log;
    fi
    sleep 30
done &