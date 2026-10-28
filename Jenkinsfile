pipeline {
    agent any

    environment {
        DOCKERHUB_USER     = 'jdhruvr'
        NOTIFICATION_EMAIL = 'dhrramjos@gmail.com'
        IMAGE_NAME         = "${DOCKERHUB_USER}/calculator-api"
        PROXY_IMAGE        = "${DOCKERHUB_USER}/calculator-proxy"
        IMAGE_TAG          = "${BUILD_NUMBER}"
    }

    triggers {
        // Auto-trigger on GitHub webhook push, with Poll SCM fallback every 2 minutes
        githubPush()
        pollSCM('H/2 * * * *')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "✅ Checked out commit: ${env.GIT_COMMIT?.take(7)}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests -B'
                echo "✅ Build complete"
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    // Publish JUnit XML reports — visible in Jenkins UI
                    junit 'target/surefire-reports/*.xml'
                }
                failure {
                    error "❌ Tests failed — pipeline aborted"
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                    docker build \
                        -t ${IMAGE_NAME}:${IMAGE_TAG} \
                        -t ${IMAGE_NAME}:latest \
                        .
                    docker build \
                        -t ${PROXY_IMAGE}:${IMAGE_TAG} \
                        -t ${PROXY_IMAGE}:latest \
                        ./nginx
                """
                echo "✅ Images built: ${IMAGE_NAME}:${IMAGE_TAG} and ${PROXY_IMAGE}:${IMAGE_TAG}"
            }
        }

        stage('Push to Docker Hub') {
            steps {
                // Credentials stored securely in Jenkins — never hardcoded
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo "\$DOCKER_PASS" | docker login -u "\$DOCKER_USER" --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${IMAGE_NAME}:latest
                        docker push ${PROXY_IMAGE}:${IMAGE_TAG}
                        docker push ${PROXY_IMAGE}:latest
                        docker logout
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    export DOCKER_IMAGE=${IMAGE_NAME}:${IMAGE_TAG}
                    export PROXY_IMAGE=${PROXY_IMAGE}:${IMAGE_TAG}
                    docker compose down --remove-orphans || true
                    docker compose up -d --remove-orphans
                """
                // Wait for the app to become healthy before declaring success
                sh '''
                    echo "Waiting for health check..."
                    RETRIES=15
                    for i in $(seq 1 $RETRIES); do
                        STATUS=$(curl -sf http://host.docker.internal/actuator/health \
                                 || curl -sf http://localhost/actuator/health \
                                 || echo '{"status":"DOWN"}')
                        PARSED=$(echo "$STATUS" | python3 -c "import sys,json; data=json.load(sys.stdin) if not sys.stdin.isatty() else {}; print(data.get('status','DOWN'))" 2>/dev/null || echo "DOWN")
                        echo "Attempt $i/$RETRIES — status: $PARSED"
                        if [ "$PARSED" = "UP" ]; then
                            echo "✅ Deployment is healthy and returning UP"
                            exit 0
                        fi
                        sleep 4
                    done
                    echo "❌ Health check timed out"
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            script {
                try {
                    emailext(
                        subject: "✅ [SUCCESS] ${JOB_NAME} — Build #${BUILD_NUMBER}",
                        body: """
                            <h2 style="color:green;">Build Succeeded</h2>
                            <table>
                              <tr><td><b>Project</b></td><td>${JOB_NAME}</td></tr>
                              <tr><td><b>Build</b></td><td>#${BUILD_NUMBER}</td></tr>
                              <tr><td><b>Image</b></td><td>${IMAGE_NAME}:${IMAGE_TAG}</td></tr>
                              <tr><td><b>Commit</b></td><td>${env.GIT_COMMIT?.take(7)}</td></tr>
                            </table>
                            <p><a href="${BUILD_URL}">View Build Details</a></p>
                        """,
                        to: "${NOTIFICATION_EMAIL}",
                        mimeType: 'text/html'
                    )
                } catch (Exception e) {
                    echo "⚠️ Email notification skipped: ${e.message}"
                }
            }
        }
        failure {
            script {
                try {
                    emailext(
                        subject: "❌ [FAILED] ${JOB_NAME} — Build #${BUILD_NUMBER}",
                        body: """
                            <h2 style="color:red;">Build Failed</h2>
                            <table>
                              <tr><td><b>Project</b></td><td>${JOB_NAME}</td></tr>
                              <tr><td><b>Build</b></td><td>#${BUILD_NUMBER}</td></tr>
                              <tr><td><b>Commit</b></td><td>${env.GIT_COMMIT?.take(7)}</td></tr>
                            </table>
                            <p><a href="${BUILD_URL}console">View Console Output</a></p>
                        """,
                        to: "${NOTIFICATION_EMAIL}",
                        mimeType: 'text/html'
                    )
                } catch (Exception e) {
                    echo "⚠️ Email notification skipped: ${e.message}"
                }
            }
        }
        always {
            cleanWs()
        }
    }
}
