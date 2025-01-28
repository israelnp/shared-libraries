def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('kaniko') {
    sh '''
      REGISTRY="docker.io"
      REPOSITORY="israelxnp/${JOB_NAME%/*}"
      ENVIRONMENT=""
      TAG=""

      if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
        TAG="dev-${GIT_COMMIT:0:10}"
        ENVIRONMENT="dev"
      elif [ $(echo $GIT_BRANCH | grep -E "^hotfix-.*") ]; then
        TAG="${GIT_BRANCH#*-}-${GIT_COMMIT:0:10}"
        ENVIRONMENT="stg"
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