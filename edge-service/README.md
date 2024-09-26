## Defining a security realm on Keycloak 

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
./opt/keycloak/bin/kcadm.sh create clients -r PolarBookshop \
    -s clientId=edge-service \
    -s enabled=true \
    -s publicClient=false \
    -s secret=polar-keycloak-secret \
    -s 'redirectUris=["http://localhost:9000", "http://localhost:9000/login/oauth2/code/*"]'
```

#### 6.2. Containerized Execution (Docker Compose)
```
./opt/keycloak/bin/kcadm.sh create clients -r PolarBookshop \
    -s clientId=edge-service \
    -s enabled=true \
    -s publicClient=false \
    -s secret=polar-keycloak-secret \
    -s 'redirectUris=["http://edge-service:9000", "http://edge-service:9000/login/oauth2/code/*"]'
```

#### 6.3. Kubernetes Execution 

First, start the Minikube tunnel to make both Keycloak and the edge-service accessible. 

```
minikube addons enable ingress 

kubectl apply -f edgeservice-ingress.yml 
kubectl apply -f keycloak-ingress.yml 

minikube tunnel 
```

Next, we need to modify the local DNS configuration. In Kubernetes, services are discovered by their names (such as edge-service and polar-keycloak). This works well for internal communications within the Kubernetes cluster.

However, the Keycloak OAuth2 authentication flow requires requests with a URI that is exposed to the browser outside the cluster. Therefore, we need to configure local DNS so that the hostname resolves to the cluster IP address, which is accessible through the ingress interface we just created.

On terminal, run: 
```
echo "<127.0.0.1 | ip-address> polar-keycloak" | sudo tee -a /etc/hosts
```

If window, run as an administrator:
```
Add-Content C:\Windows\System32\drivers\etc\hosts "127.0.0.1 polar-keycloak"
```

Now with the correct redirect URIs, let's bind the `edge-service` client to KeyCloak.  
```
./opt/keycloak/bin/kcadm.sh create clients -r PolarBookshop \
    -s clientId=edge-service \
    -s enabled=true \
    -s publicClient=false \
    -s secret=polar-keycloak-secret \
    -s 'redirectUris=["http://127.0.0.1", "http://127.0.0.1/login/oauth2/code/*"]'
```
> Ensure the IP address (127.0.0.1) matches the Minikube tunnel gateway (in linux, you can retrieve the specific IP by running `minikube ip`)`.