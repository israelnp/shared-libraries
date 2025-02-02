def call(body) {
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('buildah') {
    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
      sh """
        REGISTRY="docker.io/israelxnp"
        REPOSITORY=\$(echo "$JOB_NAME" | awk -F'/' '{print \$1}')
        TAG=""
        ENVIRONMENT=""

        if echo "\$GIT_BRANCH" | grep -q '^develop$'; then
          TAG="dev-\$(echo \$GIT_COMMIT | cut -c1-10)"
          ENVIRONMENT="dev"
        elif echo "\$GIT_BRANCH" | grep -E -q "^hotfix-.*"; then
          TAG="\$(echo "\$GIT_BRANCH" | cut -d'-' -f2)-\$(echo \$GIT_COMMIT | cut -c1-10)"
          ENVIRONMENT="stg"
        else
          TAG="latest"
          ENVIRONMENT="prod"
        fi

        DESTINATION="\${REGISTRY}/\${REPOSITORY}:\${TAG}"

        echo "\$DOCKER_PASSWORD" | buildah login -u "\$DOCKER_USERNAME" --password-stdin docker.io
        buildah bud -f Dockerfile -t "\$DESTINATION" "\$(pwd)"
        buildah push "\$DESTINATION"
      """
    }
  }
}
