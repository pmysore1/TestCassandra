#!/bin/bash
echo "Running docker build script"
echo "Build Number : $1"
BUILD_NUMBER=$1
#BUILD_NUMBER=27

#sudo docker tag cassandra-webapp:$1 $REPOSITORY_URI:v_$BUILD_NUMBER
#sudo docker push $REPOSITORY_URI:v_$BUILD_NUMBER

DOCKER_LOGIN=`aws ecr get-login --no-include-email --region us-east-1`
echo "$DOCKER_LOGIN"
sudo ${DOCKER_LOGIN}

#Constants

REGION=us-east-1
REPOSITORY_NAME=pradeep-ecs-repo
CLUSTER=new-pradeep-ecs-cluster
FAMILY=`sed -n 's/.*"family": "\(.*\)",/\1/p' taskdef.json`
NAME=`sed -n 's/.*"name": "\(.*\)",/\1/p' taskdef.json`
SERVICE_NAME=${NAME}-service

#Store the repositoryUri as a variable
REPOSITORY_URI=`aws ecr describe-repositories --repository-names ${REPOSITORY_NAME} --region ${REGION} | jq .repositories[].repositoryUri | tr -d '"'`
# Tag Docker Image
sudo docker tag cassandra-webapp:$BUILD_NUMBER $REPOSITORY_URI:v_$BUILD_NUMBER

# Push docker image to AWS ECR
sudo docker push $REPOSITORY_URI:v_$BUILD_NUMBER

#Replace the build number and respository URI placeholders with the constants above
sed -e "s;%BUILD_NUMBER%;${BUILD_NUMBER};g" -e "s;%REPOSITORY_URI%;${REPOSITORY_URI};g" taskdef.json > ${NAME}-v_${BUILD_NUMBER}.json

echo "family : ${FAMILY}"
echo "name : ${NAME}"
echo "service name : ${SERVICE_NAME}"
#Register the task definition in the repository

aws ecs register-task-definition --family ${FAMILY} --cli-input-json file://./${NAME}-v_${BUILD_NUMBER}.json --region ${REGION}

SERVICES=`aws ecs describe-services --services ${SERVICE_NAME} --cluster ${CLUSTER} --region ${REGION} | jq .failures[]`
echo $SERVICES

#Get latest revision
REVISION=`aws ecs describe-task-definition --task-definition ${FAMILY} --region ${REGION} | jq .taskDefinition.revision`

echo $REVISION

#Create or update service
if [ "$SERVICES" == "" ]; then
  echo "entered existing service"
  DESIRED_COUNT=`aws ecs describe-services --services ${SERVICE_NAME} --cluster ${CLUSTER} --region ${REGION} | jq .services[].desiredCount`
  if [ ${DESIRED_COUNT} = "0" ]; then
    DESIRED_COUNT="1"
  fi
  aws ecs update-service --cluster ${CLUSTER} --region ${REGION} --service ${SERVICE_NAME} --task-definition ${FAMILY}:${REVISION} --desired-count ${DESIRED_COUNT}
else
  echo "entered new service"
  aws ecs create-service --service-name ${SERVICE_NAME} --desired-count 1 --task-definition ${FAMILY} --cluster ${CLUSTER} --region ${REGION}
fi
