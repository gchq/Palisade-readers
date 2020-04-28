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
podTemplate(yaml: '''
apiVersion: v1
kind: Pod
spec:
  affinity:
    nodeAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 1
        preference:
          matchExpressions:
          - key: palisade-node-name
            operator: In
            values: 
            - node1
            - node2
            - node3
  containers:
  - name: docker-cmds
    image: 779921734503.dkr.ecr.eu-west-1.amazonaws.com/jnlp-did:INFRA
    imagePullPolicy: IfNotPresent
    command:
    - sleep
    args:
    - 99d
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
''') {

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
            container('docker-cmds') {
                configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS install'
                }
            }
        }
        stage('SonarQube analysis') {
            container('docker-cmds') {
                withCredentials([string(credentialsId: "${env.SQ_WEB_HOOK}", variable: 'SONARQUBE_WEBHOOK'),
                                 string(credentialsId:  "${env.SQ_KEY_STORE_PASS}", variable: 'KEYSTORE_PASS'),
                                 file(credentialsId: "${env.SQ_KEY_STORE}", variable: 'KEYSTORE')]) {
                    configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                        withSonarQubeEnv(installationName: 'sonar') {
                            sh 'mvn -s $MAVEN_SETTINGS org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar -Dsonar.projectKey="Palisade-Readers-${BRANCH_NAME}" -Dsonar.projectName="Palisade-Readers-${BRANCH_NAME}" -Dsonar.webhooks.project=$SONARQUBE_WEBHOOK -Djavax.net.ssl.trustStore=$KEYSTORE -Djavax.net.ssl.trustStorePassword=$KEYSTORE_PASS'
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
            container('docker-cmds') {
                configFileProvider(
                        [configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
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
    // No need to occupy a node
            stage("SonarQube Quality Gate"){
              timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
                def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
                if (qg.status != 'OK') {
                  error "Pipeline aborted due to SonarQube quality gate failure: ${qg.status}"
                }
              }
            }
}
