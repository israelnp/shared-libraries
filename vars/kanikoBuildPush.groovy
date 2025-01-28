def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('kaniko') {
  withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
    sh '''
      REGISTRY="docker.io"
      REPOSITORY="mateusmullerme/${JOB_NAME%/*}"
      TAG=""
      ENVIRONMENT=""

      # Criar arquivo de configuração Docker
      mkdir -p ~/.docker
      echo "{\"auths\":{\"docker.io\":{\"username\":\"$DOCKER_USERNAME\",\"password\":\"$DOCKER_PASSWORD\"}}}" > ~/.docker/config.json

      if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
        TAG="dev-${GIT_COMMIT:0:10}"
        ENVIRONMENT="dev"
      elif [ $(echo $GIT_BRANCH | grep -E "^hotfix-.*") ]; then
        TAG="${GIT_BRANCH#*-}-${GIT_COMMIT:0:10}"
        ENVIRONMENT="stg"
      fi

      DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

      /kaniko/executor \
        --insecure \
        --destination "${DESTINATION}" \
        --context $(pwd)

      echo "${TAG}" > /artifacts/${ENVIRONMENT}.artifact
    '''
  }
}

}