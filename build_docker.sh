#!/bin/bash
echo "Running docker build script"
existing = $(sudo docker ps | grep cassandra-webapp | grep -o "^[0-9a-z]*")  
if [ ! -z "$existing" ]; then  
  sudo docker stop $existing
  sudo docker rm $existing
fi

sudo docker build -t cassandra-webapp .
sudo docker run -d -p 8080:8080 cassandra-webapp

