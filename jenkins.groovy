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
//            def branchName = env.BRANCH_NAME ?: 'main'

            def batchFileContent = """
                        @echo off

echo Checking if curl is installed and accessible...
curl --version
if %ERRORLEVEL% neq 0 (
    echo curl is not installed or not in the PATH
    exit /b 1
)

                        set TELEGRAM_CHAT_ID=${TELEGRAM_CHAT_ID}
                        set TELEGRAM_TOKEN=${TELEGRAM_TOKEN}
                        set JOB_NAME=${env.JOB_NAME}
                        set BUILD_RESULT=${currentBuild.result ?: 'SUCCESS'}
                        set BUILD_NUMBER=${env.BUILD_NUMBER}
                        set JOB_URL=${env.JOB_URL}
                        set BRANCH_NAME=${task_branch}
                        set BROWSER_NAME=${env.BROWSER_NAME}

//echo.
//echo Environment variables:
//echo TELEGRAM_CHAT_ID=%TELEGRAM_CHAT_ID%
//echo TELEGRAM_TOKEN=%TELEGRAM_TOKEN%
//echo JOB_NAME=%JOB_NAME%
//echo BUILD_RESULT=%BUILD_RESULT%
//echo BUILD_NUMBER=%BUILD_NUMBER%
//echo JOB_URL=%JOB_URL%
//echo BRANCH_NAME=%BRANCH_NAME%
//echo BROWSER_NAME=%BROWSER_NAME%

//echo.
//echo Executing simplified curl command for debugging...
//curl --location "https://api.telegram.org/bot%TELEGRAM_TOKEN%/sendMessage" ^
//--header "Content-Type: application/json" ^
//--data "{\\"chat_id\\":\\"%TELEGRAM_CHAT_ID%\\",\\"text\\":\\"Test message\\"}"
//
//echo.
//echo Executing full curl command with simplified message...
//curl --location "https://api.telegram.org/bot%TELEGRAM_TOKEN%/sendMessage" ^
//--header "Content-Type: application/json" ^
//--data "{\\"chat_id\\":\\"%TELEGRAM_CHAT_ID%\\",\\"text\\":\\"Job '%JOB_NAME%' completed with status %BUILD_RESULT%.\\",\\"parse_mode\\":\\"HTML\\"}"

echo.
echo Preparing full message for curl command...
set FULL_MESSAGE={\\"chat_id\\":\\"%TELEGRAM_CHAT_ID%\\",\\"text\\":\\" '%JOB_NAME%' completed !!! %BUILD_RESULT%\\n Branch: %BRANCH_NAME%. Browser: %BROWSER_NAME%.\\n Report is here:\\n %JOB_URL%%BUILD_NUMBER%/allure/\\",\\"parse_mode\\":\\"HTML\\"}

echo Full message: %FULL_MESSAGE%

echo.
echo Executing full curl command with full message...
curl --location "https://api.telegram.org/bot%TELEGRAM_TOKEN%/sendMessage" ^
--header "Content-Type: application/json" ^
--data ^"%FULL_MESSAGE%^"
                    """.stripIndent()

            def batchFilePath = "${env.WORKSPACE}\\sendTelegramMessage.bat"

            writeFile file: batchFilePath, text: batchFileContent

//            echo "Workspace: ${env.WORKSPACE}"
//            echo "Batch file path: ${batchFilePath}"
//
//            if (fileExists(batchFilePath)) {
//                echo "Batch file created successfully."
//            } else {
//                error "Failed to create batch file."
//            }

            def batchFile = readFile(batchFilePath)
//            echo "Batch file content:\n${batchFile}"

//            dir("${env.WORKSPACE}") {
//                if (fileExists('sendTelegramMessage.bat')) {
//                    echo "Batch file exists in workspace directory."
//                } else {
//                    error "Batch file does not exist in workspace directory."
//                }
//
//                bat 'echo Current directory: %cd%'

                bat 'sendTelegramMessage.bat'
            }
        }
    }
}