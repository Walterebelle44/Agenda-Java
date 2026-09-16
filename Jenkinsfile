pipeline {
    agent any

    // Agent Windows détecté (C:\ProgramData\Jenkins\...) : ce pipeline
    // utilise donc "bat" au lieu de "sh". Si un JDK "JDK17" est bien
    // configuré et fonctionnel dans Manage Jenkins > Tools, tu peux
    // décommenter le bloc "tools" ci-dessous. Sinon (le plus simple),
    // installe le JDK toi-même sur la machine Windows et laisse ce
    // bloc "tools" commenté : le pipeline utilisera le java/javac déjà
    // présent dans le PATH Windows.
    //
    // tools {
    //     jdk 'JDK17'
    // }

    environment {
        SRC_DIR    = 'src'
        BUILD_DIR  = 'bin'
        JAR_NAME   = 'Agenda.jar'
        MAIN_CLASS = 'Agenda'
    }

    options {
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

        stage('Check Java') {
            steps {
                echo 'Vérification de la présence de Java sur l\'agent...'
                bat 'java -version'
                bat 'javac -version'
            }
        }

        stage('Clean') {
            steps {
                echo 'Nettoyage du répertoire de build...'
                bat "if exist ${BUILD_DIR} rmdir /S /Q ${BUILD_DIR}"
                bat "mkdir ${BUILD_DIR}"
            }
        }

        stage('Compile') {
            steps {
                echo 'Compilation du projet Java...'
                bat "javac -d ${BUILD_DIR} ${SRC_DIR}\\*.java"
            }
        }

        stage('Package') {
            steps {
                echo 'Création du fichier JAR exécutable...'
                dir("${BUILD_DIR}") {
                    bat """
                        echo Main-Class: ${MAIN_CLASS}> manifest.txt
                        jar cfm ${JAR_NAME} manifest.txt *.class
                    """
                }
                bat "copy ${BUILD_DIR}\\${JAR_NAME} ."
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
    }
}