def call (body) {

  def settings = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = settings
  body()

  container('helm') {
    sh '''
     
      git clone https://github.com/israelnp/helm-applications.git
      
      if [ $(echo $GIT_BRANCH | grep ^develop$) ]; then
        ENVIRONMENT="dev"
      elif [ $(echo $GIT_BRANCH | grep -E "^hotfix-.*") ]; then
        ENVIRONMENT="stg"
      fi

      cd helm-applications/restapi-flask
      helm dependency build
      helm upgrade --install \
        --values values-ci.yaml \
        --namespace citest \
        --create-namespace \
        --set image.tag="$(cat /artifacts/${ENVIRONMENT}.artifact)" \
        --set fullnameOverride="flask" \
        --wait \
        flask-ci .

      status_code="$(curl --silent \
        --output /dev/null \
        --write-out '%{http_code}\n' \
        http://flask.citest.svc.cluster.local:5000/users)"

      if [ "$status_code" == "200" ]; then
        echo "All good, API response HTTP 200"
      else
        echo "ERROR: $status_code"
        exit 1
      fi
    '''
  }

}