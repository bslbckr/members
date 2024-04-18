podman run \
       --name mariadb-dev \
       --rm \
       --detach \
       --publish 3306:3306 \
       --env MARIADB_USER=guc_members \
       --env MARIADB_PASSWORD=test \
       --env MARIADB_DATABASE=guc_members \
       --env MARIADB_ROOT_PASSWORD=test \
       docker.io/library/mariadb:10.6
