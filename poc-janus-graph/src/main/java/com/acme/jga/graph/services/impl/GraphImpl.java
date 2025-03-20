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
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.schema.SchemaManager;
import org.janusgraph.graphdb.tinkerpop.io.graphson.JanusGraphSONModuleV2d0;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.acme.jga.graph.GodsConverter.godToPropertyMap;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal.Symbols.from;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphImpl implements GraphApi {
    private final GodsFeeder godsFeeder;
    private final GraphTraversalSource graphTraversalSource;
    private final JanusGraph janusGraph;
    private final Client gremlinClient;

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

    private void checkCreateEdgeLabels() {

        // Create a JanusGraph instance (assuming your JanusGraph server is remote)
        /*JanusGraph graph = (JanusGraph) graphTraversalSource.getGraph();*/

        // Access the schema management
        SchemaManager schemaManager = janusGraph.openManagement();

        // Create an edge label "father"
        if (!schemaManager.containsEdgeLabel(GodMetaData.FATHER)) {
            schemaManager.makeEdgeLabel(GodMetaData.FATHER).make();
            System.out.println("Edge label '" + GodMetaData.FATHER + "' created.");
        }
        // Create an edge label "mother"
        if (!schemaManager.containsEdgeLabel(GodMetaData.MOTHER)) {
            schemaManager.makeEdgeLabel(GodMetaData.MOTHER).make();
            System.out.println("Edge label '" + GodMetaData.MOTHER + "' created.");
        }
    }

    private void insertAncestors(List<God> gods) {
        checkCreateEdgeLabels();
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
                String edgeName = String.format("%s_to_%s", GodMetaData.MARRIED, marriedTo);
                boolean edgeAlreadyExists = graphTraversalSource.V(fatherId).out(edgeName).hasId(childId).hasNext();
                log.info("Married relation to [{}]-[{}] already exists [{}]", g.getShortName(), marriedTo, edgeAlreadyExists);
                if (!edgeAlreadyExists) {
                    log.info("Creating married relation [{}]-[{}] ", g.getShortName(), marriedTo);
                    graphTraversalSource.tx().begin();
                    graphTraversalSource.addE(edgeName).from(__.V(fatherId)).to(__.V(childId)).iterate();
                    graphTraversalSource.tx().commit();
                }
            }
        });
    }

    private void addAncestor(God g, String ancestorShortName, String edgeLabel) {
        Optional<Vertex> parentVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, ancestorShortName).tryNext();
        Optional<Vertex> childVertex = graphTraversalSource.V().has(GodMetaData.SHORT_NAME, g.getShortName()).tryNext();
        if (parentVertex.isPresent() && childVertex.isPresent()) {
            String edgeName = String.format("%s_of_%s", edgeLabel, g.getShortName());
            Object fatherId = parentVertex.get().id();
            Object childId = childVertex.get().id();

            //g.V().has('name','marko').as('a').bothE().bothV().where(neq('a')).path()

            /*Optional<Vertex> optVertex = graphTraversalSource.V(fatherId).as("a").outE().bothV().where(P.neq("a")).tryNext();
            boolean edgeAlreadyExists = optVertex.isPresent();*/
            //boolean edgeAlreadyExists = graphTraversalSource.V(fatherId).out(edgeLabel).hasId(childId).hasNext();
            /*log.info("Edge for ancestor [{}] with parent [{}] already exists [{}]", g.getShortName(), ancestorShortName, edgeAlreadyExists);
            if (!edgeAlreadyExists) {*/
            log.info("Creating ancestors for [{}] with parent [{}] ", g.getShortName(), ancestorShortName);
            graphTraversalSource.tx().begin();
            /*graphTraversalSource.V(fatherId).addE(edgeLabel).to(__.V(childId)).iterate();*/
            //graphTraversalSource.V(fatherId).out(edgeLabel).to(__.V(childId)).iterate();
            //g.addE('knows').from(v1).to(v2).property('since', 2021).next()

            Edge edge = graphTraversalSource.addE(edgeLabel).from(__.V(fatherId)).to(__.V(childId)).next();
            graphTraversalSource.tx().commit();
            /*}*/
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
                    graphTraversalSource.addV(SchemaImpl.VERTEX_GOD).property(properties).next();
                    graphTraversalSource.tx().commit();
                } catch (Exception e) {
                    graphTraversalSource.tx().rollback();
                    throw e;
                }
            }
        }
    }

    private boolean hasFather(God g, List<God> gods) {
        return gods.stream().anyMatch(gl -> gl.getFather().equals(g.getShortName()));
    }

}


