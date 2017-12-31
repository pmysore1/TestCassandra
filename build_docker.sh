#!/bin/bash
echo "Running docker build script"
echo "$1"
#docker stop $(docker ps -a -q)
#docker rm $(docker ps -a -q)



#docker rm $(docker stop $(docker ps -a -q --filter ancestor=<image-name> --format="{{.ID}}"))
#docker rmi $(docker ps -q --filter ancestor=cassandra-webapp )


sudo docker stop cassandra-webapp || true
sudo docker rm -f cassandra-webapp || true
for docker_images in $( sudo docker images | grep cassandra-webapp | awk '{print $3}'); do
    #sudo docker rm -f cassandra-webapp || true     
    sudo docker rmi -f $docker_images || true     
done
#sudo docker rmi -f cassandra-webapp || true
sudo docker build -t cassandra-webapp:$1 .
sudo docker run -d -p 8080:8080 -e JAVA_OPTS='-Ddev-yy' --name cassandra-webapp cassandra-webapp:$1

-e JAVA_OPTS='-Xmx1g'

