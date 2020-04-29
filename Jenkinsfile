/*
 * Copyright 2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
podTemplate(containers: [
        containerTemplate(name: 'maven', image: 'maven:3.6.1-jdk-11', ttyEnabled: true, command: 'cat')
]) {
    node(POD_LABEL) {
        def GIT_BRANCH_NAME

        stage('Bootstrap') {
            if (env.CHANGE_BRANCH) {
                GIT_BRANCH_NAME=env.CHANGE_BRANCH
            } else {
                GIT_BRANCH_NAME=env.BRANCH_NAME
            }
            echo sh(script: 'env | sort', returnStdout: true)
        }

        stage('Prerequisites') {
            dir ('Palisade-common') {
                git url: 'https://github.com/gchq/Palisade-common.git'
                sh "git fetch origin develop"
                if (sh(script: "git checkout ${GIT_BRANCH_NAME}", returnStatus: true) == 0) {
                    container('maven') {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            sh 'mvn -s $MAVEN_SETTINGS install'
                        }
                    }
                }
            }
        }

        stage('Install, Unit Tests, Checkstyle') {
            dir ('Palisade-readers') {
                git url: 'https://github.com/gchq/Palisade-readers.git'
                sh "git fetch origin develop"
                sh "git checkout ${GIT_BRANCH_NAME} || git checkout develop"
                container('maven') {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        sh 'mvn -s $MAVEN_SETTINGS install'
                    }
                }
            }
        }

        stage('SonarQube analysis') {
            dir ('Palisade-readers') {
                container('maven') {
                    withCredentials([string(credentialsId: '3dc8e0fb-23de-471d-8009-ed1d5890333a', variable: 'SONARQUBE_WEBHOOK'),
                                     string(credentialsId: 'b01b7c11-ccdf-4ac5-b022-28c9b861379a', variable: 'KEYSTORE_PASS'),
                                     file(credentialsId: '91d1a511-491e-4fac-9da5-a61b7933f4f6', variable: 'KEYSTORE')]) {
                        configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                            withSonarQubeEnv(installationName: 'sonar') {
                                sh 'mvn -s $MAVEN_SETTINGS org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar -Dsonar.projectKey="Palisade-Readers-${BRANCH_NAME}" -Dsonar.projectName="Palisade-Readers-${BRANCH_NAME}" -Dsonar.webhooks.project=$SONARQUBE_WEBHOOK -Djavax.net.ssl.trustStore=$KEYSTORE -Djavax.net.ssl.trustStorePassword=$KEYSTORE_PASS'
                            }
                        }
                    }
                }
            }
        }

        stage('Maven deploy') {
            dir ('Palisade-readers') {
                container('maven') {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        if (("${env.BRANCH_NAME}" == "develop") ||
                                ("${env.BRANCH_NAME}" == "master")) {
                            sh 'palisade-login'
                            //now extract the public IP addresses that this will be open on
                            sh 'extract-addresses'
                            sh 'mvn -s $MAVEN_SETTINGS deploy -Dmaven.test.skip=true'
                            sh 'helm upgrade --install palisade . --set traefik.install=true,dashboard.install=true --set global.repository=${ECR_REGISTRY} --set global.hostname=${EGRESS_ELB} --set global.localMount.enabled=false --set global.localMount.volumeHandle=${VOLUME_HANDLE} --namespace dev'
                        } else {
                            sh "echo - no deploy"
                        }
                    }
                }
            }
        }
    }
    // No need to occupy a node
    stage("SonarQube Quality Gate") {
        timeout(time: 1, unit: 'HOURS') {
            // Just in case something goes wrong, pipeline will be killed after a timeout
            def qg = waitForQualityGate()
            // Reuse taskId previously collected by withSonarQubeEnv
            if (qg.status != 'OK') {
                error "Pipeline aborted due to SonarQube quality gate failure: ${qg.status}"
            }
        }
    }
}
