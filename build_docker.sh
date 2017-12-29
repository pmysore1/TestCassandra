#!/bin/bash
echo "Running docker build script"
echo "$1"
#docker stop $(docker ps -a -q)
#docker rm $(docker ps -a -q)

sudo docker stop cassandra-webapp
sudo docker rm -f cassandra-webapp
sudo docker rmi -f cassandra-webapp
sudo docker build -t $1 cassandra-webapp .
sudo docker run -d -p 8080:8080 --name cassandra-webapp cassandra-webapp

