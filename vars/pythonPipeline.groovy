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
    environment {
      DISCORD_WEBHOOK = credentials('discord-webhook')
    }
    stages {
      stage('Unit tests') { 
        steps {
          pythonUnitTest {}
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
     
     
   
    
      
      
     
    }
   
  }
}