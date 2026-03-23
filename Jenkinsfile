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
            // Use doubleValue() or toDouble() safely
            double actual = env.ACTUAL_PASS_PERCENT.toDouble()
            double limit = env.PASS_THRESHOLD.toDouble()

            if (actual < limit) {
                echo "REVERTING: Pass rate ${actual}% is below threshold ${limit}%."

                withCredentials([usernamePassword(credentialsId: "${GIT_CREDS}", 
                                                 passwordVariable: 'GIT_PASSWORD', 
                                                 usernameVariable: 'GIT_USERNAME')]) {
                    
                    // Clean workspace and ensure we are on a fresh state
                    bat 'git reset --hard'
                    bat 'git clean -fdx'

                    // Use the specific repo URL and ensure master is up to date
                    bat "git fetch https://%GIT_USERNAME%:%GIT_PASSWORD%@${env.REPO_URL.replace('https://', '')} master"
                    bat "git checkout master"
                    bat "git pull https://%GIT_USERNAME%:%GIT_PASSWORD%@${env.REPO_URL.replace('https://', '')} master"

                    // Identity is required for the revert commit
                    bat 'git config user.email "budchane24@gmail.com"'
                    bat 'git config user.name "bhagyashri"'

                    // Revert the specific commit that triggered this build
                    // --no-edit prevents the interactive editor from popping up
                    bat "git revert --no-edit ${env.GIT_COMMIT}"

                    // Push the revert back to the remote master branch
                    bat "git push https://%GIT_USERNAME%:%GIT_PASSWORD%@${env.REPO_URL.replace('https://', '')} HEAD:master"
                }

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
