pipeline {
    agent any

    environment {
      IMAGE_NAME = "kopilnagi/june-batch-cicd"
      IMAGE_TAG = "${BUILD_NUMBER}"
    }


    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/kapilnagi007/June-StaticWebsiteHost.git'
            }
        }

        stage('Debug') {
            steps{
                dir('june-batch-cicd') {
                    sh 'pwd'
                    sh 'ls -la'
                    sh 'find . -name package.json'
                    sh 'node -v'
                    sh 'npm -v'
                }
            }
        }

        stage('Install Dependencies') {
            steps{
                dir('june-batch-cicd') {
                    sh 'npm install'
                }
            }
        }

        stage('SonarQube Analysis'){
            steps{
                dir('june-batch-cicd'){
                    script{
                    def scannerHome = tool (
                      name: 'sonar-scanner',
                      type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                      )

                    withSonarQubeEnv('sonarqube') {
                      sh "${scannerHome}/bin/sonar-scanner"
                    }
                  }
                }
            }
        }

        stage('Quality Gate'){
            steps{
                dir('june-batch-cicd'){
                    timeout(time: 5, unit: 'MINUTES') {
                      waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        stage('Build Application') {
            steps{
                dir('june-batch-cicd') {
                    sh 'npm run build'    
                }
            }
        }

        stage('Test Application') {
            steps{
                dir('june-batch-cicd') {
                    sh 'npm test -- --watchAll==false'    
                }
            }
        }

        stage('Build Docker Image'){
            steps{
                dir('june-batch-cicd') {
                    sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG .'
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                sh """
                mkdir -p \$WORKSPACE/.trivycache
        
                docker run --rm \
                -v \$WORKSPACE/.trivycache:/root/.cache/trivy \
                -v /var/run/docker.sock:/var/run/docker.sock \
                aquasec/trivy:latest image \
                --scanners vuln \
                --severity HIGH,CRITICAL \
                kopilnagi/june-batch-cicd:${BUILD_NUMBER}
                """
            }
        }

        stage('Push Image') {
          steps {
            dir('june-batch-cicd') {
              withCredentials([
                  usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                  )
                ])
                {
                  sh '''
                  echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                  docker push $IMAGE_NAME:$IMAGE_TAG
                  '''
                }  
            }
            
          }
        }

        stage('Deploy'){
          steps {
            dir('june-batch-cicd') {
                sh '''
                docker stop react-app || true
                docker rm react-app || true

                docker run -d --name react-app -p 80:80 $IMAGE_NAME:$IMAGE_TAG
                '''
            }
          }
        }
    }
}
