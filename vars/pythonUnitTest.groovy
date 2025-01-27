def call() {
    container('python') {
        sh '''
        echo "Instalando dependências..."
        pip install -r requirements.txt

        echo "Análise estática de código com Bandit..."
        bandit -r . -x '/.venv/','/tests/'

        echo "Formatando código com Black..."
        black .

        echo "Executando Flake8 para linting..."
        flake8 . --exclude .venv

        echo "Executando testes com Pytest..."
        pytest -v --disable-warnings
        '''
    }
}