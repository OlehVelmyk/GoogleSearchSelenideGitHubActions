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
                       "Report is here: ${env.JOB_URL}$currentBuild.number/allure/",
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
        curl --location 'https://api.telegram.org/bot$telegram-token/sendMessage' \
             --header 'Content-Type: application/json' \
             --data '{"chat_id": "$telegram_chatId", 
                      "text": " <<$env.JOB_BASE_NAME>> completed !!! $currentBuild.result\\nBranch: $task_branch. Browser: $browser_name.\\nReport is here: ${env.JOB_URL}$currentBuild.number/allure/"}'
           """
    } else {
        withCredentials([
                string(credentialsId: 'telegram_chatId', variable: 'TELEGRAM_CHAT_ID'),
                string(credentialsId: 'telegram-token', variable: 'TELEGRAM_TOKEN')
        ]) {
            def batchFileContent = """
                @echo off

                set TELEGRAM_CHAT_ID=${TELEGRAM_CHAT_ID}
                set TELEGRAM_TOKEN=${TELEGRAM_TOKEN}
                set JOB_NAME=${env.JOB_NAME}
                set BUILD_RESULT=${currentBuild.result}
                set BUILD_NUMBER=${env.BUILD_NUMBER}
//                set JOB_URL=${env.JOB_URL}
                set BRANCH_NAME=${task_branch}
                set BROWSER_NAME=${env.BROWSER_NAME}

                set FULL_MESSAGE={\\"chat_id\\":\\"%TELEGRAM_CHAT_ID%\\",\\"text\\":\\"%u2705% '%JOB_NAME%' completed !!! %BUILD_RESULT%\\n Branch: %BRANCH_NAME%. Browser: %BROWSER_NAME%.\\n Report is here: http://172.25.160.1:8090/job/%JOB_NAME%/%BUILD_NUMBER%/allure/\\",\\"parse_mode\\":\\"HTML\\"}

                curl --location "https://api.telegram.org/bot%TELEGRAM_TOKEN%/sendMessage" ^
                     --header "Content-Type: application/json" ^
                     --data ^"%FULL_MESSAGE%^"
            """.stripIndent()

            def batchFilePath = "${env.WORKSPACE}\\sendTelegramMessage.bat"

            writeFile file: batchFilePath, text: batchFileContent

//            def batchFile = readFile(batchFilePath)

                bat 'sendTelegramMessage.bat'
            }
        }
    }