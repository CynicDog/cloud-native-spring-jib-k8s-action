#!/bin/bash

# Configure Keycloak Admin CLI credentials
/opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user $KEYCLOAK_ADMIN \
  --password $KEYCLOAK_ADMIN_PASSWORD

# Create the PolarBookshop realm
/opt/keycloak/bin/kcadm.sh create realms -s realm=PolarBookshop -s enabled=true

# Create roles
/opt/keycloak/bin/kcadm.sh create roles -r PolarBookshop -s name=employee
/opt/keycloak/bin/kcadm.sh create roles -r PolarBookshop -s name=customer

# Create users and assign roles
/opt/keycloak/bin/kcadm.sh create users \
  -r PolarBookshop \
  -s username=isabelle \
  -s firstName=Isabelle \
  -s lastName=Dahl \
  -s enabled=true

/opt/keycloak/bin/kcadm.sh add-roles \
  -r PolarBookshop \
  --uusername isabelle \
  --rolename employee \
  --rolename customer

/opt/keycloak/bin/kcadm.sh create users \
  -r PolarBookshop \
  -s username=bjorn \
  -s firstName=Bjorn \
  -s lastName=Vinterberg \
  -s enabled=true

/opt/keycloak/bin/kcadm.sh add-roles \
  -r PolarBookshop \
  --uusername bjorn \
  --rolename customer

# Set passwords
/opt/keycloak/bin/kcadm.sh set-password -r PolarBookshop --username isabelle --new-password password
/opt/keycloak/bin/kcadm.sh set-password -r PolarBookshop --username bjorn --new-password password

# Register Edge Service as an OAuth2 Client in the PolarBookshop realm
/opt/keycloak/bin/kcadm.sh create clients -r PolarBookshop \
  -s clientId=edge-service \
  -s enabled=true \
  -s publicClient=false \
  -s secret=polar-keycloak-secret \
  -s 'redirectUris=["http://edge-service:80", "http://edge-service:80/login/oauth2/code/*"]'
