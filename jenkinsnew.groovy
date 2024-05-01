task_branch = "${TEST_BRANCH_NAME}"
browser_name = "${BROWSER_NAME}"
def branch_cutted = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch_cutted _ $browser_name"
base_git_url = "https://github.com/OlehVelmyk/GoogleSearchSelenide.git"


node {
    withEnv(["branch=${branch_cutted}", "base_url=${base_git_url}"]) {
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
            }
        }

//        tools {
//            maven 'Maven 3.9.6'
//            jdk 'jdk8'
//        }

//        stage("Build Maven") {
//            tool name: "Maven 3.9.6", type: "maven"
//            bat "mvn clean deploy"
//        }

            try {
                stage("Run tests in ${browser_name}") {
//                    labelledShell(label: "Run ${browser_name}", script: "mvn clean test -DbrowserName=${browser_name}")
                    label: "Run ${browser_name}"
                    bat "mvn clean test -DbrowserName=${browser_name}"
                }
            } catch (err) {
                    echo "Some failed tests ${browser_name}"
                    throw ("${err}")
                }
//            finally {
//                   stage ("Allure") {
//                       generateAllure()
//                   }
//                }

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

//def runTestWithTag(String tag) {
//    try {
//        labelledShell(label: "Run ${tag}", script: "chmod +x gradlew \n./gradlew -x test ${tag}")
//    } finally {
//        echo "some failed tests"
//    }
//}

//def runTestWithTag(String tag) {
//    try {
//        labelledPowerShell(label: 'Run ${tag}', script: "mvn clean test -DbrowserName=${tag}")
//    } catch (err) {
//        echo "Some failed tests ${tag}"
//        throw ("${err}")
//    }
//}

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


