pipeline {
    agent any

    environment {
        COMPOSE_FILE = "docker-compose.bookingservice.yml"
        TARGET_SERVICE = "bookingservice"
        TARGET_CONTAINER_NAME = "bookingservice"
        TARGET_IMAGE_NAME = "bookingservice:latest"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Docker Version') {
            steps {
                sh 'docker --version'
                sh 'docker compose version'
            }
        }

        stage('Stop Existing Container') {
            steps {
                script {

                    def existingContainer = sh(
                        script: "docker ps -aq -f name=${TARGET_CONTAINER_NAME}",
                        returnStdout: true
                    ).trim()

                    if (existingContainer) {

                        echo "Existing container found."

                        sh """
                            docker rm -f ${TARGET_CONTAINER_NAME} || true
                        """

                    } else {
                        echo "No existing container found."
                    }
                }
            }
        }

        stage('Remove Old Image') {
            steps {
                sh "docker rmi -f ${TARGET_IMAGE_NAME} || true"
            }
        }

        stage('Build Image') {
            steps {
                sh "docker compose -f ${COMPOSE_FILE} build ${TARGET_SERVICE}"
            }
        }

        stage('Start Container') {
            steps {
                sh "docker compose -f ${COMPOSE_FILE} up -d ${TARGET_SERVICE}"
            }
        }

        stage('Health Check') {
            steps {
                script {

                    sleep(time: 20, unit: 'SECONDS')

                    def healthStatus = sh(
                        script: "docker ps -q -f name=${TARGET_CONTAINER_NAME}",
                        returnStatus: true
                    )

                    if (healthStatus != 0) {

                        sh "docker logs ${TARGET_CONTAINER_NAME} || true"

                        error("Container failed to start.")

                    } else {
                        echo "✅ ${TARGET_CONTAINER_NAME} deployed successfully."
                    }
                }
            }
        }
    }

    post {
        always {
            echo '✅ Pipeline execution completed.'
        }

        failure {
            echo '❌ Deployment failed.'
        }

        success {
            echo '🚀 Deployment successful.'
        }
    }
}
