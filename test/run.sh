cd $(dirname $0)
cd ..
mvn clean test javadoc:javadoc -Dcucumber.options="--tags @hello,@bonjour,@blog,@playToLogoGame,@jouerAuJeuDesLogos" -PscenarioInitiator,javadoc,unit-tests -Dmaven.test.failure.ignore=true

gem install travis

travis logs

echo "NORA-UI is ready"

rm -rf target

exit
