#!/bin/bash

createTopics() {
  python3 script.py
}

waitForHealth() {
echo "Checking if rest-proxy is available"
i=0
max_attempts=20
while [[ $i -lt $max_attempts ]]
do
  if [[ $(curl -k -s -o /dev/null -w %{http_code} http://localhost:8082/topics) = 200 ]]; then
    echo
    break
  fi
  i="$((i+1))"
  printf '.'
  sleep 2
done
}

start() {
  echo "Start project...."
  pip3 install -r requirements.txt
  docker-compose -f docker-compose.yml up -d
  waitForHealth
  createTopics
}

start