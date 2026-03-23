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
                        // 1. Record the results
                        def testResults = junit 'target/surefire-reports/*.xml'
                        
                        // 2. Calculate the percentage
                        double total = testResults.totalCount
                        double passed = testResults.passCount
                        double percent = (total > 0) ? (passed / total) * 100 : 0
                        
                        env.ACTUAL_PASS_PERCENT = percent
                        echo "Captured Pass Percentage: ${env.ACTUAL_PASS_PERCENT}%"

                        // 3. FORCE SUCCESS (This is the missing piece)
                        // This prevents 'skipStagesAfterUnstable' from triggering yet.
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
                            bat "git push https://%GIT_USERNAME%:%GIT_PASSWORD%@${env.REPO_URL} HEAD:refs/heads/master"
                        }
                        
                        //  fail the build after the revert is done
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
