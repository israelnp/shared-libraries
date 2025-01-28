container('kaniko') {
  withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
    sh '''
      set -e

      # Realizar login no Docker Hub
      echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin docker.io

      # Definir TAG e ENVIRONMENT com base no branch
      REGISTRY="docker.io"
      REPOSITORY="israelxnp/${JOB_NAME%/*}"
      ENVIRONMENT=""
      TAG=""

      if [[ "$GIT_BRANCH" == "develop" ]]; then
        TAG="dev-${GIT_COMMIT:0:10}"
        ENVIRONMENT="dev"
      elif [[ "$GIT_BRANCH" == hotfix-* ]]; then
        TAG="${GIT_BRANCH#hotfix-}-${GIT_COMMIT:0:10}"
        ENVIRONMENT="stg"
      else
        echo "Branch não suportado: $GIT_BRANCH"
        exit 1
      fi

      DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

      # Executar Kaniko para build e push da imagem
      /kaniko/executor --destination "${DESTINATION}" --context $(pwd)

      # Salvar TAG no arquivo de artefato
      mkdir -p /artifacts
      echo "${TAG}" > /artifacts/${ENVIRONMENT}.artifact
    '''
  }
}