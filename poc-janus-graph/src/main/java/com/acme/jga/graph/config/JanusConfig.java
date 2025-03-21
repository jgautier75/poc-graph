package com.acme.jga.graph.config;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.io.binary.TypeSerializerRegistry;
import org.apache.tinkerpop.gremlin.util.ser.GraphBinaryMessageSerializerV1;
import org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Configuration
public class JanusConfig {
    @Autowired
    private JanusProperties janusProperties;

    @Bean
    public GraphTraversalSource graphTraversalSource() throws Exception {
        TypeSerializerRegistry typeSerializerRegistry = TypeSerializerRegistry.build()
                .addRegistry(JanusGraphIoRegistry.instance())
                .create();
        Cluster cluster = Cluster.build()
                .addContactPoint((String) janusProperties.getConfig().get("contact_point"))
                .port((Integer) janusProperties.getConfig().get("contact_port"))
                .serializer(new GraphBinaryMessageSerializerV1(typeSerializerRegistry))
                .maxConnectionPoolSize(5)
                .maxInProcessPerConnection(1)
                .maxSimultaneousUsagePerConnection(10)
                .create();
        return traversal().withRemote(DriverRemoteConnection.using(cluster));
    }
}
