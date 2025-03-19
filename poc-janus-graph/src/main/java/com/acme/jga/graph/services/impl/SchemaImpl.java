package com.acme.jga.graph.services.impl;

import com.acme.jga.graph.parsing.pojo.GodMetaData;
import com.acme.jga.graph.services.api.SchemaApi;
import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static org.janusgraph.core.Multiplicity.*;

@Service
@RequiredArgsConstructor
public class SchemaImpl implements SchemaApi {
    public static final String VERTEX_GOD = "god";

    @Override
    public void createSchema(JanusGraphManagement management) {
        VertexLabel vertexLabel = management.getVertexLabel(VERTEX_GOD);
        if (vertexLabel == null) {
            management.makeVertexLabel(VERTEX_GOD).make();
        }
        List<String> keys = Arrays.asList(GodMetaData.SHORT_NAME, GodMetaData.NAME, GodMetaData.CATEGORY, GodMetaData.GENDER, GodMetaData.DESCRIPTION);
        keys.forEach(ppt -> {
            PropertyKey propertyKey = management.getPropertyKey(ppt);
            if (propertyKey == null) {
                management.makePropertyKey(ppt).dataType(String.class).make();
            }
        });
    }

    public void createEdgeLabels(JanusGraphManagement management) {
        EdgeLabel fatherLabel = management.getEdgeLabel(GodMetaData.FATHER);
        if (fatherLabel == null) {
            management.makeEdgeLabel(GodMetaData.FATHER).multiplicity(MANY2ONE).make();
        }
        EdgeLabel motherLabel = management.getEdgeLabel(GodMetaData.MOTHER);
        if (motherLabel == null) {
            management.makeEdgeLabel(GodMetaData.MOTHER).multiplicity(MANY2ONE).make();
        }
        EdgeLabel marriedLabel = management.getEdgeLabel(GodMetaData.MARRIED);
        if (marriedLabel == null) {
            management.makeEdgeLabel(GodMetaData.MARRIED).multiplicity(MULTI).make();
        }
    }

    @Override
    public void createIndexes(JanusGraphManagement management) {
        List<String> props = Arrays.asList(GodMetaData.SHORT_NAME,
                GodMetaData.NAME,
                GodMetaData.CATEGORY,
                GodMetaData.GENDER);

        props.forEach(p -> {
            String indexName = "by-" + p;
            JanusGraphIndex graphIndex = management.getGraphIndex(indexName);
            if (graphIndex == null) {
                management.buildIndex("by-" + p, Vertex.class).addKey(management.getPropertyKey(p)).buildCompositeIndex();
            }
        });
    }
}
