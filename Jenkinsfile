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
        // You can keep this now, because we will prevent 'Test' from becoming unstable
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
                bat 'mvn test -Dmaven.test.failure.ignore=true'
            }
            post {
                always {
                    script {
                        // The 'junit' step usually marks build UNSTABLE. 
                        // We use it here just to collect data.
                        def testResults = junit 'target/surefire-reports/*.xml'
                        
                        double total = testResults.totalCount
                        double passed = testResults.passCount
                        double percent = (total > 0) ? (passed / total) * 100 : 0
                        
                        env.ACTUAL_PASS_PERCENT = percent
                        echo "Captured Pass Percentage: ${env.ACTUAL_PASS_PERCENT}%"
                        
                        // IMPORTANT: Force the build to stay "SUCCESSFUL" for a moment 
                        // so the next stage isn't skipped.
                        currentBuild.result = 'SUCCESS'
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
                        echo "REVERTING: Pass rate ${actual}% is below threshold ${limit}%."
                        
                        withCredentials([usernamePassword(credentialsId: "${GIT_CREDS}", 
                                         passwordVariable: 'GIT_PASSWORD', 
                                         usernameVariable: 'GIT_USERNAME')]) {
                            
                            bat 'git config user.email "budchane24@gmail.com"'
                            bat 'git config user.name "bhagyashri"'
                            
                            // Revert the commit
                            bat "git revert --no-edit HEAD"
                            
                            // Push the revert
                            bat "git push https://%GIT_USERNAME%:%GIT_PASSWORD%@${env.REPO_URL} HEAD"
                        }
                        
                        // NOW we fail the build after the revert is done
                        error("Build Reverted: Pass rate ${actual}% was too low (Threshold: ${limit}%).")
                    } else {
                        echo "PASSED: Pass rate ${actual}% meets threshold."
                    }
                }
            }
        }

        stage('Deliver') {
            steps {
                bat "mvn help:evaluate -Dexpression=project.version -DtestRate=${env.ACTUAL_PASS_PERCENT}"
                bat 'jenkins\\scripts\\delivery.bat'
            }
        }
    }
}
