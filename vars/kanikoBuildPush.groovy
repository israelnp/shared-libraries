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

      # Verificação do branch e definição de TAG e ENVIRONMENT
      if [[ "$GIT_BRANCH" == "develop" ]]; then
        TAG="dev-${GIT_COMMIT:0:10}"
        ENVIRONMENT="dev"
      elif [[ "$GIT_BRANCH" =~ ^hotfix-.* ]]; then
        TAG="${GIT_BRANCH#*-}-${GIT_COMMIT:0:10}"
        ENVIRONMENT="stg"
      else
        echo "Branch não suportado: $GIT_BRANCH"
        exit 1
      fi

      DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

      # Executar Kaniko para build e push da imagem
      /kaniko/executor \
        --destination "${DESTINATION}" \
        --context $(pwd) \
        --dockerfile $(pwd)/Dockerfile

      # Salvar TAG no arquivo de artefato
      mkdir -p /artifacts
      echo "${TAG}" > /artifacts/${ENVIRONMENT}.artifact
    '''
  }
}