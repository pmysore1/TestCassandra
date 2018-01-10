pipeline {
    agent {label 'jenkins-slave-docker'}
    stages
    {
            stage('Checkout SCM') {
                steps {
                    checkout([$class: 'GitSCM', branches: [[name: '*/hotfix']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/pmysore1/TestCassandra.git']]])
                }
            }
            
            stage('Test') {
                    steps {
                        echo 'Testing'
                    }
                    post {
                        always {
                        junit 'build/reports/**/*.xml'
                    }
            }
                }
            }
            
            stage('Build') {
                steps {
                    echo 'Building..'
                    sh '/home/jenkins/apache-maven-3.5.2/bin/mvn -Pdev clean package'
                    //sh '/home/jenkins/apache-maven-3.5.2/bin/mvn -Ptest package'
                }
                post{
                    success{
                        echo 'Archiving.....,'
                        archiveArtifacts artifacts: '**/target/*.war'
                        //sh 'sudo docker build -t cassandra-webapp .'
                        sh 'chmod +x build_docker.sh'
                        sh './build_docker.sh ${BUILD_NUMBER}'
                    }
                }
        }
    }
}