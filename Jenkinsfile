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
            sh "echo ${env.BRANCH_NAME}"
            sh "echo ${env.BRANCH_NAME}"
            x = env.BRANCH_NAME

            sh "echo before"
            sh "echo ${x}"
            sh "echo after"
            z = x.substring(1, 2)
            zz = x.substring(0, 1)
            zzz = x.substring(0, 2);
            sh "echo ${z}"
            sh "echo ${zz}"
            sh "echo ${zzz}"


            if (x.substring(1, 2) == "PR") {
                sh "echo in substring"
                y = x.substring(3)
                git url: 'https://github.com/gchq/Palisade-readers.git'
                git pull origin pull / $ { y } / head
            } else {
                sh "echo not in substring"
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/gchq/Palisade-readers.git'
            }
            container('maven') {
                configFileProvider([configFile(fileId: "${env.CONFIG_FILE}", variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS install'
                }
            }
        }
        stage('Build a Maven project') {
            sh "echo ${env.BRANCH_NAME}"
            sh "echo ${env.BRANCH_NAME}"
            x = env.BRANCH_NAME

            sh "echo before"
            sh "echo ${x}"
            sh "echo after"


            if (x.substring(0, 2) == "PR") {
                y = x.substring(3)
                git url: 'https://github.com/gchq/Palisade-readers.git'
                git pull origin pull / $ { y } / head
            } else {
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/gchq/Palisade-readers.git'
            }
            container('maven') {
                configFileProvider(
                        [configFile(fileId: '450d38e2-db65-4601-8be0-8621455e93b5', variable: 'MAVEN_SETTINGS')]) {
                    if (("${env.BRANCH_NAME}" == "develop") ||
                            ("${env.BRANCH_NAME}" == "master")) {
                        sh 'mvn -s $MAVEN_SETTINGS deploy -Dmaven.test.skip=true'
                    } else {
                        sh "echo - no deploy"
                    }
                }
            }
        }
    }
}
