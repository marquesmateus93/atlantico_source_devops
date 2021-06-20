# Infrastructure Deploy

The following lines show the project folders and describe their files.

### terraform

- **main .tf:**  Deploy a VM in GCP Cloud from preconfigured SO image.

### jenkins

- **deploy.groovy:** Compile Hello World app, build Docker image and deploy on GCP VM.

### dockerfiles/tomcat/9

  - **Dockerfile:** Build Hello World Docker image.

  - **server.xml:** Adapted Tomcat file.