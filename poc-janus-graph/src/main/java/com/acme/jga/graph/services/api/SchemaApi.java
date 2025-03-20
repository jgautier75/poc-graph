package com.acme.jga.graph.services.api;

import java.io.IOException;

public interface SchemaApi {

    void createSchema();

    void createIndexes();

    void createEdgeLabels();

    void initSchema() throws IOException;

}
