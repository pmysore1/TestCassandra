#!/bin/bash
echo "Running docker build script"
echo "$1"
DOCKER_LOGIN=`aws ecr get-login --no-include-email --region us-east-1`
${DOCKER_LOGIN}

sudo docker tag cassandra-webapp:$1 468913107500.dkr.ecr.us-east-1.amazonaws.com/pradeep-ecs-repo:cassandra-webapp.latest
sudo docker push 468913107500.dkr.ecr.us-east-1.amazonaws.com/pradeep-ecs-repo:cassandra-webapp.latest



