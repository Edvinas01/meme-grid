#!/usr/bin/env bash

# Build executable .jar file.
./gradlew shadowJar

localName=meme-grid

# Build local docker image.
docker build -f Dockerfile -t ${localName} .

# Push local image to Docker Hub.
docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}
docker tag ${localName} ${DOCKER_REPOSITORY}:latest
docker push ${DOCKER_REPOSITORY}:latest
