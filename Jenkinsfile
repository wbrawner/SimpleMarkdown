pipeline {
    agent any
    stages {
        stage('Config') {
            steps {
                configFileProvider([
                        configFile(
                                fileId: 'ce0fea29-ab06-4921-8ff9-11b09ec8705b',
                                variable: 'ACRA_PROPERTIES_FILE'
                        )
                ]) {
                    sh 'cp "$ACRA_PROPERTIES_FILE" app/acra.properties'
                }
                configFileProvider([
                        configFile(
                                fileId: '9f7d74a1-0971-41b7-8984-ac875ad6301f',
                                variable: 'KEYSTORE_PROPERTIES'
                        )
                ]) {
                    sh 'cp "$KEYSTORE_PROPERTIES" keystore.properties'
                }
            }
        }
        stage('Build') {
            steps {
                sh './gradlew assembleRelease test'
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts 'app/build/outputs/apk/*/release/*.apk'
                archiveArtifacts 'app/build/outputs/mapping/**'
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
