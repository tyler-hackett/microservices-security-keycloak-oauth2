#!/bin/bash
#
export ENV_FILE=chat-webapp.env
cd ~; \
   \
   echo "Looking up QUARKUS_DATASOURCE_PASSWORD..."; \
   echo "QUARKUS_DATASOURCE_PASSWORD=$(aws ssm get-parameter --name 'caf-dbuser-user-pass' --with-decryption --query Parameter.Value --output text)" > $ENV_FILE; \
   \
   echo "Looking up QUARKUS_DATASOURCE_USERNAME..."; \
   echo "QUARKUS_DATASOURCE_USERNAME=$(aws ssm get-parameter --name 'caf-dbuser-username' --query Parameter.Value --output text)" >> $ENV_FILE; \
   \
   echo "Looking up DATABASE_NAME..."; \
   DATABASE_NAME="$(aws ssm get-parameter --name 'caf-dbuser-db-name' --query Parameter.Value --output text)"; \
   \
   echo "Looking up DATABASE_HOST..."; \
   DATABASE_HOST="$(aws ssm get-parameter --name 'caf-dbuser-db-host' --query Parameter.Value --output text)"; \
   \
   echo "QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://${DATABASE_HOST}:5432/${DATABASE_NAME}" >> $ENV_FILE
   \
   echo "Running: sudo docker run -d --name chat-webapp -p 8080:8080 --env-file $ENV_FILE caf/chat-webapp"; \
   sudo docker run -d --name chat-webapp -p 8080:8080 --env-file $ENV_FILE caf/chat-webapp; \
   \
   echo "Removing environment variables file..."; \
   /bin/rm -f $ENV_FILE
