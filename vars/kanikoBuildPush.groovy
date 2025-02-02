def call(body) {
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('buildah') {
    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
      sh """
        ls -la
        echo $DOCKER_PASSWORD | buildah login -u $DOCKER_USERNAME --password-stdin docker.io
        buildah bud -f Dockerfile -t docker.io/israelxnp/teste3:0.0.2 \$(pwd)
        buildah push docker.io/israelxnp/teste3:0.0.2
      """
    }
  }
}