package com.acme.jga.graph.services.api;

import com.acme.jga.graph.rest.dtos.UpdateVertexDto;
import com.acme.jga.graph.rest.dtos.VertexDto;

public interface GraphApi {
    void createSchema();
    String createVertex(VertexDto vertexDto);
    String findVertexByUid(String uid);
    void updateVertex(UpdateVertexDto updateVertexDto);
}
