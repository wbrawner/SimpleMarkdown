pipeline {
    agent any
    stages {
        stage('Config') {
            steps {
                withCredentials([file(
                        credentialsId: '23ecf53c-2fc9-41ed-abfa-0b32d60d688f',
                        variable     : 'ACRA_PROPERTIES_FILE'
                )]) {
                    sh 'cp "$ACRA_PROPERTIES_FILE" app/acra.properties'
                }
            }
        }
        stage('Build') {
            steps {
                sh './gradlew assembleRelease test'
            }
        }
        stage('Sign & Archive') {
            steps {
                [
                        $class    : 'SignApksBuilder',
                        apksToSign: 'app/build/outputs/apk/*/release/*.apk',
                        keyAlias  : 'simplemarkdown',
                        keyStoreId: '44651a2a-1e46-4708-80ab-d8befc6e94f0'
                ]
                archiveArtifacts 'app/build/outputs/mapping'
            }
        }
        stage('Report') {
            steps {
                junit '**/build/test-results/**/*.xml'
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
