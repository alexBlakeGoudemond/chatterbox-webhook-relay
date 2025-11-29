# Variables
APP_NAME = chatterbox
SRC_DIR = src

# Default target if you just run "make"
all: help

help:
	@echo "'Make Help': Specify an argument: make <argument>"

# dockerissimo: custom script to compose containers
docker-up:
	dockerissimo -f docker/docker-compose.yml -p chatterbox-container-group

# dockerissimo: custom script to delete containers and images
docker-down:
	dockerissimo -f docker/docker-compose.yml -p chatterbox-container-group -c


