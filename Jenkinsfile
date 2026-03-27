pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    PATH = "/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
    CI = 'true'
    PORT = '4310'
    APP_BASE_URL = 'http://127.0.0.1:4310'
    USE_FIRESTORE = 'false'
    OPS_API_KEY = 'jenkins-ci-key'
    SMTP_HOST = 'localhost'
    SMTP_PORT = '1025'
    SMTP_SECURE = 'false'
    SMTP_USER = ''
    SMTP_PASS = ''
    MAIL_FROM = 'ci@example.com'
    MAIL_TO = 'ci@example.com'
  }

  stages {
    stage('Verify Node Toolchain') {
      steps {
        sh 'echo "PATH=$PATH"'
        sh 'node -v'
        sh 'npm -v'
      }
    }

    stage('Install Dependencies') {
      steps {
        sh 'npm ci'
        sh 'npm --prefix frontend ci'
      }
    }

    stage('Run Frontend Unit Tests') {
      steps {
        sh 'npm --prefix frontend run test -- --watch=false'
      }
    }

    stage('Build Frontend') {
      steps {
        sh 'npm run build'
      }
    }

    stage('Backend Smoke Test') {
      steps {
        sh '''
          set -eu
          rm -f backend-smoke.log health.json
          node index.js > backend-smoke.log 2>&1 &
          server_pid=$!

          cleanup() {
            kill "$server_pid" >/dev/null 2>&1 || true
          }
          trap cleanup EXIT

          i=0
          until [ "$i" -ge 30 ]
          do
            if curl -fsS "http://127.0.0.1:${PORT}/api/health" > health.json; then
              break
            fi
            i=$((i + 1))
            sleep 1
          done

          test -s health.json
        '''
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'frontend/dist/**,health.json,backend-smoke.log', allowEmptyArchive: true
    }
  }
}
