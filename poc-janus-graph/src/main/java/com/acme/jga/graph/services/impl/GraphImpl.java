package com.acme.jga.graph.services.impl;

import com.acme.jga.graph.rest.dtos.UpdateVertexDto;
import com.acme.jga.graph.rest.dtos.VertexDto;
import com.acme.jga.graph.services.api.GraphApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphImpl implements GraphApi {
    private final JanusGraph janusGraph;

    @Override
    public void createSchema() {
        JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        final PropertyKey name = janusGraphManagement.makePropertyKey("name").dataType(String.class).make();
        JanusGraphManagement.IndexBuilder nameIndexBuilder = janusGraphManagement.buildIndex("name", Vertex.class).addKey(name).unique();
        JanusGraphIndex nameIndex = nameIndexBuilder.buildCompositeIndex();
        janusGraphManagement.setConsistency(nameIndex, ConsistencyModifier.LOCK);

        final PropertyKey uuid = janusGraphManagement.makePropertyKey("uuid").dataType(String.class).make();
        JanusGraphManagement.IndexBuilder uuidIndexBuilder = janusGraphManagement.buildIndex("uuid", Vertex.class).addKey(uuid).unique();
        JanusGraphIndex uuidIndex = uuidIndexBuilder.buildCompositeIndex();
        janusGraphManagement.setConsistency(uuidIndex, ConsistencyModifier.LOCK);
    }

    @Override
    public String createVertex(VertexDto vertexDto) {
        String uuid = UUID.randomUUID().toString();
        try {
            janusGraph.tx().begin();
            janusGraph.traversal().addV(vertexDto.getName()).property("uuid", uuid).next();
            janusGraph.tx().commit();
        } catch (Exception e) {
            janusGraph.tx().rollback();
            throw e;
        }
        return uuid;
    }

    @Override
    public String findVertexByUid(String uid) {
        Vertex v = janusGraph.traversal().V().has("uuid", uid).next();
        Optional<Map<Object, Object>> mapGraphTraversal = janusGraph.traversal().V(v).valueMap().tryNext();
        final StringBuilder sb = new StringBuilder();
        mapGraphTraversal.ifPresent((m) -> m.forEach((key, value) -> sb.append("[key=").append(key).append("], value=[").append(value).append("]")));
        return sb.toString();
    }

    @Override
    public void updateVertex(UpdateVertexDto updateVertexDto) {
        Map<Object, Object> props = new HashMap<>();
        updateVertexDto.getProperties().forEach(vp -> props.put(vp.getKey(), vp.getValue()));
        janusGraph.traversal().V(updateVertexDto.getName()).property(props);
    }
}
