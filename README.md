# JanusGraph

```bash
docker-compose -f janus-graph.yml --profile setup up -d
```

Credits: https://github.com/deviantony/docker-elk

Containers


| Service         | Version                       | Port | Description                               |
|-----------------|-------------------------------|------|-------------------------------------------|
| kibana          | 8.17.2                        | 5601 | Kibana                                    |
| elasticsearch   | 8.17.2                        | 9200 | Elasticsearch                             |
| logstash        | 8.17.2                        | 5044 | Logstash                                  |
| janusgraph      | 1.2.0-20250219-143145.6c030f7 | 8182 | JanusGraph                                |
| cassandra       | 4.0.17                        | 9042 | Cassandra                                 |                               
