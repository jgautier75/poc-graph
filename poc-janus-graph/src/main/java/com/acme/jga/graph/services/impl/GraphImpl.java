package com.acme.jga.graph.services.impl;

import com.acme.jga.graph.parsing.pojo.God;
import com.acme.jga.graph.parsing.pojo.GodMetaData;
import com.acme.jga.graph.parsing.pojo.GodsList;
import com.acme.jga.graph.parsing.processors.GodsFeeder;
import com.acme.jga.graph.rest.dtos.VertexReadDto;
import com.acme.jga.graph.services.api.GraphApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.acme.jga.graph.GodsConverter.godToPropertyMap;
import static com.acme.jga.graph.GodsConverter.vertexToDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphImpl implements GraphApi {
    private final GodsFeeder godsFeeder;
    private final GraphTraversalSource graphTraversalSource;

    @Override
    public void loadGoads() throws IOException {
        GodsList godsList = godsFeeder.loadGods("gods.json");
        insertGods(godsList.getGods());
        //insertAncestors(godsList.getGods());
    }

    @Override
    public VertexReadDto findGodByShortName(String name) {
        Optional<Vertex> v = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, name).tryNext();
        if (v.isEmpty()) {
            return null;
        } else {
            return vertexToDto(v.get());
        }
    }

    private void insertAncestors(List<God> gods) {
        List<God> godsWithFathers = gods.stream().filter(g -> !Strings.isEmpty(g.getFather()) && !Strings.isEmpty(g.getMother())).toList();
        godsWithFathers.forEach(g -> {
            Optional<Vertex> fatherVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, g.getFather()).tryNext();
            Optional<Vertex> childVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, g.getShortName()).tryNext();
            if (fatherVertex.isPresent() && childVertex.isPresent()) {
                log.info("Creating ancestors for {} with father {}", g.getShortName(), g.getFather());
                graphTraversalSource.tx().begin();
                Object fatherId = fatherVertex.get().id();
                Object childId = childVertex.get().id();
                graphTraversalSource.V(fatherId).addE(GodMetaData.FATHER).to(__.V(childId));
                graphTraversalSource.tx().commit();
            }
        });
    }

    private void insertGods(List<God> gods) {
        for (God g : gods) {
            log.info("Creating god [{}]", g.getName());
            boolean exists = graphTraversalSource.V().has(GodMetaData.NAME, g.getName()).hasNext();
            if (exists) {
                log.info("God [{}] already exist", g.getName());
            } else {
                try {
                    graphTraversalSource.tx().begin();
                    Map<Object, Object> properties = godToPropertyMap(g);
                    graphTraversalSource.addV(g.getName()).property(properties).next();
                    graphTraversalSource.tx().commit();
                } catch (Exception e) {
                    graphTraversalSource.tx().rollback();
                    throw e;
                }
            }
        }
    }

}


