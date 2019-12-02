/*
 * Copyright 2019 Crown Copyright
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
        stage('Bootstrap') {
            sh "echo ${env.BRANCH_NAME}"
        }
        stage('Install a Maven project') {
            x = env.BRANCH_NAME

            if (x.substring(0, 2) == "PR") {
                y = x.substring(3)
                git url: 'https://github.com/gchq/Palisade-readers.git'
                sh "git fetch origin pull/${y}/head:${x}"
                sh "git checkout ${x}"
            } else { //just a normal branch
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/gchq/Palisade-readers.git'
            }
            container('maven') {
                configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS install'
                }
            }
        }
        stage('SonarQube analysis') {
            container('maven') {
                withCredentials([string(credentialsId: 'b01b7c11-ccdf-4ac5-b022-28c9b861379a', variable: 'KEYSTORE_PASS'),
                                 file(credentialsId: '91d1a511-491e-4fac-9da5-a61b7933f4f6', variable: 'KEYSTORE')]) {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        withSonarQubeEnv(installationName: 'sonar') {
                            sh 'mvn -s $MAVEN_SETTINGS org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar -Djavax.net.ssl.trustStore=$KEYSTORE -Djavax.net.ssl.trustStorePassword=$KEYSTORE_PASS'
                        }
                    }
                }
            }
        }
        stage('Build a Maven project') {
            x = env.BRANCH_NAME

            if (x.substring(0, 2) == "PR") {
                y = x.substring(3)
                git url: 'https://github.com/gchq/Palisade-readers.git'
                sh "git fetch origin pull/${y}/head:${x}"
                sh "git checkout ${x}"
            } else {
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/gchq/Palisade-readers.git'
            }
            container('maven') {
                configFileProvider(
                        [configFile(fileId: '450d38e2-db65-4601-8be0-8621455e93b5', variable: 'MAVEN_SETTINGS')]) {
                    if (("${env.BRANCH_NAME}" == "develop") ||
                            ("${env.BRANCH_NAME}" == "master")) {
                        //this will upload to ECR
                        sh 'mvn -s $MAVEN_SETTINGS deploy -Dmaven.test.skip=true'
                    } else {
                        sh "echo - no deploy"
                    }
                }
            }
        }
    }
}
