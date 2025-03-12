package com.acme.jga.graph.services.impl;

import com.acme.jga.graph.parsing.pojo.GodMetaData;
import com.acme.jga.graph.services.api.SchemaApi;
import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static org.janusgraph.core.Multiplicity.MANY2ONE;

@Service
@RequiredArgsConstructor
public class SchemaImpl implements SchemaApi {
    public static final String VERTEX_GOD = "god";

    @Override
    public void createSchema(JanusGraphManagement management) {
        management.makeVertexLabel(VERTEX_GOD).make();
        List<String> keys = Arrays.asList(GodMetaData.SHORT_NAME, GodMetaData.NAME, GodMetaData.CATEGORY, GodMetaData.GENDER, GodMetaData.DESCRIPTION);
        keys.forEach(ppt -> management.makePropertyKey(ppt).dataType(String.class).make());
    }

    public void createEdgeLabels(JanusGraphManagement management) {
        management.makeEdgeLabel(GodMetaData.FATHER).multiplicity(MANY2ONE).make();
        management.makeEdgeLabel(GodMetaData.MOTHER).multiplicity(MANY2ONE).make();
    }

    @Override
    public void createIndexes(JanusGraphManagement management) {
        List<String> props = Arrays.asList(GodMetaData.SHORT_NAME,
                GodMetaData.NAME,
                GodMetaData.CATEGORY,
                GodMetaData.GENDER);
        props.forEach(p -> management.buildIndex("by-" + p, Vertex.class).addKey(management.getPropertyKey(p)).buildCompositeIndex());
    }
}
