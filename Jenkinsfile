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

post {
        unstable {
            script {
                // This block runs ONLY if tests fail (Unstable status)
                double actual = env.ACTUAL_PASS_PERCENT.toDouble()
                double limit = env.PASS_THRESHOLD.toDouble()

                if (actual < limit) {
                    echo "REVERTING: Pass rate ${actual}% is below threshold ${limit}%."
                    
                    withCredentials([usernamePassword(credentialsId: "${GIT_CREDS}", 
                                     passwordVariable: 'GIT_PASSWORD', 
                                     usernameVariable: 'GIT_USERNAME')]) {
                        
                        bat 'git config user.email "budchane24@gmail.com"'
                        bat 'git config user.name "bhagyashri"'
                        bat "git revert --no-edit HEAD"
                        bat "git push https://%GIT_USERNAME%:%GIT_PASSWORD%@${env.REPO_URL} HEAD"
                    }
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
