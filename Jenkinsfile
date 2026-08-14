/*
 * Pipeline de testes automatizados (TestNG + RestAssured) para a API
 * criar-cenario-testes.
 *
 * COMO ESTE JOB É DISPARADO
 * Este Jenkinsfile vive no repositório de TESTES, mas precisa reagir a
 * merges na branch `develop` do repositório da API (outro repositório).
 * Jenkins não sabe "escutar" pushes de um repo dentro do Jenkinsfile de
 * outro — isso é configuração do JOB, não do Pipeline script. Duas
 * opções, da mais simples para a mais desacoplada:
 *
 *   1) [Recomendado] Downstream trigger: ao final do Jenkinsfile da API
 *      (criar-cenario-testes/Jenkinsfile), quando a branch for `develop`,
 *      chamar:
 *          build job: 'criar-cenario-testes-api-tests', wait: false,
 *              parameters: [string(name: 'API_GIT_BRANCH', value: env.BRANCH_NAME ?: 'develop')]
 *      Isso NÃO foi adicionado automaticamente ao Jenkinsfile da API —
 *      é uma mudança em outro repositório e deve ser feita conscientemente.
 *
 *   2) Configurar este job na UI do Jenkins com um webhook do provedor
 *      Git apontando para o repositório da API, branch `develop`
 *      (plugin "Generic Webhook Trigger" ou GitHub/GitLab webhook +
 *      "Pipeline script from SCM" configurado para o repo de testes).
 *
 * O parâmetro API_GIT_BRANCH abaixo permite executar manualmente contra
 * qualquer branch da API (útil para validar uma feature antes do merge).
 */

pipeline {
    agent any

    parameters {
        string(
            name: 'API_GIT_URL',
            defaultValue: 'https://github.com/JeanHeberth/criar-cenario-testes.git',
            description: 'Repositório da API sob teste'
        )
        string(
            name: 'API_GIT_BRANCH',
            defaultValue: 'develop',
            description: 'Branch da API a ser validada'
        )
        choice(
            name: 'TEST_SUITE',
            choices: ['smoke', 'regression'],
            description: 'smoke = contrato rápido/determinístico (gate de merge). regression = inclui cenários e2e dependentes de IA/Mongo/Jira reais.'
        )
    }

    environment {
        API_BASE_URL      = 'http://localhost:8089'
        API_HEALTH_URL    = 'http://localhost:8089/cenario/workflows'
        API_CHECKOUT_DIR  = 'api-under-test'
        COMPOSE_PROJECT   = "criar-cenario-testes-ci-${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {

        stage('Checkout - Projeto de Testes') {
            steps {
                echo 'Clonando repositório de testes (TestNG + RestAssured)...'
                checkout scm
            }
        }

        stage('Checkout - API sob teste') {
            steps {
                echo "Clonando ${params.API_GIT_URL} @ ${params.API_GIT_BRANCH} em ${API_CHECKOUT_DIR}..."
                dir(API_CHECKOUT_DIR) {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/${params.API_GIT_BRANCH}"]],
                        userRemoteConfigs: [[url: params.API_GIT_URL]]
                    ])
                }
            }
        }

        stage('Subir API via Docker Compose') {
            steps {
                withCredentials([
                    string(credentialsId: 'criar-cenario-testes-mongo-uri',      variable: 'MONGO_URI_NUVEM'),
                    string(credentialsId: 'criar-cenario-testes-openai-key',     variable: 'OPENAI_API_KEY'),
                    string(credentialsId: 'criar-cenario-testes-gemini-key',     variable: 'GEMINI_API_KEY'),
                    string(credentialsId: 'criar-cenario-testes-jira-base-url',  variable: 'JIRA_BASE_URL'),
                    string(credentialsId: 'criar-cenario-testes-jira-email',     variable: 'JIRA_EMAIL'),
                    string(credentialsId: 'criar-cenario-testes-jira-api-token', variable: 'JIRA_API_TOKEN')
                ]) {
                    script {
                        if (isUnix()) {
                            sh '''
                                set -e

                                cat > "$API_CHECKOUT_DIR/.env" <<EOF
MONGO_URI_NUVEM=$MONGO_URI_NUVEM
OPENAI_API_KEY=$OPENAI_API_KEY
GEMINI_API_KEY=$GEMINI_API_KEY
JIRA_BASE_URL=$JIRA_BASE_URL
JIRA_EMAIL=$JIRA_EMAIL
JIRA_API_TOKEN=$JIRA_API_TOKEN
APP_CORS_ALLOWED_ORIGINS=
EOF

                                echo "Subindo backend via docker compose (projeto: $COMPOSE_PROJECT)..."

                                docker compose \
                                    -f "$API_CHECKOUT_DIR/docker-compose.yml" \
                                    --env-file "$API_CHECKOUT_DIR/.env" \
                                    -p "$COMPOSE_PROJECT" \
                                    up -d --build backend
                            '''
                        } else {
                            bat '''
                                @echo off

                                (
                                    echo MONGO_URI_NUVEM=%MONGO_URI_NUVEM%
                                    echo OPENAI_API_KEY=%OPENAI_API_KEY%
                                    echo GEMINI_API_KEY=%GEMINI_API_KEY%
                                    echo JIRA_BASE_URL=%JIRA_BASE_URL%
                                    echo JIRA_EMAIL=%JIRA_EMAIL%
                                    echo JIRA_API_TOKEN=%JIRA_API_TOKEN%
                                    echo APP_CORS_ALLOWED_ORIGINS=
                                ) > "%API_CHECKOUT_DIR%\\.env"

                                echo Subindo backend via docker compose (projeto: %COMPOSE_PROJECT%)...

                                docker compose ^
                                    -f "%API_CHECKOUT_DIR%\\docker-compose.yml" ^
                                    --env-file "%API_CHECKOUT_DIR%\\.env" ^
                                    -p "%COMPOSE_PROJECT%" ^
                                    up -d --build backend

                                if errorlevel 1 exit /b %ERRORLEVEL%
                            '''
                        }
                    }
                }
            }
        }

        stage('Aguardar API iniciar') {
            steps {
                script {
                    // Sem actuator na API: usamos GET /cenario/workflows como
                    // readiness probe — é o único endpoint sem dependência de
                    // Mongo/IA/Jira, então responde assim que o contexto Spring sobe.
                    if (isUnix()) {
                        sh '''
                            echo "Aguardando API em $API_HEALTH_URL..."

                            for i in $(seq 1 24); do
                                echo "Tentativa $i de 24..."

                                if curl --silent --fail "$API_HEALTH_URL" > /dev/null; then
                                    echo "API disponível."
                                    exit 0
                                fi

                                sleep 5
                            done

                            echo "ERRO: API não iniciou no tempo esperado."
                            docker compose -f "$API_CHECKOUT_DIR/docker-compose.yml" -p "$COMPOSE_PROJECT" logs backend || true
                            exit 1
                        '''
                    } else {
                        bat '''
                            @echo off

                            echo Aguardando API em %API_HEALTH_URL%...

                            for /L %%i in (1,1,24) do (
                                echo Tentativa %%i de 24...

                                curl.exe --silent --fail "%API_HEALTH_URL%" >nul 2>&1

                                if not errorlevel 1 (
                                    echo API disponivel.
                                    exit /b 0
                                )

                                powershell.exe -NoProfile -Command "Start-Sleep -Seconds 5"
                            )

                            echo ERRO: API nao iniciou no tempo esperado.
                            docker compose -f "%API_CHECKOUT_DIR%\\docker-compose.yml" -p "%COMPOSE_PROJECT%" logs backend
                            exit /b 1
                        '''
                    }
                }
            }
        }

        stage('Preparar Projeto de Testes') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            chmod +x gradlew

                            ./gradlew clean testClasses \
                                --stacktrace \
                                --no-daemon
                        '''
                    } else {
                        bat '''
                            @echo off

                            call gradlew.bat clean testClasses ^
                                --stacktrace ^
                                --no-daemon

                            exit /b %ERRORLEVEL%
                        '''
                    }
                }
            }
        }

        stage('Executar Testes') {
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    script {
                        String gradleTask = (params.TEST_SUITE == 'regression') ? 'testRegression' : 'testSmoke'

                        if (isUnix()) {
                            sh """
                                echo "Executando suite: ${gradleTask}..."

                                ./gradlew ${gradleTask} \
                                    -Dapi.baseUrl=\$API_BASE_URL \
                                    --stacktrace \
                                    --info \
                                    --no-daemon
                            """
                        } else {
                            bat """
                                @echo off

                                echo Executando suite: ${gradleTask}...

                                call gradlew.bat ${gradleTask} ^
                                    -Dapi.baseUrl=%API_BASE_URL% ^
                                    --stacktrace ^
                                    --info ^
                                    --no-daemon

                                exit /b %ERRORLEVEL%
                            """
                        }
                    }
                }
            }
        }

        stage('Publicar Resultados JUnit') {
            steps {
                junit(
                    allowEmptyResults: true,
                    testResults: '**/build/test-results/**/*.xml'
                )
            }
        }

        stage('Publicar Allure Report') {
            steps {
                allure(
                    includeProperties: false,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: 'build/allure-results']]
                )
            }
        }
    }

    post {
        success {
            echo 'Suite de testes aprovada.'
        }

        unstable {
            echo 'Pipeline concluída com testes instáveis/falhos.'
        }

        failure {
            echo 'Falha na pipeline de testes.'
        }

        always {
            script {
                if (isUnix()) {
                    sh '''
                        docker compose -f "$API_CHECKOUT_DIR/docker-compose.yml" -p "$COMPOSE_PROJECT" down -v || true
                        rm -f "$API_CHECKOUT_DIR/.env" || true
                    '''
                } else {
                    bat '''
                        docker compose -f "%API_CHECKOUT_DIR%\\docker-compose.yml" -p "%COMPOSE_PROJECT%" down -v
                        del /f /q "%API_CHECKOUT_DIR%\\.env"
                    '''
                }
            }

            archiveArtifacts(
                artifacts: '**/build/reports/tests/**',
                allowEmptyArchive: true
            )

            archiveArtifacts(
                artifacts: '**/build/allure-results/**',
                allowEmptyArchive: true
            )

            echo 'Pipeline de testes concluída.'
        }
    }
}
