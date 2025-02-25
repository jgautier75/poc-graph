package com.acme.jga.graph.rest;

import com.acme.jga.graph.rest.dtos.UpdateVertexDto;
import com.acme.jga.graph.rest.dtos.VertexDto;
import com.acme.jga.graph.services.api.GraphApi;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GraphController {
    private final GraphApi graphApi;

    @PostMapping(value="/api/v1/schema")
    public ResponseEntity<Void> createSchema(){
        graphApi.createSchema();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/api/v1/vertex")
    public ResponseEntity<String> createVertex(@RequestBody VertexDto vertexDto) {
        String vertexId = graphApi.createVertex(vertexDto);
        return ResponseEntity.ok(vertexId);
    }

    @GetMapping(value = "/api/v1/vertex/{uid}")
    public ResponseEntity<String> findVertex(@PathVariable(value = "uid") String uid) {
        String vertexData = graphApi.findVertexByUid(uid);
        return ResponseEntity.ok(vertexData);
    }

    @PutMapping(value = "/api/v1/vertex/{uid}")
    public ResponseEntity<Void> updateVertex(@PathParam(value = "uid") String uid, @RequestBody UpdateVertexDto updateVertexDto) {
        graphApi.updateVertex(updateVertexDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
