package com.acme.jga.graph.services.api;

import org.janusgraph.core.schema.JanusGraphManagement;

public interface SchemaApi {

    void createSchema(JanusGraphManagement management);

    void createEdgeLabels(JanusGraphManagement management);

    void createIndexes(JanusGraphManagement management);

}
