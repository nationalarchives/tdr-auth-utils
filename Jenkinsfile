library("tdr-jenkinslib")

def versionBumpBranch = "version-bump-${BUILD_NUMBER}-${params.VERSION}"

pipeline {
    agent {
        label "master"
    }
    parameters {
        choice(name: "STAGE", choices: ["intg", "staging", "prod"], description: "The stage you are deploying the auth utils library to")
    }
    stages {
        stage("Deploy to sonatype and commit changes to GitHub") {
            agent {
                ecs {
                    inheritFrom "base"
                    taskDefinitionOverride "arn:aws:ecs:eu-west-2:${env.MANAGEMENT_ACCOUNT}:task-definition/s3publish-${params.STAGE}:2"
                }
            }
            steps {
              script {
                tdr.configureJenkinsGitUser()
              }

              sh "git checkout ${versionBumpBranch}"

              sshagent(['github-jenkins']) {
                sh "sbt +'release with-defaults'"
              }

              slackSend color: "good", message: "*Auth utils* :arrow_up: The auth utils package has been published", channel: "#tdr-releases"

              script {
                tdr.pushGitHubBranch(versionBumpBranch)
              }
            }
        }
        stage("Create version bump pull request") {
          agent {
            label "master"
          }
          steps {
            script {
              tdr.createGitHubPullRequest(
                pullRequestTitle: "Version Bump from build number ${BUILD_NUMBER}",
                buildUrl: env.BUILD_URL,
                repo: "tdr-auth-utils",
                branchToMergeTo: "master",
                branchToMerge: versionBumpBranch
              )
            }
          }
        }
    }
}
