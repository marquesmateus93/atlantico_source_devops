pipeline{

    agent any

    options {
        ansiColor('xterm')
    }

    environment {
        int DEPLOY_BUILD_NUMBER = VersionNumber([
        versionNumberString : '${BUILDS_ALL_TIME}',
        projectStartDate : '2020-02-14',
        worstResultForIncrement: 'SUCCESS'
        ]);

        IP = '34.71.104.252'

        APPLICATION_GENERAL_NAME='hello_world'
        APPLICATION_WAR_NAME='ROOT'
        APPLICATION_SERVER_JAVA_PATH='tomcat/9'
        APPLICATION_BUILD_FOLDER='spring_boot_deploy'
        CREDENTIALS_SSH_ID='jenkins'

        PROJECT_NAME='sistema-marques'
        CONTAINER_NAME='hello_world'
        HTTP_CONTAINER_PORT='80'
        HTTPS_CONTAINER_PORT='443'
    }

    stages{
        stage ('Workspace Cleanup'){ // Apaga todo o conteúdo do diretório.
            steps{
                sh "rm -rf *"
            }
        }

        stage('Git Clone'){ // Clona o projeto.
            steps{
                dir ('spring_boot_deploy'){
                    git branch: 'master',
                    credentialsId: 'jenkins',
                    url: 'git@github.com:marquesmateus93/atlantico_source_code.git'
                }
            }
        }
        
        stage('Version Generator'){ // Gera a versão.
            steps{
                sh "cd ${APPLICATION_BUILD_FOLDER} && '$WORKSPACE'/${APPLICATION_BUILD_FOLDER}/mvnw package"
                sh "mv ${APPLICATION_BUILD_FOLDER}/target/demo-0.0.1-SNAPSHOT.war ${APPLICATION_BUILD_FOLDER}/target/${APPLICATION_WAR_NAME}.war"
            }
        }

         stage('Docker Image'){ // Gera imagem no servidor Docker.
            steps{
                sh "cp ${APPLICATION_BUILD_FOLDER}/target/${APPLICATION_WAR_NAME}.war /opt/dockerfiles/${APPLICATION_SERVER_JAVA_PATH}/${APPLICATION_WAR_NAME}.war"
                script{
                    withDockerServer([uri: "tcp://${IP}:59752", credentialsId: ""]){
                        docker.build("${APPLICATION_GENERAL_NAME}:${DEPLOY_BUILD_NUMBER}", \
                            "--build-arg WAR_NAME=${APPLICATION_WAR_NAME} \
                            -f /opt/dockerfiles/${APPLICATION_SERVER_JAVA_PATH}/Dockerfile /opt/dockerfiles/${APPLICATION_SERVER_JAVA_PATH}")
                    }
                }
            }
        }

         stage('Docker Deploy'){ // Sobe versão no Docker.
            steps{
                sshagent(["${CREDENTIALS_SSH_ID}"]){
                    script{
                        stageResults = [:]
                        sh '''
                            ssh -tt jenkins@${IP} -o StrictHostKeyChecking=no \
                            "set +e
                            sudo docker rm -f ${CONTAINER_NAME};
                            sudo docker image prune -f;
                            
                            sudo docker run \
                            -itd \
                            --restart always \
                            --name ${CONTAINER_NAME} \
                            -e JAVA_OPTS='-DHTTP_PORT=80 \
                            -DHTTPS_PORT=443' \
                            -p ${HTTP_CONTAINER_PORT}:${HTTP_CONTAINER_PORT} \
                            -p ${HTTPS_CONTAINER_PORT}:${HTTPS_CONTAINER_PORT} \
                            ${APPLICATION_GENERAL_NAME}:${DEPLOY_BUILD_NUMBER};

                            if [[ $((DEPLOY_BUILD_NUMBER -1)) -ge 0 ]];
                                then
                                sudo docker rmi -f ${APPLICATION_GENERAL_NAME}:$((DEPLOY_BUILD_NUMBER -1))
                            fi"
                        '''
                    }
                }
            }
        }
    }
    
    post{
        always{
            sh 'rm -f /opt/dockerfiles/${APPLICATION_SERVER_JAVA_PATH}/${APPLICATION_WAR_NAME}.war'
        }
    }
}