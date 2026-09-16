pipeline {
    agent any

    tools {
        // Adaptez le nom à celui configuré dans Jenkins > Global Tool Configuration
        jdk 'JDK17'
    }

    environment {
        SRC_DIR   = 'src'
        BUILD_DIR = 'bin'
        JAR_NAME  = 'Agenda.jar'
        MAIN_CLASS = 'Agenda'
    }

    options {
        // Garde uniquement les 10 derniers builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Récupération du code source...'
                checkout scm
            }
        }

        stage('Clean') {
            steps {
                echo 'Nettoyage du répertoire de build...'
                sh "rm -rf ${BUILD_DIR}"
                sh "mkdir -p ${BUILD_DIR}"
            }
        }

        stage('Compile') {
            steps {
                echo 'Compilation du projet Java...'
                sh "javac -d ${BUILD_DIR} ${SRC_DIR}/*.java"
            }
        }

        stage('Package') {
            steps {
                echo 'Création du fichier JAR exécutable...'
                dir("${BUILD_DIR}") {
                    sh """
                        echo "Main-Class: ${MAIN_CLASS}" > manifest.txt
                        jar cfm ${JAR_NAME} manifest.txt *.class
                    """
                }
                sh "cp ${BUILD_DIR}/${JAR_NAME} ."
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'Archivage des artefacts générés...'
                archiveArtifacts artifacts: "${JAR_NAME}, ${BUILD_DIR}/**/*.class", fingerprint: true
            }
        }
    }

    post {
        success {
            echo "✅ Build réussi : ${JAR_NAME} est prêt."
        }
        failure {
            echo '❌ Le build a échoué. Consultez les logs ci-dessus.'
        }
        always {
            cleanWs(patterns: [[pattern: "${BUILD_DIR}/manifest.txt", type: 'INCLUDE']])
        }
    }
}