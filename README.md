# JanusGraph

```bash
docker-compose -f janus-graph.yml up -d
```

Credits: https://github.com/deviantony/docker-elk

Containers


| Service               | Version | Port | Description      |
|-----------------------|---------|------|------------------|
| janusgraph            | 1.1.0   | 8182 | JanusGraph       |
| cassandra             | 4.0.17  | 9042 | Cassandra        |
| elasticsearch         | 8.17.2  | 9200 | Elasticsearch    |
| janusgraph-visualizer | -       | 3001 | Graph visualizer |
| graphexp              | -       | 8001 | Graph visualizer |

**Connecting to Janus Graph**

* First enter janus graph container

```bash
docker exec -it jce-janusgraph bash
```

* Then launch gremlin

```bash
bin/gremlin.sh
```

* Then connect to JanuGraph backend (cassandra)

```bash
graph = JanusGraphFactory.open('cql:cassandra')
g = graph.traversal()
g.V().count()
```

* Once done, launch spring-boot PocGraph application

* To initialize schema, connect to gremlin console and import init.groovy script

```bash
:load /tmp/init.groovy
```

curl - X POST http://localhost:8080/api/v1/schema

** Gremlin

Deleting all vertices
```bash
g.V().drop()
```

** Visualizers:

JanusGraph visualizer:  http://localhost:3001/

![JanusGraphVisualizer](poc-janus-graph/docs/janusgraph.png)

GraphExp: http://localhost:8001/graphexp.html

![GraphExp](poc-janus-graph/docs/graphexp.png)

Connection: 
* Server Address: 192.168.1.x
* Server Port: 8182
* Protocol: websocket
* Gremlin version: 3.4

** Export:

Either connect to gremlin console in jce-janusgraph container 

```bash
graph = JanusGraphFactory.open('cql:cassandra')
graph.io(IoCore.graphml()).writeGraph("test.graphml");
```

Or use export REST function: 

```
GET http://localhost:8080/api/v1/graph
```

Then you can import graml file into Gephi ==> https://gephi.org/

** Gremlin tips

```bash
graph = JanusGraphFactory.open('cql:cassandra')
mgmt = graph.openManagement()
mgmt.makeEdgeLabel("father").make()
mgmt.makeEdgeLabel("mother").make()
mgmt.makeEdgeLabel("married").make()
mgmt.makePropertyKey("shortName").dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey("category").dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey("description").dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.makePropertyKey("gender").dataType(String.class).cardinality(Cardinality.SINGLE).make()
mgmt.buildIndex("byShortName", Vertex.class).addKey(mgmt.getPropertyKey("shortName")).buildCompositeIndex()
mgmt.buildIndex("byName", Vertex.class).addKey(mgmt.getPropertyKey("name")).buildCompositeIndex()
mgmt.buildIndex("byCategory", Vertex.class).addKey(mgmt.getPropertyKey("category")).buildCompositeIndex()
```