pipeline {
    agent {label 'jenkins-slave-docker'}
    stages
    {
        stage('Build') {
                steps {
                    echo 'Building..'
                    sh '/home/jenkins/apache-maven-3.5.2/bin/mvn clean package'
                }
                post{
                    success{
                        echo 'Archiving.....,'
                        archiveArtifacts artifacts: '**/target/*.war'
                        sh 'sudo docker build -t cassandra-webapp .'
                    }
                }
        }
    }
}