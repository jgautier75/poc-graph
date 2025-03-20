package com.acme.jga.graph.rest.controllers;

import com.acme.jga.graph.rest.dtos.VertexReadDto;
import com.acme.jga.graph.services.api.GraphApi;
import com.acme.jga.graph.services.api.SchemaApi;
import lombok.RequiredArgsConstructor;
import org.janusgraph.core.JanusGraph;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class GraphController {
    private final JanusGraph janusGraph;
    private final GraphApi graphApi;
    private final SchemaApi schemaApi;

    @PostMapping(value = "/api/v1/schema")
    public ResponseEntity<Void> createSchema() throws IOException {
        schemaApi.initSchema();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/api/v1/graph")
    public ResponseEntity<Void> deleteGraph() {
        graphApi.dropAllData();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/api/v1/gods")
    public ResponseEntity<Void> insertGods() throws IOException {
        graphApi.loadGoads();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/api/v1/gods/{sname}")
    public ResponseEntity<VertexReadDto> godByShortName(@PathVariable(value = "sname") String shortName) {
        return ResponseEntity.ok(graphApi.findGodByShortName(shortName));
    }

    @GetMapping(value = "/api/v1/graph")
    public ResponseEntity<Void> exportGraph() {
        graphApi.export();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
