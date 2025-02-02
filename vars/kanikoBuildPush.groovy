def call(body) {
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('buildah') {
    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
      sh """
        REGISTRY="docker.io/israelxnp"
        REPOSITORY=${JOB_NAME%/*}
        TAG=""
        ENVIRONMENT=""

        if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
          TAG="dev-${GIT_COMMIT:0:10}"
          ENVIRONMENT="dev"
        elif [ $(echo $GIT_BRANCH | grep -E "^hotfix-.*") ]; then
          TAG="${GIT_BRANCH#*-}-${GIT_COMMIT:0:10}"
          ENVIRONMENT="stg"
        fi

        DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

        echo $DOCKER_PASSWORD | buildah login -u $DOCKER_USERNAME --password-stdin docker.io
        buildah bud -f Dockerfile -t $DESTINATION \$(pwd)
        buildah push $DESTINATION 
      """
    }
  }
}