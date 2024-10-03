
### Index

1. [Version Table](#version-table)
2. [Minikube Deployment](#minikube-deployment)
     1. [Prerequisites](#1-prerequisites)
     1. [Pull the Docker Image](#2-pull-the-docker-image)
     2. [Start Minikube](#3-start-minikube)
     3. [Load the Image onto Minikube (Optional)](#4-load-the-image-onto-minikube-optional)
        - [For Linux/macOS](#41-for-linuxmacos)
        - [For Windows](#42-for-windows)
     4. [Apply the Manifest Files](#5-apply-the-manifest-files)
     5. [Access the Application via Ingress](#6-access-the-application-via-ingress)
3. [Defining a Security Realm on Keycloak](#defining-a-security-realm-on-keycloak)
   - [1. Execute a bash session in the running container](#1-execute-a-bash-session-in-the-running-container)
   - [2. Navigate to the CLI script directory and authenticate the session](#2-navigate-to-the-cli-script-directory-and-authenticate-the-session)
   - [3. Create a dedicated realm for a service](#3-create-a-dedicated-realm-for-a-service)
   - [4. Create roles](#4-create-roles)
   - [5. Add users and credentials](#5-add-users-and-credentials)
   - [6. Register Edge Service as OAuth2 Client in the realm](#6-register-edge-service-as-oauth2-client-in-the-realm)
     - [6.1. Local Execution](#61-local-execution)
     - [6.2. Containerized Execution (Docker Compose)](#62-containerized-execution-docker-compose)
     - [6.3. Kubernetes Execution](#63-kubernetes-execution)
4. [Create GitHub Identity Provider (GitHub Apps)](#create-github-identity-provider-github-apps)
   - [Create a GitHub App with Email Read Permission](#create-a-github-app-with-email-read-permission)
   - [Register GitHub App Client ID and Secret as Keycloak Client information](#register-github-app-client-id-and-secret-as-keycloak-client-information)

# Version Table

|             | Version       |
|-------------|---------------|
| JDK         | 20 (Temurin)  |
| Spring Boot | 3.0.0         |
| Gradle      | 8.10          |
| Jib         | 3.4.3         |
| Minikube    | 1.32.0        |

# Minikube Deployment 

Deploy a Spring Boot application using Minikube on macOS and Windows with Ingress and manifest files.

---

## 1. Prerequisites

- Docker
- Minikube
- kubectl
- HTTPie or curl

---

## 2. Pull the Docker Image
```bash
docker pull ghcr.io/cynicdog/cloud-native-spring-jib-k8s-action/catalog-service:latest
```
> 1. Make sure to clear the previous credentials on `ghcr.io` in your local Docker context by running `docker logout ghcr.io`.
> 2. You may specify the build platform of the image by adding `--platform` tag with the value `linux/amd64` or `linux/arm64`.

## 3. Start Minikube
```bash
minikube start --cpus 2 --memory 4g --driver docker
```
> Assign compute resources at your need. 

## 4. Load the Image onto Minikube (Optional) 
> You may skip this step if services are to be deployed declaratively using manifest files.

### 4.1. For Linux/macOS:
4.1.1. Set Docker to use Minikube’s environment:
```bash
eval $(minikube docker-env)
```

4.1.2. Load the image into Minikube:
```bash
minikube image load cynicdog/catalog-service:latest
```

### 4.2. For Windows:
4.2.1. Save the image as `.tar`:
```bash
docker image save -o catalog-service-image.tar cynicdog/catalog-service:latest
```

4.2.2. Load the image into Minikube:
```bash
minikube image load catalog-service-image.tar
```

## 5. Apply the Manifest Files

5.1. Enable Ingress on Minikube (if not done already):
```bash
minikube addons enable ingress
```

5.2. Apply the Kubernetes deployment and service manifest:
```bash
kubectl apply -f ./manifest/
```

## 6. Access the Application via Ingress

Once the Ingress is applied, you can retrieve the Minikube IP:
```bash
minikube ip
```

Then, expose the cluster to the local environment:
```bash
minikube tunnel
```

# Defining a security realm on Keycloak 

#### 1. Execute a bash session in the running container: 
```
kubectl exec -it <POD-name> -- /bin/bash
```

#### 2. Navigate to the CLI script directory and authenticate the session: 
```
cd /opt/keycloak/bin
./kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user {USER_NAME} \
    --password {PASSWORD}
```

#### 3. Create a dedicated realm for a service:
```
./kcadm.sh create realms -s realm={REALM_NAME} -s enabled=true
```

#### 4. Create roles: 
```
$ ./kcadm.sh create roles -r {REALM_NAME} -s name={ROLE_NAME}
```

#### 5. Add users and credentials:
```
./kcadm.sh create users -r {REALM_NAME} \
    -s username={USERNAME} \
    -s firstName={FIRST_NAME} \
    -s lastName={LAST_NAME} \
    -s enabled=true
 
./kcadm.sh add-roles -r {REALM_NAME} \
    --uusername {USERNAME} \
    --rolename {ROLE_NAME_1} \
    --rolename {ROLE_NAME_2}
    
./kcadm.sh set-password -r {REALM_NAME} \
   --username {USERNAME} \
   --new-password {PASSWORD}
```

### 6. Register Edge Service as OAuth2 Client in the realm 
Make sure you properly set the redirect URIs based on execution context. 

#### 6.1. Local Execution 
```
    ./opt/keycloak/bin/kcadm.sh create clients -r {REALM_NAME} \
    -s clientId={CLIENT_SERVICE_NAME} \
    -s enabled=true \
    -s publicClient=false \
    -s secret={SOME_SECRET} \
    -s 'redirectUris=["http://localhost:9000", "http://localhost:9000/login/oauth2/code/*"]'
```

#### 6.2. Containerized Execution (Docker Compose)
```
./opt/keycloak/bin/kcadm.sh create clients -r {REALM_NAME} \
    -s clientId={CLIENT_SERVICE_NAME} \
    -s enabled=true \
    -s publicClient=false \
    -s secret={SOME_SECRET} \
    -s 'redirectUris=["http://edge-service:9000", "http://edge-service:9000/login/oauth2/code/*"]'
```

#### 6.3. Kubernetes Execution 

First, start the Minikube tunnel to make both Keycloak and the edge-service accessible. 

```
minikube addons enable ingress 

kubectl apply -f ingress-bff.yml 
kubectl apply -f ingress-keycloak.yml 

minikube tunnel 
```

Next, we need to modify the local DNS configuration. In Kubernetes, services are discovered by their names (such as edge-service and polar-keycloak). This works well for internal communications within the Kubernetes cluster.

However, the Keycloak OAuth2 authentication flow requires requests with a URI that is exposed to the browser outside the cluster. Therefore, we need to configure local DNS so that the hostname resolves to the cluster IP address, which is accessible through the ingress interface we just created.

On terminal, run: 
```
echo "<127.0.0.1 | ip-address> keycloak" | sudo tee -a /etc/hosts
```

If window, run as an administrator:
```
Add-Content C:\Windows\System32\drivers\etc\hosts "127.0.0.1 keycloak"
```

Now with the correct redirect URIs, let's bind the `edge-service` client to KeyCloak.  
```
./opt/keycloak/bin/kcadm.sh create clients -r {REALM_NAME} \
    -s clientId={CLIENT_SERVICE_NAME} \
    -s enabled=true \
    -s publicClient=false \
    -s secret={SOME_SECRET} \
    -s 'redirectUris=["http://127.0.0.1", "http://127.0.0.1/login/oauth2/code/*"]'
```
> Ensure the IP address (127.0.0.1) matches the Minikube tunnel gateway (in linux, you can retrieve the specific IP by running `minikube ip`)`.

# Create GitHub Identity Provider (GitHub Apps) 

#### Create a GitHub App with Email Read Permission 

<details>
  <summary>Register Callback URL</summary>
  <img src="https://github.com/user-attachments/assets/903abd67-2e19-4035-9266-9183c63a8247"></img>
</details>

<details>
  <summary>Add Read-Access Scope on Email</summary>
  <img src="https://github.com/user-attachments/assets/6a1e10c1-79d0-4928-8f33-b244e3505034"></img>
</details>

#### Register GitHub App Client ID and Secret as Keyclock Client information 
<details>
  <summary>Retrieve Client Credentials</summary>
  <img src="https://github.com/user-attachments/assets/6a1e10c1-79d0-4928-8f33-b244e3505034"></img>
</details>

With client credentials above, run the following command on your KeyCloak admin agent:  
```
/opt/keycloak/bin/kcadm.sh create identity-provider/instances \
	-r {REALM_NAME} \
	-s alias=github \
	-s providerId=github \
	-s enabled=true  \
	-s 'config.useJwksUrl="true"' \
	-s config.clientId={CLIENT_ID} \
	-s config.clientSecret={CLIENT_SECRET}
```
