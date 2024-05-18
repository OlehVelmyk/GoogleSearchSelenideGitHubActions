task_branch = "${TEST_BRANCH_NAME}"
browser_name = "${BROWSER_NAME}"
def branch_cutted = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch_cutted _ $browser_name"
base_git_url = "https://github.com/OlehVelmyk/GoogleSearchSelenide.git"


node {
    withEnv(["branch=${branch_cutted}", "base_url=${base_git_url}"]) {
//        withCredentials([string(credentialsId: 'telegram-token', variable: 'tg_token'),
//                         string(credentialsId: 'telegram_chatId', variable: 'tg_chatId')]) {
            stage("Checkout Branch") {
                if (!"$branch_cutted".contains("master")) {
                    try {
                        getProject("$base_git_url", "$branch_cutted")
                    } catch (err) {
                        echo "Failed get branch $branch_cutted"
                        throw ("${err}")
                    }
                } else {
                    echo "Current branch is master"
                    git "$base_git_url"
                }
            }

            try {
                stage("Run tests in ${browser_name}") {
                    runTestWithTag(browser_name)
                }
            } catch (err) {
                echo "Some failed tests ${browser_name}"
                throw ("${err}")
            }
            finally {
                stage ("Allure") {
                    generateAllure()
                }
                stage ("Slack") {
                    generateSlackNotification()
                }
                stage ("Telegram") {
                    generateTelegramNotification()
                }
            }


//        try {
//            parallel getTestStages(["apiTests", "uiTests"])
//        } finally {
//            stage ("Allure") {
//                generateAllure()
//            }
//        }

//        try {
//            stage("Run tests") {
//                parallel(
//                        'Api Tests': {
//                            runTestWithTag("apiTests")
//                        },
//                        'Ui Tests': {
//                            runTestWithTag("uiTests")
//                        }
//                )
//            }
//        } finally {
//            stage("Allure") {
//                generateAllure()
//            }
//        }
        }
    }

//def getTestStages(testTags) {
//    def stages = [:]
//    testTags.each { tag ->
//        stages["${tag}"] = {
//            runTestWithTag(tag)
//        }
//    }
//    return stages
//}

def runTestWithTag(String tag) {
    try {
        if (isUnix()) {
            echo "Current OS - Unix"
            labelledShell(label: 'Run ${tag}', script: "mvn clean test -DbrowserName=${tag}")
        } else {
            echo "Current OS - Windows"
            bat "mvn clean test -DbrowserName=${tag}"
        }
    } catch(err) {
        echo "some failed tests"
        throw ("${err}")
    }
}

def getProject(String repo, String branch) {
    cleanWs()
    checkout scm: [
            $class           : 'GitSCM', branches: [[name: branch]],
            userRemoteConfigs: [[
                                        url: repo
                                ]]
    ]
}

def generateAllure() {
    allure([
            includeProperties: true,
            jdk              : '',
            properties       : [],
            reportBuildPolicy: 'ALWAYS',
            results          : [[path: 'reports/allure/allure-results']]
    ])
}

def generateSlackNotification() {
    if (currentBuild.result == "SUCCESS") {
        sendSlackNotification("#36a64f", ":white_check_mark:")
    } else {
        sendSlackNotification("#ff0000", ":rage:")
    }
}

def sendSlackNotification(String color, String slackEmoji) {
    slackSend botUser: true,
              channel: 'test_notifications',
              color: color,
              message: "${slackEmoji} <<$env.JOB_BASE_NAME>> completed!!! $currentBuild.result \r\n" +
                       "Branch: $task_branch. Browser: $browser_name. \r\n" +
                       "Report is here: http://localhost:8090/job/GoogleSearchSelenide_Pipeline/$currentBuild.number/allure/",
              tokenCredentialId: 'slack-token'
}


def generateTelegramNotification() {
    if (currentBuild.result == "SUCCESS") {
        sendTelegramNotification("\\u2705")
    } else {
        sendTelegramNotification("\\uD83D\\uDCA1")
    }
}

def sendTelegramNotification(String slackEmoji) {
    if (isUnix()) {
        sh """
        curl --location 'https://api.telegram.org/bot$tg-token/sendMessage' \
             --header 'Content-Type: application/json' \
             --data '{"chat_id": "$tg_chatId", 
                      "text": " <<$env.JOB_BASE_NAME>> completed !!! $currentBuild.result\\nBranch: $task_branch. Browser: $browser_name.\\nReport is here: http://localhost:8090/job/GoogleSearchSelenide_Pipeline/$currentBuild.number/allure/"}'
           """
    } else {
        withCredentials([
                string(credentialsId: 'telegram_chatId', variable: 'TELEGRAM_CHAT_ID'),
                string(credentialsId: 'telegram-token', variable: 'TELEGRAM_TOKEN')
        ]) {
            // Write the batch file content with proper escaping and without direct interpolation
            def batchFileContent = """
                @echo off 
                                               
                curl --location "https://api.telegram.org/bot%TELEGRAM_TOKEN%/sendMessage" ^
                --header "Content-Type: application/json" ^
                --data "{\\"chat_id\\":\\"%TELEGRAM_CHAT_ID%\\",\\"text\\":\\" '$env.JOB_BASE_NAME' completed !!! $currentBuild.result\\n Branch: $task_branch. Browser: $browser_name.\\n <a href=\\"http://localhost:8090/job/GoogleSearchSelenide_Pipeline/$currentBuild.number/allure/\\">Report is here</a>\\",\\"parse_mode\\":\\"HTML\\"}"
            """.stripIndent()

//            // Write the batch file to the workspace
//            writeFile file: 'sendTelegramMessage.bat', text: batchFileContent
//
//            // Run the batch file
//            bat 'sendTelegramMessage.bat'

            // Define the file path within the workspace
            def batchFilePath = "${env.WORKSPACE}/sendTelegramMessage.bat"

            // Write the batch file to the workspace
            writeFile file: batchFilePath, text: batchFileContent

            // Print the current workspace and batch file path for debugging
            echo "Workspace: ${env.WORKSPACE}"
            echo "Batch file path: ${batchFilePath}"

            // Ensure the batch file is created successfully
            if (fileExists(batchFilePath)) {
                echo "Batch file created successfully."
            } else {
                error "Failed to create batch file."
            }

            // Run the batch file
            bat batchFilePath
        }
    }
}