//Define Pipeline
pipeline {
    agent {
        label "master"
    }

    tools {
        // Note: this should match with the tool name configured in your jenkins instance (JENKINS_URL/configureTools/)
        maven "maven-3"
    }

    environment {
        // Git Repo
        GIT_URL = "http://gitlab.domain.com/eanylin/quarkus-test.git"
        
        // This can be nexus3 or nexus2
        NEXUS_VERSION = "nexus3"

        // This can be http or https
        NEXUS_PROTOCOL = "http"

        // Where your Nexus is running
        NEXUS_URL = "nexus.domain.com:8081"

        // Repository where we will upload the artifact
        NEXUS_REPOSITORY = "maven-releases"

        // Jenkins credential id to authenticate to Nexus OSS
        NEXUS_CREDENTIAL_ID = "nexus-credentials"
    }

    stages {
        stage("Build") {
            steps {
                script {
                    // Clone the repository
                    git GIT_URL
                        
                    // If you are using Windows then you should use "bat" step
                    // Since unit testing is out of the scope we skip them
                    sh "mvn -B -DskipTests=true clean package" 
                }
            }
        }

        stage("Unit Test") {
            steps {
                script {
                    // Test complied source code
                    sh "mvn -B clean test" 
                }
            }
        }

        stage("Integration Test") {
            steps {
                script {
                    // Run checks on results of integration tests to ensure quality criteria are met
                    sh "mvn -B clean verify -DskipTests=true" 
                }
            }
        }

        stage("SonarQube Analysis") {
            steps {
                script {
                    withSonarQubeEnv('sonarqube'){
                    sh "mvn sonar:sonar -DskipTests=true"
                    }
                }
            }
        }

        stage("Publish to Nexus") {
            steps {
                script {
                    // Read POM xml file using 'readMavenPom' step , this step 'readMavenPom' is included in: https://plugins.jenkins.io/pipeline-utility-steps
                    pom = readMavenPom file: "pom.xml";
                    
                    // Find built artifact under target folder
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}");

                    // Print some info from the artifact found
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"

                    // Extract the path from the File found
                    artifactPath = filesByGlob[0].path;

                    // Assign to a boolean response verifying If the artifact name exists
                    artifactExists = fileExists artifactPath;

                    if(artifactExists) {
                        echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";

                        nexusArtifactUploader(
                            nexusVersion: NEXUS_VERSION,
                            protocol: NEXUS_PROTOCOL,
                            nexusUrl: NEXUS_URL,
                            groupId: pom.groupId,
                            version: pom.version,
                            repository: NEXUS_REPOSITORY,
                            credentialsId: NEXUS_CREDENTIAL_ID,

                            artifacts: [
                                // Artifact generated such as .jar, .ear and .war files.
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: artifactPath,
                                type: pom.packaging],

                                // Lets upload the pom.xml file for additional information for Transitive dependencies
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: "pom.xml",
                                type: "pom"]
                            ]
                        );
                    } else {
                        error "*** File: ${artifactPath}, could not be found";
                    }
                }
            }
        }
        
        stage("Approval") {
            steps {
                script {
                    // Deploy to Production?
                    input "Deploy?"
                }
            }
        }

        stage("Deploy") {
            steps {
                script {
                    // Trigger Ansible Tower
                    deploy('hello','prod');
                }
            }
        }        
    }
}

def deploy(String appname, String env) {
    echo "Triggering Ansible Tower"

    callAT();
}

def callAT(){

     ansibleTower(
            towerServer: 'ansibleTower',
            templateType: 'workflow',
            jobTemplate: 'Application Continuous Deployment',
            importTowerLogs: true,
            inventory: 'application_deployment',
            jobTags: '',
            skipJobTags: '',
            limit: '',
            removeColor: false,
            verbose: true,
            credential: '',
            extraVars: '''---
            maven_repository_url: "http://nexus.domain.com:8081/repository/maven-releases/"
            app_group_id: "com.redhat.app"
            app_artifact_id: "hello"
            artifact_extension_type: "jar"
            app_version: "1.0"
            app_folder: "/opt/app"
            web_app_url: "http://app-server1:9080/hello"
            '''
        )
}