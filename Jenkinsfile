pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    REGISTRY = "local"
    IMAGE_BACKEND = "devsecops-backend"
    IMAGE_FRONTEND = "devsecops-frontend"
    COMMIT_SHA = "${env.GIT_COMMIT?.take(7) ?: 'local'}"
    SONARQUBE_SERVER = "sonarqube"
    OWASP_FAIL_CVSS = "7"
    TRIVY_SEVERITY = "HIGH,CRITICAL"
  }

  stages {
    stage("Checkout") {
      steps {
        checkout scm
      }
    }

    stage("Secrets Scan (Gitleaks)") {
      steps {
        sh """
          mkdir -p build/security
          gitleaks detect --source . --report-format json --report-path build/security/gitleaks-report.json --redact --exit-code 1
        """
      }
    }

    stage("SAST Scan (SonarQube)") {
      steps {
        withSonarQubeEnv("${SONARQUBE_SERVER}") {
          withCredentials([string(credentialsId: 'sonarqube_token', variable: 'SONAR_TOKEN')]) {
            sh "mvn -f backend/pom.xml -DskipTests sonar:sonar -Dsonar.token=${SONAR_TOKEN}"
          }
        }
      }
    }

    stage("Quality Gate") {
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage("Build Backend") {
      steps {
        sh "mvn -f backend/pom.xml clean install"
      }
    }

    stage("Run Unit Tests") {
      steps {
        sh "mvn -f backend/pom.xml test"
      }
    }

    stage("Build Docker Images") {
      parallel {
        failFast true
        stage("Backend Image") {
          steps {
            sh "docker build -t ${IMAGE_BACKEND}:latest -t ${IMAGE_BACKEND}:${COMMIT_SHA} backend"
          }
        }
        stage("Frontend Image") {
          steps {
            sh "docker build --build-arg NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 -t ${IMAGE_FRONTEND}:latest -t ${IMAGE_FRONTEND}:${COMMIT_SHA} ecommerce-app"
          }
        }
      }
    }

    stage("Dependency Scan (OWASP)") {
      steps {
        withCredentials([string(credentialsId: 'nvd_api_key', variable: 'NVD_API_KEY')]) {
          sh "mvn -f backend/pom.xml -DskipTests org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=${OWASP_FAIL_CVSS}"
        }
      }
    }

    stage("Container Scan (Trivy)") {
      steps {
        sh """
          mkdir -p build/security
          trivy image --severity ${TRIVY_SEVERITY} --ignore-unfixed --exit-code 1 --format json --output build/security/trivy-backend.json ${IMAGE_BACKEND}:${COMMIT_SHA}
          trivy image --severity ${TRIVY_SEVERITY} --ignore-unfixed --exit-code 1 --format json --output build/security/trivy-frontend.json ${IMAGE_FRONTEND}:${COMMIT_SHA}
        """
      }
    }

    stage("Deploy") {
      steps {
        withCredentials([
          string(credentialsId: 'postgres_password', variable: 'POSTGRES_PASSWORD'),
          string(credentialsId: 'redis_password', variable: 'REDIS_PASSWORD'),
          string(credentialsId: 'jwt_secret', variable: 'APP_JWT_SECRET')
        ]) {
          writeFile file: '.env.ci', text: """POSTGRES_DB=vectortech
POSTGRES_USER=vector_user
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
POSTGRES_PORT=5432
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASSWORD}
BACKEND_PORT=8080
DB_HOST=db
DB_PORT=5432
DB_USER=vector_user
DB_PASSWORD=${POSTGRES_PASSWORD}
APP_JWT_SECRET=${APP_JWT_SECRET}
APP_JWT_EXPIRATION_MS=86400000
FRONTEND_PORT=3000
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
"""
        }

        sh "docker compose --env-file .env.ci up -d --build"
      }
    }
  }

  post {
    success {
      echo "Pipeline completed successfully."
    }
    failure {
      echo "Pipeline failed. Check logs for details."
    }
    always {
      publishHTML(target: [
        reportDir: 'backend/target',
        reportFiles: 'dependency-check-report.html',
        reportName: 'OWASP Dependency-Check',
        allowMissing: true,
        keepAll: true,
        alwaysLinkToLastBuild: true
      ])
      archiveArtifacts artifacts: 'build/security/*.json', allowEmptyArchive: true
    }
    cleanup {
      sh "rm -f .env.ci"
    }
  }
}
