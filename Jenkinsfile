pipeline {
    agent any

    environment {
        IMAGE_NAME = "bookingservice"
        CONTAINER_NAME = "bookingservice"
        DOCKER_NETWORK = "updated_orgadmin_rmscadminnetwork"
        HOST_PORT = "9001"
        CONTAINER_PORT = "9001"
        DOCKER_BUILDKIT = "0"
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
            }
        }

        stage('Ensure Docker Network') {
            steps {
                sh """
                    docker network inspect ${DOCKER_NETWORK} >/dev/null 2>&1 || \
                    docker network create ${DOCKER_NETWORK}
                """
            }
        }

        stage('Clean Old Container and Image') {
            steps {
                sh """
                    docker rm -f ${CONTAINER_NAME} || true
                    docker rmi -f ${IMAGE_NAME}:latest || true
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    DOCKER_BUILDKIT=0 docker build --no-cache -t ${IMAGE_NAME}:latest .
                """
            }
        }

        stage('Run Container') {
            steps {
                sh """
                    docker run -d --name ${CONTAINER_NAME} \
                        --restart unless-stopped \
                        -p ${HOST_PORT}:${CONTAINER_PORT} \
                        --network ${DOCKER_NETWORK} \
                        -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://adminserviceregistry:8761/eureka/ \
                        -e EUREKA_CLIENT_REGISTER_WITH_EUREKA=true \
                        -e EUREKA_CLIENT_FETCH_REGISTRY=true \
                        -e EUREKA_INSTANCE_LEASE_RENEWAL_INTERVAL_IN_SECONDS=10 \
                        -e EUREKA_INSTANCE_LEASE_EXPIRATION_DURATION_IN_SECONDS=30 \
                        -e MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,eureka,shutdown \
                        -e SPRING_DATASOURCE_URL='jdbc:mysql://erp-mysql:3306/hms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
                        -e SPRING_DATASOURCE_USERNAME=root \
                        -e SPRING_DATASOURCE_PASSWORD=root \
                        -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                        ${IMAGE_NAME}:latest
                """
            }
        }

        stage('Health Check') {
            steps {
                sh """
                    sleep 20
                    docker ps -q -f name=${CONTAINER_NAME} | grep . || \
                    (docker logs ${CONTAINER_NAME} || true && exit 1)
                """
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
