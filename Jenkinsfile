pipeline {
    agent any
    tools {
        maven 'maven3' 
    }

    environment {
        PASS_THRESHOLD = 90.0
        GIT_CREDS = 'Admin'
        
        REPO_URL = 'github.com/Bhagyashri099/TestingAuto.git'
    }

    options {
        skipStagesAfterUnstable()
    }

    stages {
        stage('Build') {
            steps {
                bat 'mvn -B -DskipTests clean package'
            }
        }

        stage('Test') {
            steps {
                // Ignore failure here so the Quality Gate can decide whether to revert
                bat 'mvn test -Dmaven.test.failure.ignore=true'
            }
            post {
                always {
                    script {
                        // 1. Capture results and calculate percentage
                        def testResults = junit 'target/surefire-reports/*.xml'
                        
                        double total = testResults.totalCount
                        double passed = testResults.passCount
                        double percent = (total > 0) ? (passed / total) * 100 : 0
                        
                        env.ACTUAL_PASS_PERCENT = percent
                        echo "Captured Pass Percentage: ${env.ACTUAL_PASS_PERCENT}%"
                    }
                }
            }
        }

        stage('Quality Gate & Revert') {
            steps {
                script {
                    double actual = env.ACTUAL_PASS_PERCENT.toDouble()
                    double limit = env.PASS_THRESHOLD.toDouble()

                    if (actual < limit) {
                        echo "FAILED: Pass rate ${actual}% is below threshold ${limit}%."
                        
                        withCredentials([usernamePassword(credentialsId: "${GIT_CREDS}", 
                                         passwordVariable: 'GIT_PASSWORD', 
                                         usernameVariable: 'GIT_USERNAME')]) {
                            
                            // Essential Git config for the commit to work
                            bat 'git config user.email "budchane24@gmail.com"'
                            bat 'git config user.name "bhagyashri"'
                            
                            // Perform revert
                            bat "git revert --no-edit HEAD"
                            
                            // Use %Variables% to inject your Admin credentials into the push
                            bat "git push https://%GIT_USERNAME%:%GIT_PASSWORD%@${env.REPO_URL} HEAD"
                        }
                        
                        error("Build Reverted: Pass rate ${actual}% was too low.")
                    } else {
                        echo "PASSED: Pass rate ${actual}% meets threshold."
                    }
                }
            }
        }

        stage('Deliver') {
            steps {
                // 3. Pass information back to the build tool (Maven)
                bat "mvn help:evaluate -Dexpression=project.version -DtestRate=${env.ACTUAL_PASS_PERCENT}"
                bat 'jenkins\\scripts\\delivery.bat'
            }
        }
    }
}
