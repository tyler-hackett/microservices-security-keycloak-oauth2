#!/bin/bash

ENV_FILE=db-server.env

echo "Retrieving Database Configuration..."
echo "POSTGRES_PASSWORD=$(aws ssm get-parameter --name 'caf-dbserver-admin-pass' --with-decryption --query Parameter.Value --output text)" > $ENV_FILE
echo "POSTGRES_USER=$(aws ssm get-parameter --name 'caf-dbuser-username' --query Parameter.Value --output text)" >> $ENV_FILE
echo "POSTGRES_DB=$(aws ssm get-parameter --name 'caf-dbuser-db-name' --query Parameter.Value --output text)" >> $ENV_FILE

echo "Launching Database Container..."
sudo docker run -d \
  --name db-server \
  -v /data:/var/lib/postgresql/ \
  -p 5432:5432 \
  --env-file $ENV_FILE \
  postgres:latest

rm -f $ENV_FILE
echo "Database container started successfully."