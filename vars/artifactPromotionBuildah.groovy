def call(body) {
  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('buildah') {
    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
      sh '''
        REGISTRY="docker.io/israelxnp"
        REPOSITORY=$(echo "$JOB_NAME" | cut -d'/' -f1)

        OLD_TAG=""
        TAG=""
        ENVIRONMENT=""

        if echo "$GIT_BRANCH" | grep -E "^release-.*"; then
          OLD_TAG=$(cat /artifacts/dev.artifact)
          ENVIRONMENT="stg"
          TAG="${GIT_BRANCH#*-}-$(echo ${OLD_TAG} | cut -d - -f 2)"
        elif echo "$GIT_BRANCH" | grep -E "v[0-9]\\.[0-9]{1,2}\\.[0-9]{1,3}$"; then
          OLD_TAG=$(cat /artifacts/stg.artifact)
          ENVIRONMENT="pro"
          TAG="$(echo ${OLD_TAG} | cut -d - -f 1)"
        fi

        OLD_DESTINATION="${REGISTRY}/${REPOSITORY}:${OLD_TAG}"
        NEW_DESTINATION="${REGISTRY}/${REPOSITORY}:${TAG}"

        echo "🔹 Autenticando no registry..."
        echo "$DOCKER_PASSWORD" | buildah login -u "$DOCKER_USERNAME" --password-stdin docker.io

        echo "🔹 Garantindo que a imagem existe antes do tagging..."
        buildah pull ${OLD_DESTINATION} || echo "Imagem já disponível localmente"

        echo "🔹 Copiando imagem: ${OLD_DESTINATION} -> ${NEW_DESTINATION}"
        buildah tag ${OLD_DESTINATION} ${NEW_DESTINATION}

        echo "${TAG}" > /artifacts/${ENVIRONMENT}.artifact
      '''
    }
  }
}
