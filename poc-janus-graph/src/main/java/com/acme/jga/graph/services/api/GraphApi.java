package com.acme.jga.graph.services.api;

import com.acme.jga.graph.rest.dtos.UpdateVertexDto;
import com.acme.jga.graph.rest.dtos.VertexDto;
import com.acme.jga.graph.rest.dtos.VertexReadDto;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface GraphApi {
   /* void createSchema() throws ExecutionException, InterruptedException;
    String createVertex(VertexDto vertexDto);
    String findVertexByUid(String uid);
    void updateVertex(UpdateVertexDto updateVertexDto);*/
    void loadGoads() throws IOException;
    VertexReadDto findGodByShortName(String name);
}
