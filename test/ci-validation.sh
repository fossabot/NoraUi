echo "******** start log *****"
curl -s "https://api.travis-ci.org/jobs/${TRAVIS_JOB_ID}/log.txt?deansi=true" > nonaui.log
cat nonaui.log

echo "******** count log *****"

sed -n ':;s/INFO//p;t' nonaui.log | sed -n '$='




echo "******** stop log *****"
