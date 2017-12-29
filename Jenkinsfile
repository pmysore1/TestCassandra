pipeline {
    agent {label 'jenkins-slave-docker'}
    stage('Build') {
            steps {
                echo 'Building..'
                sh 'mvn clean package'
            }
            post{
                success{
                    echo 'Archiving.....,'
                    archiveArtifacts artifacts: '**/target/*.war'
                }
            }
        }
}