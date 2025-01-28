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
            script {
              // pythonUnitTest {}
            }
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
              kanikoBuildPush {}
            }
        }
        when {
          anyOf {
            branch pattern: "develop"
            branch pattern: "hotfix-*"
          }
        }
      }
   
    
      
      
     
    }
   
  }
}