#!/bin/bash

docker-compose -f docker-compose.yml down
docker volume prune -f