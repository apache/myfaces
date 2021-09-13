/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pipeline {
    agent {
        label 'ubuntu'
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 2, unit: 'HOURS')
        retry(2)
    }

    triggers {
        cron('@daily')
        pollSCM('@hourly')
    }

    stages {
        stage('Prepare') {
            matrix {
                agent {
                  label 'ubuntu'
                }
                axes {
                    axis {
                        name 'JAVA_VERSION'
                        values 'jdk_11_latest', 'jdk_14_latest'
                    }
                }

                tools {
                    maven "maven_latest"
                    jdk "${JAVA_VERSION}"
                }

                stages {
                    stage('BuildAndTest') {
                        steps {
                            sh 'mvn -V clean verify checkstyle:check apache-rat:check'
                        }
                        post {
                            always {
                               junit(testResults: '**/surefire-reports/*.xml', allowEmptyResults: true)
                               junit(testResults: '**/failsafe-reports/*.xml', allowEmptyResults: true)
                               archiveArtifacts '**/target/*.jar'
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            tools {
                maven "maven_latest"
                jdk "jdk_1.8_latest"
            }
            steps {
                sh "mvn clean deploy -Pgenerate-assembly"
            }
        }
    }

    post {
        // Build Failed
        failure {
            mail to: "notifications@myfaces.apache.org",
            subject: "Jenkins pipeline failed: ${currentBuild.fullDisplayName}",
            body: """
            Jenkins build URL: ${env.BUILD_URL}
            The build for ${env.JOB_NAME} completed successfully and is back to normal.
            Build: ${env.BUILD_URL}
            Logs: ${env.BUILD_URL}console
            Changes: ${env.BUILD_URL}changes
            """
        }

        // Build succeeded, but some tests failed
        unstable {
            mail to: "notifications@myfaces.apache.org",
            subject: "Jenkins pipeline failed: ${currentBuild.fullDisplayName}",
            body: """
            Jenkins build URL: ${env.BUILD_URL}
            The build for ${env.JOB_NAME} completed successfully and is back to normal.
            Build: ${env.BUILD_URL}
            Logs: ${env.BUILD_URL}console
            Changes: ${env.BUILD_URL}changes
            """
        }

        // Last build failed, but current one was successful
        fixed {
            mail to: "notifications@myfaces.apache.org",
            subject: "Jenkins pipeline is back to normal: ${currentBuild.fullDisplayName}",
            body: """
            Jenkins build URL: ${env.BUILD_URL}
            The build for ${env.JOB_NAME} completed successfully and is back to normal.
            Build: ${env.BUILD_URL}
            Logs: ${env.BUILD_URL}console
            Changes: ${env.BUILD_URL}changes
            """
        }
    }
}

