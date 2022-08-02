echo "Hello, I am clean_docker.sh file for forex_to_db java/SB/postgreSQL DB/docker app by juhav."
echo ""
echo "I was started by user $(whoami)"
echo ""
echo "I remove the old database contents from forex_db"
echo "I remove the old images from docker-compose.up"
echo ""
docker stop forex_to_db
docker rm forex_to_db
docker rmi -f docker-spring-boot-postgres
docker stop forex_db
#docker volume rm docker_forex_vol
#docker rmi postgres
echo "output from \"docker volume ls\" :"
docker volume ls
echo "output from \"docker images\" :" 
docker images
echo "output from \"docker ps -a\" :"
docker ps -a
cp ~/eclipse-space/forex_to_db/target/forex_to_db.jar ~/eclipse-space/forex_to_db/src/main/docker/
echo "output from ls -l :"
ls -l  ~/eclipse-space/forex_to_db/src/main/docker/

