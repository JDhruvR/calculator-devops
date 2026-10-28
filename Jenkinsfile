pipeline {
    agent any

    environment {
        // ── Change these two values to your own ──────────────────────────────
        DOCKERHUB_USER     = 'your-dockerhub-username'
        NOTIFICATION_EMAIL = 'your-email@example.com'
        // ────────────────────────────────────────────────────────────────────
        IMAGE_NAME = "${DOCKERHUB_USER}/calculator-api"
        IMAGE_TAG  = "${BUILD_NUMBER}"
    }

    triggers {
        // Auto-trigger on every GitHub push (requires webhook configured in GitHub)
        githubPush()
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
                """
                echo "✅ Image built: ${IMAGE_NAME}:${IMAGE_TAG}"
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
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${IMAGE_NAME}:latest
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    # Export image name so docker-compose.yml picks it up
                    export DOCKER_IMAGE=${IMAGE_NAME}:${IMAGE_TAG}

                    # Pull the new image and redeploy — idempotent: safe to re-run
                    docker compose pull
                    docker compose up -d --remove-orphans
                """
                // Wait for the app to become healthy before declaring success
                sh '''
                    echo "Waiting for health check..."
                    RETRIES=10
                    for i in $(seq 1 $RETRIES); do
                        STATUS=$(curl -sf http://localhost/actuator/health \
                                 | python3 -c "import sys,json; print(json.load(sys.stdin).get('status','DOWN'))" \
                                 2>/dev/null || echo "DOWN")
                        echo "Attempt $i/$RETRIES — status: $STATUS"
                        if [ "$STATUS" = "UP" ]; then
                            echo "✅ Deployment is healthy"
                            exit 0
                        fi
                        sleep 10
                    done
                    echo "❌ Health check timed out"
                    exit 1
                '''
            }
        }
    }

    post {
        success {
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
        }
        failure {
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
        }
        always {
            // Clean workspace after each run to keep the agent disk usage low
            cleanWs()
        }
    }
}
