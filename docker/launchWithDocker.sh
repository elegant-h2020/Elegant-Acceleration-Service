#!/usr/bin/env bash

IMAGE=acceleration-service-container
TAG_VERSION=1.0

if [[ "$1" == "--buildAndLaunch" ]]; then
    docker volume create elegant_service_files
    docker build --no-cache -t $IMAGE .
    docker tag $IMAGE beehivelab/$IMAGE:$TAG_VERSION
    docker tag $IMAGE beehivelab/$IMAGE:latest
    docker run -it -p 8080:8080 -v "$PWD":/elegant_service_files beehivelab/acceleration-service-container:latest
elif [[ "$1" == "--launch" ]]; then
    docker run -it -p 8080:8080 -v "$PWD":/elegant_service_files beehivelab/acceleration-service-container:latest
elif [[ "$1" == "--deleteVolume" ]]; then
    docker volume rm elegant_service_files
else
    echo "Please run:"
    echo "  ./docker/launchWithDocker.sh --buildAndLaunch	to build and launch the image, or"
    echo "  ./docker/launchWithDocker.sh --launch		to launch a built image, or"
    echo "  ./docker/launchWithDocker.sh --deleteVolume	to delete the generated volume"
fi

