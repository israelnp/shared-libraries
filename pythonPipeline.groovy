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
          stage('JUnit test') {
              steps {
                  script {
                    pythonUnitTest {}
                  }
              }
          }
      }
  }

}