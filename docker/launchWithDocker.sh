#!/usr/bin/env bash

if [[ "$1" == "--buildAndLaunch" ]]; then
    docker volume create elegant_service_files
    docker build $2 -t elegant-acceleration-service-container .
    docker run -it -p 8080:8080 -v elegant_service_files:/root/Elegant-Acceleration-Service/service_db elegant-acceleration-service-container
elif [[ "$1" == "--launch" ]]; then
    docker run -it -p 8080:8080 -v elegant_service_files:/root/Elegant-Acceleration-Service/service_db elegant-acceleration-service-container
elif [[ "$1" == "--deleteVolume" ]]; then
    docker volume rm elegant_service_files
else
    echo "Please run:"
    echo "  ./docker/launchWithDocker.sh --buildAndLaunch	to build and launch the image, or"
    echo "  ./docker/launchWithDocker.sh --launch		to launch a built image, or"
    echo "  ./docker/launchWithDocker.sh --deleteVolume	to delete the generated volume"
fi

