if [ "$HMS_ENV" = "staging" ]
then
exec java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging

else
exec java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=development
fi

