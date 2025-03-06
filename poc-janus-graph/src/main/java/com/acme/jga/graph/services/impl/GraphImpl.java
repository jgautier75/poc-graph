package com.acme.jga.graph.services.impl;

import com.acme.jga.graph.parsing.pojo.God;
import com.acme.jga.graph.parsing.pojo.GodMetaData;
import com.acme.jga.graph.parsing.pojo.GodsList;
import com.acme.jga.graph.parsing.processors.GodsFeeder;
import com.acme.jga.graph.rest.dtos.UpdateVertexDto;
import com.acme.jga.graph.rest.dtos.VertexDto;
import com.acme.jga.graph.rest.dtos.VertexReadDto;
import com.acme.jga.graph.services.api.GraphApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.ConsistencyModifier;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphImpl implements GraphApi {
    private final JanusGraph janusGraph;
    private final GodsFeeder godsFeeder;

    @Override
    public void createSchema() throws ExecutionException, InterruptedException {
        janusGraph.tx().rollback();
        JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        createPropertyKeyWithIndex(janusGraphManagement, GodMetaData.NAME, String.class);
        createPropertyKeyWithIndex(janusGraphManagement, GodMetaData.UUID, String.class);
        createPropertyKeyWithIndex(janusGraphManagement, GodMetaData.SHORT_NAME, String.class);
        createPropertyKeyWithIndex(janusGraphManagement, GodMetaData.CATEGORY, String.class);

        List<String> indexes = Arrays.asList(GodMetaData.NAME + "-unique", GodMetaData.UUID + "-unique", GodMetaData.SHORT_NAME + "-unique", GodMetaData.CATEGORY + "-unique");
        indexes.forEach(index -> {
            try {
                ManagementSystem.awaitGraphIndexStatus(janusGraph, index).call();
            } catch (InterruptedException e) {
                log.error("Graph status", e);
            }
        });

        janusGraphManagement = janusGraph.openManagement();
        for (String idx : indexes) {
            reindex(janusGraphManagement, idx);
        }
        janusGraphManagement.commit();
    }

    @Override
    public String createVertex(VertexDto vertexDto) {
        String uuid = UUID.randomUUID().toString();
        try (JanusGraphTransaction tx = janusGraph.newTransaction()) {
            JanusGraphVertex vertex = tx.addVertex(vertexDto.getName());
            vertex.property(GodMetaData.UUID, uuid);
            tx.commit();
        }
        return uuid;
    }

    @Override
    public String findVertexByUid(String uid) {
        Vertex v = janusGraph.traversal().V().has(GodMetaData.UUID, uid).next();
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

    @Override
    public void loadGoads() throws IOException {
        GodsList godsList = godsFeeder.loadGods("gods.json");
        insertGods(godsList.getGods());
    }

    @Override
    public VertexReadDto findGodByShortName(String name) {
        Optional<Vertex> v = janusGraph.traversal().V().has(GodMetaData.SHORT_NAME, name).tryNext();
        if (v.isEmpty()) {
            return null;
        } else {
            return vertexToDto(v.get());
        }
    }

    private void insertGods(List<God> gods) {
        for (God g : gods) {
            log.info("Creating god [{}]", g.getName());
            boolean exists = janusGraph.traversal().V().has(GodMetaData.NAME, g.getName()).hasNext();
            if (exists) {
                log.info("God [{}] already exist", g.getName());
            } else {
                try {
                    janusGraph.tx().begin();
                    Map<Object, Object> properties = godToPropertyMap(g);
                    janusGraph.traversal().addV(g.getName()).property(properties).next();
                    janusGraph.tx().commit();
                } catch (Exception e) {
                    janusGraph.tx().rollback();
                    throw e;
                }
            }
        }
    }

    private VertexReadDto vertexToDto(Vertex v) {
        VertexReadDto vertexReadDto = new VertexReadDto();
        Iterator<VertexProperty<Object>> vertextIterator = v.properties();
        while (vertextIterator.hasNext()) {
            VertexProperty<Object> vprop = vertextIterator.next();
            switch (vprop.label()) {
                case "uuid":
                    vertexReadDto.setUuid(vprop.value().toString());
                    break;
                case "shortName":
                    vertexReadDto.setShortName(vprop.value().toString());
                    break;
                case "gender":
                    vertexReadDto.setGender(vprop.value().toString());
                    break;
                case "description":
                    vertexReadDto.setDescription(vprop.value().toString());
                    break;
                case "category":
                    vertexReadDto.setCategory(vprop.value().toString());
                    break;
                case "father":
                    vertexReadDto.setFather(vprop.value().toString());
                    break;
                case "mother":
                    vertexReadDto.setMother(vprop.value().toString());
                    break;
            }
        }
        return vertexReadDto;
    }

    private Map<Object, Object> godToPropertyMap(God g) {
        Map<Object, Object> properties = new HashMap<>();
        properties.put(GodMetaData.SHORT_NAME, g.getShortName());
        properties.put(GodMetaData.GENDER, g.getGender());
        if (g.getDescription() != null) {
            properties.put(GodMetaData.DESCRIPTION, g.getDescription());
        }
        if (g.getFather() != null) {
            properties.put(GodMetaData.FATHER, g.getFather());
        }
        if (g.getMother() != null) {
            properties.put(GodMetaData.MOTHER, g.getMother());
        }
        if (g.getCategory() != null) {
            properties.put(GodMetaData.CATEGORY, g.getCategory());
        }
        return properties;
    }

    private void createPropertyKeyWithIndex(JanusGraphManagement janusGraphManagement, String keyName, Class<?> dataType) throws InterruptedException, ExecutionException {
        if (!janusGraphManagement.containsPropertyKey(keyName)) {
            final PropertyKey propertyKey = janusGraphManagement.makePropertyKey(keyName).dataType(dataType).make();
            JanusGraphManagement.IndexBuilder indexBuilder = janusGraphManagement.buildIndex(keyName, Vertex.class).addKey(propertyKey).unique();
            JanusGraphIndex index = indexBuilder.buildCompositeIndex();
            janusGraphManagement.setConsistency(index, ConsistencyModifier.LOCK);
        } else {
            PropertyKey propertyKey = janusGraphManagement.getPropertyKey(keyName);
            String indexName = keyName + "-unique";
            log.info("Build index named [{}]", indexName);
            JanusGraphIndex graphIndex = janusGraphManagement.getGraphIndex(indexName);
            if (graphIndex == null) {
                graphIndex = janusGraphManagement.buildIndex(indexName, Vertex.class).addKey(propertyKey).buildCompositeIndex();
                janusGraphManagement.commit();
                log.info("Waiting graph index named [{}]", indexName);
                ManagementSystem.awaitGraphIndexStatus(janusGraph, indexName).call();
            }
        }
    }

    private void reindex(JanusGraphManagement mgmt, String indexName) throws ExecutionException, InterruptedException {
        log.info("Updating index named [{}]", indexName);
        mgmt.updateIndex(mgmt.getGraphIndex(indexName), SchemaAction.REINDEX).get();
    }

}


