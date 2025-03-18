package com.acme.jga.graph.services.api;

import com.acme.jga.graph.rest.dtos.VertexReadDto;

import java.io.IOException;

public interface GraphApi {
    void loadGoads() throws IOException;
    VertexReadDto findGodByShortName(String name);
    void dropAllData();
    void export();
}
