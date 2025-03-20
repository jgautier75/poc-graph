package com.acme.jga.graph.services.impl;

import com.acme.jga.graph.parsing.pojo.GodMetaData;
import com.acme.jga.graph.services.api.SchemaApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.JsonSchemaInitStrategy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.janusgraph.core.Multiplicity.*;

@Service
@RequiredArgsConstructor

@Slf4j
public class SchemaImpl implements SchemaApi {
    public static final String VERTEX_GOD = "god";
    private final JanusGraph janusGraph;
    private final Client gremlinClient;

    @Override
    public void createSchema() {
        /*JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        VertexLabel vertexLabel = janusGraphManagement.getVertexLabel(VERTEX_GOD);
        if (vertexLabel == null) {
            janusGraphManagement.makeVertexLabel(VERTEX_GOD).make();
        }
        List<String> keys = Arrays.asList(GodMetaData.SHORT_NAME, GodMetaData.NAME, GodMetaData.CATEGORY, GodMetaData.GENDER, GodMetaData.DESCRIPTION);
        keys.forEach(ppt -> {
            PropertyKey propertyKey = janusGraphManagement.getPropertyKey(ppt);
            if (propertyKey == null) {
                janusGraphManagement.makePropertyKey(ppt).dataType(String.class).make();
            }
        });*/
    }

    public void createEdgeLabels() {
        /*JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        EdgeLabel fatherLabel = janusGraphManagement.getEdgeLabel(GodMetaData.FATHER);
        if (fatherLabel == null) {
            janusGraphManagement.makeEdgeLabel(GodMetaData.FATHER).multiplicity(MANY2ONE).make();
        }
        EdgeLabel motherLabel = janusGraphManagement.getEdgeLabel(GodMetaData.MOTHER);
        if (motherLabel == null) {
            janusGraphManagement.makeEdgeLabel(GodMetaData.MOTHER).multiplicity(MANY2ONE).make();
        }
        EdgeLabel marriedLabel = janusGraphManagement.getEdgeLabel(GodMetaData.MARRIED);
        if (marriedLabel == null) {
            janusGraphManagement.makeEdgeLabel(GodMetaData.MARRIED).multiplicity(MULTI).make();
        }*/
    }

    @Override
    public void initSchema() {
        List<String> jsonLines = IOUtils.readLines(Objects.requireNonNull(SchemaImpl.class.getClassLoader().getResourceAsStream("schema.json")), "UTF-8");
        String jsonSchema = String.join("", jsonLines);
        JsonSchemaInitStrategy.initializeSchemaFromString(janusGraph, jsonSchema);
    }

    @Override
    public void createIndexes() {
        /*JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        List<String> props = Arrays.asList(GodMetaData.SHORT_NAME,
                GodMetaData.NAME,
                GodMetaData.CATEGORY,
                GodMetaData.GENDER);

        props.forEach(p -> {
            String indexName = "by-" + p;
            JanusGraphIndex graphIndex = janusGraphManagement.getGraphIndex(indexName);
            if (graphIndex == null) {
                janusGraphManagement.buildIndex("by-" + p, Vertex.class).addKey(janusGraphManagement.getPropertyKey(p)).buildCompositeIndex();
            }
        });*/
    }
}
