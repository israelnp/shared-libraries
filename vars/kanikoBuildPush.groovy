def call(body) {
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('kaniko') {
    sh '''
      set -e

      REGISTRY="docker.io"
      REPOSITORY="israelxnp/${JOB_NAME%/*}"
      ENVIRONMENT=""
      TAG=""

      if [[ "$GIT_BRANCH" == "develop" ]]; then
        TAG="dev-${GIT_COMMIT:0:10}"
        ENVIRONMENT="dev"
      elif [[ "$GIT_BRANCH" =~ ^hotfix-.* ]]; then
        TAG="${GIT_BRANCH#*-}-${GIT_COMMIT:0:10}"
        ENVIRONMENT="stg"
      else
        echo "Erro: Branch ${GIT_BRANCH} não suportado."
        exit 1
      fi

      DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

      if [[ ! -f Dockerfile ]]; then
        echo "Erro: Dockerfile não encontrado."
        exit 1
      fi

      /kaniko/executor \
        --destination "${DESTINATION}" \
        --context $(pwd) \
        --dockerfile $(pwd)/Dockerfile

      mkdir -p /artifacts
      echo "${TAG}" > /artifacts/${ENVIRONMENT}.artifact
    '''
  }
}
