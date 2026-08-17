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
                // Mongo/OpenAI/Gemini são obrigatórios — a API não sobe sem eles
                // (sem valor default em application.yml). Jira é opcional: tem
                // default vazio (${JIRA_BASE_URL:}) e a suite smoke nunca chama
                // o Jira de verdade, só valida formato de taskKey antes de
                // qualquer chamada externa.
                withCredentials([
                    string(credentialsId: 'criar-cenario-testes-mongo-uri',  variable: 'MONGO_URI_NUVEM'),
                    string(credentialsId: 'criar-cenario-testes-openai-key', variable: 'OPENAI_API_KEY'),
                    string(credentialsId: 'criar-cenario-testes-gemini-key', variable: 'GEMINI_API_KEY')
                ]) {
                    script {
                        if (isUnix()) {
                            sh '''
                                set -e

                                cat > "$API_CHECKOUT_DIR/.env" <<EOF
MONGO_URI_NUVEM=$MONGO_URI_NUVEM
OPENAI_API_KEY=$OPENAI_API_KEY
GEMINI_API_KEY=$GEMINI_API_KEY
APP_CORS_ALLOWED_ORIGINS=
EOF
                            '''
                        } else {
                            bat '''
                                @echo off

                                (
                                    echo MONGO_URI_NUVEM=%MONGO_URI_NUVEM%
                                    echo OPENAI_API_KEY=%OPENAI_API_KEY%
                                    echo GEMINI_API_KEY=%GEMINI_API_KEY%
                                    echo APP_CORS_ALLOWED_ORIGINS=
                                ) > "%API_CHECKOUT_DIR%\\.env"
                            '''
                        }
                    }
                }

                script {
                    try {
                        withCredentials([
                            string(credentialsId: 'criar-cenario-testes-jira-base-url',  variable: 'JIRA_BASE_URL'),
                            string(credentialsId: 'criar-cenario-testes-jira-email',     variable: 'JIRA_EMAIL'),
                            string(credentialsId: 'criar-cenario-testes-jira-api-token', variable: 'JIRA_API_TOKEN')
                        ]) {
                            if (isUnix()) {
                                sh '''
                                    cat >> "$API_CHECKOUT_DIR/.env" <<EOF
JIRA_BASE_URL=$JIRA_BASE_URL
JIRA_EMAIL=$JIRA_EMAIL
JIRA_API_TOKEN=$JIRA_API_TOKEN
EOF
                                '''
                            } else {
                                bat '''
                                    @echo off
                                    (
                                        echo JIRA_BASE_URL=%JIRA_BASE_URL%
                                        echo JIRA_EMAIL=%JIRA_EMAIL%
                                        echo JIRA_API_TOKEN=%JIRA_API_TOKEN%
                                    ) >> "%API_CHECKOUT_DIR%\\.env"
                                '''
                            }
                        }
                    } catch (err) {
                        echo 'Credenciais do Jira não configuradas no Jenkins — seguindo sem integração real com Jira (a suite smoke não depende disso; necessário só para os testes do grupo e2e de Jira).'
                        if (isUnix()) {
                            sh '''
                                cat >> "$API_CHECKOUT_DIR/.env" <<EOF
JIRA_BASE_URL=
JIRA_EMAIL=
JIRA_API_TOKEN=
EOF
                            '''
                        } else {
                            bat '''
                                @echo off
                                (
                                    echo JIRA_BASE_URL=
                                    echo JIRA_EMAIL=
                                    echo JIRA_API_TOKEN=
                                ) >> "%API_CHECKOUT_DIR%\\.env"
                            '''
                        }
                    }
                }

                // Claude é opcional pelo mesmo motivo do Jira: ANTHROPIC_API_KEY
                // tem default vazio em application.yml e a API só chama a
                // Anthropic quando AI_ACTIVE_PROVIDER=claude. Configurar a
                // credencial no Jenkins é o que habilita rodar a suite contra o
                // Claude sem tocar em código.
                script {
                    try {
                        withCredentials([
                            string(credentialsId: 'criar-cenario-testes-anthropic-key', variable: 'ANTHROPIC_API_KEY')
                        ]) {
                            if (isUnix()) {
                                sh '''
                                    cat >> "$API_CHECKOUT_DIR/.env" <<EOF
ANTHROPIC_API_KEY=$ANTHROPIC_API_KEY
EOF
                                '''
                            } else {
                                bat '''
                                    @echo off
                                    (
                                        echo ANTHROPIC_API_KEY=%ANTHROPIC_API_KEY%
                                    ) >> "%API_CHECKOUT_DIR%\\.env"
                                '''
                            }
                        }
                    } catch (err) {
                        echo 'Credencial da Anthropic não configurada no Jenkins — seguindo sem o provider Claude (a API sobe normalmente e usa o provider configurado em AI_ACTIVE_PROVIDER).'
                        if (isUnix()) {
                            sh '''
                                cat >> "$API_CHECKOUT_DIR/.env" <<EOF
ANTHROPIC_API_KEY=
EOF
                            '''
                        } else {
                            bat '''
                                @echo off
                                (
                                    echo ANTHROPIC_API_KEY=
                                ) >> "%API_CHECKOUT_DIR%\\.env"
                            '''
                        }
                    }
                }

                script {
                    echo "Subindo backend via docker compose (projeto: ${COMPOSE_PROJECT})..."

                    if (isUnix()) {
                        sh '''
                            docker compose \
                                -f "$API_CHECKOUT_DIR/docker-compose.yml" \
                                --env-file "$API_CHECKOUT_DIR/.env" \
                                -p "$COMPOSE_PROJECT" \
                                up -d --build backend
                        '''
                    } else {
                        bat '''
                            @echo off

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
                        @echo off
                        docker compose -f "%API_CHECKOUT_DIR%\\docker-compose.yml" -p "%COMPOSE_PROJECT%" down -v
                        if exist "%API_CHECKOUT_DIR%\\.env" del /f /q "%API_CHECKOUT_DIR%\\.env"
                        exit /b 0
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
