def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  pipeline {
    agent {
      kubernetes {
        yamlFile 'jenkinsPod.yaml'
      }
    }
    stages {
      stage('Unit tests') { 
        steps {
            // script {
            //    pythonUnitTest {}
            // }
            sh ''' ls -la '''
        }
        when {
          anyOf {
            branch pattern: "feature-*"
            branch pattern: "develop"
            branch pattern: "hotfix-*"
            branch pattern: "release-*"
            branch pattern: "v*"
          }
        }
      }
     
      stage('Build and Push') {
        steps {
          script {
              buildahBuildPush {}
            }
        }
        when {
          anyOf {
            branch pattern: "develop"
            branch pattern: "hotfix-*"
          }
        }
      }
   
     stage('Artifact Promotion') {
        steps {
          script {
              artifactPromotionBuildah {}
            }
        }
        when {
          anyOf {
            branch pattern: "release-*"
            branch pattern: "v*"
          }
        }
      }
      
      
     
    }
   
  }
}