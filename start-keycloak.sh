podman run \
       --name keycloak-dev \
       --rm \
       --detach \
       --publish 8081:8080 \
       --env KEYCLOAK_ADMIN=guc \
       --env KEYCLOAK_ADMIN_PASSWORD=potsdam \
       -v $(pwd)/members-backend/src/test/resources/keycloak:/opt/keycloak/data/import:Z \
       quay.io/keycloak/keycloak:26.0 \
       start-dev --import-realm 
