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
        SRC_DIR       = 'src'
        TEST_DIR      = 'src\\test'
        BUILD_DIR     = 'bin'
        TEST_BUILD_DIR = 'bin-test'
        JAR_NAME      = 'Agenda.jar'
        MAIN_CLASS    = 'Agenda'
        NOTIFY_EMAIL  = 'walterebelle4@gmail.com'
        JUNIT_JAR     = 'junit-platform-console-standalone.jar'
        JUNIT_URL     = 'https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar'
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

        stage('Download JUnit') {
            steps {
                echo 'Téléchargement de JUnit (si absent)...'
                bat """
                    if not exist ${JUNIT_JAR} (
                        curl -L -o ${JUNIT_JAR} "${JUNIT_URL}"
                    )
                """
            }
        }

        stage('Test') {
            steps {
                echo 'Compilation et exécution des tests unitaires...'
                bat "if exist ${TEST_BUILD_DIR} rmdir /S /Q ${TEST_BUILD_DIR}"
                bat "mkdir ${TEST_BUILD_DIR}"
                bat "javac -cp ${BUILD_DIR};${JUNIT_JAR} -d ${TEST_BUILD_DIR} ${TEST_DIR}\\*.java"
                bat "java -jar ${JUNIT_JAR} execute --class-path ${BUILD_DIR};${TEST_BUILD_DIR} --scan-classpath --details=tree"
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
            mail to: "${NOTIFY_EMAIL}",
                 subject: "✅ SUCCÈS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """Bonjour,

Le build ${env.BUILD_NUMBER} du job ${env.JOB_NAME} a réussi.

Voir les détails et télécharger le JAR :
${env.BUILD_URL}

-- Jenkins"""
        }
        failure {
            echo '❌ Le build a échoué. Consultez les logs ci-dessus.'
            mail to: "${NOTIFY_EMAIL}",
                 subject: "❌ ÉCHEC - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """Bonjour,

Le build ${env.BUILD_NUMBER} du job ${env.JOB_NAME} a échoué.

Voir les logs :
${env.BUILD_URL}console

-- Jenkins"""
        }
    }
}