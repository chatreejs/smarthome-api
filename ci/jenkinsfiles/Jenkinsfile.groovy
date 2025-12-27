pipeline {
  agent any

  environment {
    VERSION = "0.1.0"
    IMAGE_URL = "chatreejs/smarthome-api"
  }

  stages {
    stage('Prepare Env') {
      steps {
        script {
          def now = new Date()
          env.BUILD_DATE = now.format("yyyyMMdd")
          env.BUILD_VERSION = "${VERSION}-${BUILD_DATE}-${BUILD_NUMBER}"
        }
      }
    }

    stage('Static Code Scan') {
      agent {
        docker {
          image 'sonarsource/sonar-scanner-cli:latest'
          args '-v $PWD:/workspace -w /workspace -u root --privileged'
        }
      }
      steps {
        withSonarQubeEnv('SonarQube Server') {
          sh 'sonar-scanner'
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          currentBuild.displayName += " version ${BUILD_VERSION}"
        }
        sh 'docker build -f Dockerfile . -t ${IMAGE_URL}:${BUILD_VERSION}'
      }
    }

    stage('Push to registry') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-hub-credential', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
          sh 'docker login -u $USERNAME -p $PASSWORD'
          sh 'docker push ${IMAGE_URL}:${BUILD_VERSION}'
        }
      }
    }

    stage('Clear Image') {
      steps {
        sh 'docker rmi ${IMAGE_URL}:${BUILD_VERSION}'
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        build job: "chatreejs/gitops/smarthome-manifest-${ENV}", parameters: [string(name: 'API_IMAGE_TAG', value: "${BUILD_VERSION}")]
      }
    }

  }

  post {
    success {
      discordSend description: "Duration: ${currentBuild.durationString}", link: env.BUILD_URL, result: currentBuild.currentResult, title: "${JOB_NAME} - # ${BUILD_VERSION}", footer: "${currentBuild.getBuildCauses()[0].shortDescription}", webhookURL: "https://discord.com/api/webhooks/${DISCORD_WEBHOOK_ID}/${DISCORD_WEBHOOK_TOKEN}"
    }
    failure {
      discordSend description: "Duration: ${currentBuild.durationString}", link: env.BUILD_URL, result: currentBuild.currentResult, title: "${JOB_NAME} - # ${BUILD_VERSION}", footer: "${currentBuild.getBuildCauses()[0].shortDescription}", webhookURL: "https://discord.com/api/webhooks/${DISCORD_WEBHOOK_ID}/${DISCORD_WEBHOOK_TOKEN}"
    }
  }

}
