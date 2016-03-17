#!/bin/bash
# Builds a Docker image of HTRC-Portal
# Its possible to run 'play dist' during Docker image build. But this cause play to download
# dependencies everytime 'play dist' runs because java base image doesn't include required dependencies.
# This adds extra build time and increase the resource consumption (e.g. network resources). Running play dist
# in build machine allows to re-use already downloaded dependencies.

# Run play clean first
play clean-all

# Next run play dist
play dist

# Copy Dockerfile to target/docker
mkdir -p ./target/docker
cp docker/Dockerfile ./target/docker
cp docker/portal.sh ./target/docker

# Copy Portal distribution to target/docker
cp ./target/universal/htrc-portal-3.2-SNAPSHOT.zip ./target/docker

# CD to target/docker
cd target/docker

# Then run docker build
docker build -t htrc/portal:alpine .
