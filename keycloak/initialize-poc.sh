#!/bin/bash

echo "Creating PoC Users, Role Model, User Role Assigments and Clients"

/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD

# Users
/opt/keycloak/bin/kcadm.sh create users -r master -s username=paula -s firstName=Paula -s lastName=Von -s enabled=true -s email=paula@demo.com
/opt/keycloak/bin/kcadm.sh set-password -r master --username paula --new-password demo1234!
/opt/keycloak/bin/kcadm.sh create users -r master -s username=peter -s firstName=Peter -s lastName=Anderson -s enabled=true -s email=peter@demo.com
/opt/keycloak/bin/kcadm.sh set-password -r master --username peter --new-password demo1234!
/opt/keycloak/bin/kcadm.sh create users -r master -s username=richard  -s firstName=Richard -s lastName=Miles -s enabled=true -s email=richard@demo.com
/opt/keycloak/bin/kcadm.sh set-password -r master --username richard --new-password demo1234!

# Role model
/opt/keycloak/bin/kcadm.sh create roles -r master -s name=admin-catalog -s 'description=Admin Catalog'
/opt/keycloak/bin/kcadm.sh create roles -r master -s name=analyst-catalog -s 'description=Analyst Catalog'
/opt/keycloak/bin/kcadm.sh create roles -r master -s name=view-product -s 'description=View product'
/opt/keycloak/bin/kcadm.sh create roles -r master -s name=edit-product -s 'description=Edit product'
/opt/keycloak/bin/kcadm.sh add-roles --rname analyst-catalog --rolename view-product -r master
/opt/keycloak/bin/kcadm.sh add-roles --rname admin-catalog --rolename view-product -r master
/opt/keycloak/bin/kcadm.sh add-roles --rname admin-catalog --rolename edit-product -r master

# User Role Assignments
/opt/keycloak/bin/kcadm.sh add-roles -r master --uusername paula --rolename analyst-catalog
/opt/keycloak/bin/kcadm.sh add-roles -r master --uusername richard --rolename admin-catalog
