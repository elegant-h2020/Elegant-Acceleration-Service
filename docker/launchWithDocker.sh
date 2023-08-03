#!/usr/bin/env bash

docker volume create elegant_service_files
docker build $1 -t elegant-acceleration-service-container .
docker run -it -p 8080:8080 -v elegant_service_files:/root/Elegant-Acceleration-Service/service_db elegant-acceleration-service-container
