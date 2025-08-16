import json
import logging
import os
from pathlib import Path

import requests as requests
import yaml

REST_PROXY_URL = "http://localhost:8082/v3"
SCHEMA_REGISTRY_URL = "http://localhost:8081"

HEADERS = {
    "Content-Type": "application/json",
    "Accept": "application/json",
}

def get_result(topic_resp):
    if topic_resp.status_code == 201 or topic_resp.status_code == 200:
        return "successfully"
    return "failed"


def get_cluster_id():
    cluster_resp = requests.get(url=f'{REST_PROXY_URL}/clusters')
    return cluster_resp.json().get("data")[0].get("cluster_id")


cluster = get_cluster_id()


def create_topic(topic, config):
    partitions_count = config['topic']['partitions']
    topic_resp = requests.post(url=f'{REST_PROXY_URL}/clusters/{cluster}/topics', headers=HEADERS, json={
        "topic_name": topic,
        "partitions_count": partitions_count,
        "replication_factor": 1,
        "configs": [
            {
                "name": "cleanup.policy",
                "value": "delete"           # default value
            }
        ]
    })
    create_schema(f'{topic}-key', config['subjects'][f'{topic}-key']['schema'])
    create_schema(f'{topic}-value', config['subjects'][f'{topic}-value']['schema'])
    print(f'Creating topic: {topic} -> {get_result(topic_resp)}')


def create_schema(schema_name, schema_body):
    resp = requests.post(url=f'http://localhost:8081/subjects/{schema_name}/versions', headers=HEADERS, json={
        "schema": schema_body
    })
    requests.put(url=f'http://localhost:8081/config/{schema_name}', headers=HEADERS, json={
        "compatibility": "FORWARD_TRANSITIVE"
    })
    return resp


if __name__ == '__main__':
    directory = Path('./topics')
    for item in directory.iterdir():
        print(item.name)
        with open(item, 'r') as file:
            config = yaml.safe_load(file)
            topic_name = config['topic']['name']
            create_topic(topic_name, config)
