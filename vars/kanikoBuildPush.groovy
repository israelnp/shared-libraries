def call(body) {
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('kaniko') {
    sh '''


      cat /kaniko/.docker/config.json
      
     /kaniko/executor \
          --dockerfile=/workspace/Dockerfile \
          --context=dir:///workspace \
          --destination=docker.io/israelxnp/teste3:0.0.2 \
    '''
  }
}
