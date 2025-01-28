def call(body) {
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('kaniko') {
    sh '''

      REGISTRY="docker.io"
      REPOSITORY="israelxnp/${JOB_NAME%/*}"
      TAG="0.0.2"

      cat /kaniko/.docker/config.json
      DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

      /kaniko/executor \
        --destination "${DESTINATION}" \
        --context $(pwd) \

    '''
  }
}
