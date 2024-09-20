## Defining a security realm on Keycloak 

#### 1. Execute a bash session in the running container: 
```
kubectl exec -it <POD-name> -- /bin/bash
```

#### 2. Navigate to the CLI script directory and authenticate the session: 
```
cd /opt/keycloakbin
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

