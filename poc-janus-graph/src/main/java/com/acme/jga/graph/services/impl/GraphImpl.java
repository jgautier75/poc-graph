package com.acme.jga.graph.services.impl;

import com.acme.jga.graph.GodsConverter;
import com.acme.jga.graph.parsing.pojo.God;
import com.acme.jga.graph.parsing.pojo.GodMetaData;
import com.acme.jga.graph.parsing.pojo.GodsList;
import com.acme.jga.graph.parsing.processors.GodsFeeder;
import com.acme.jga.graph.rest.dtos.VertexReadDto;
import com.acme.jga.graph.services.api.GraphApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.graphdb.tinkerpop.io.graphson.JanusGraphSONModuleV2d0;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.acme.jga.graph.GodsConverter.godToPropertyMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphImpl implements GraphApi {
    private final GodsFeeder godsFeeder;
    private final GraphTraversalSource graphTraversalSource;
    private final JanusGraph janusGraph;

    @Override
    public void loadGoads() throws IOException {
        GodsList godsList = godsFeeder.loadGods("gods.json");
        createVertices(godsList.getGods());
        insertAncestors(godsList.getGods());
    }

    @Override
    public VertexReadDto findGodByShortName(String name) {
        Optional<Vertex> v = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, name).tryNext();
        return v.map(GodsConverter::vertexToDto).orElse(null);
    }

    @Override
    public void dropAllData() {
        graphTraversalSource.tx().begin();
        graphTraversalSource.V().drop().iterate();
        graphTraversalSource.tx().commit();
    }

    @Override
    public void export() {
        String targetFile = System.getProperty("user.dir") + "/test.graphml";
        log.info("Exporting graph to file {}", targetFile);
        try {
            graphTraversalSource.getGraph().io(IoCore.graphml()).writeGraph(targetFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void insertAncestors(List<God> gods) {
        List<God> godsWithFathers = gods.stream().filter(g -> !Strings.isEmpty(g.getFather())).toList();
        godsWithFathers.forEach(g -> addAncestor(g, g.getFather(), GodMetaData.FATHER));
        List<God> godsWithMothers = gods.stream().filter(g -> !Strings.isEmpty(g.getMother())).toList();
        godsWithMothers.forEach(g -> addAncestor(g, g.getMother(), GodMetaData.MOTHER));
        insertMarriedRelations(gods);
    }

    private void insertMarriedRelations(List<God> gods) {
        List<God> marriedTo = gods.stream().filter(g -> g.getMarried() != null && !g.getMarried().isEmpty()).toList();
        marriedTo.forEach(this::addMarried);
    }

    private void addMarried(God g) {
        g.getMarried().forEach(marriedTo -> {
            Optional<Vertex> parentVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, g.getShortName()).tryNext();
            Optional<Vertex> childVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, marriedTo).tryNext();
            if (parentVertex.isPresent() && childVertex.isPresent()) {
                Object fatherId = parentVertex.get().id();
                Object childId = childVertex.get().id();
                boolean edgeAlreadyExists = graphTraversalSource.V(fatherId).out(GodMetaData.MARRIED).hasId(childId).hasNext();
                log.info("Married relation to [{}]-[{}] already exists [{}]", g.getShortName(), marriedTo, edgeAlreadyExists);
                if (!edgeAlreadyExists) {
                    log.info("Creating married relation [{}]-[{}] ", g.getShortName(), marriedTo);
                    graphTraversalSource.tx().begin();
                    graphTraversalSource.addE(GodMetaData.MARRIED).from(__.V(fatherId)).to(__.V(childId)).iterate();
                    graphTraversalSource.tx().commit();
                }
            }
        });
    }

    private void addAncestor(God g, String ancestorShortName, String edgeLabel) {
        Optional<Vertex> parentVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, ancestorShortName).tryNext();
        Optional<Vertex> childVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, g.getShortName()).tryNext();
        if (parentVertex.isPresent() && childVertex.isPresent()) {
            Object fatherId = parentVertex.get().id();
            Object childId = childVertex.get().id();
            boolean edgeAlreadyExists = graphTraversalSource.V(fatherId).out(edgeLabel).hasId(childId).hasNext();
            log.info("Edge for ancestor [{}] with parent [{}] already exists [{}]", g.getShortName(), ancestorShortName, edgeAlreadyExists);
            if (!edgeAlreadyExists) {
                log.info("Creating ancestors for {} with parent {} ", g.getShortName(), ancestorShortName);
                graphTraversalSource.tx().begin();
                graphTraversalSource.addE(edgeLabel).from(__.V(fatherId)).to(__.V(childId)).iterate();
                graphTraversalSource.tx().commit();
            }
        }
    }

    private void createVertices(List<God> gods) {
        for (God g : gods) {
            boolean exists = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, g.getShortName()).hasNext();
            if (exists) {
                log.info("God [{}] already exist", g.getShortName());
            } else {
                log.info("Creating god [{}]", g.getName());
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


