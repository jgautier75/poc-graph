package com.acme.jga.graph.config;

import org.apache.commons.configuration2.MapConfiguration;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.util.system.ConfigurationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Configuration
public class JanusConfig {
    @Autowired
    private JanusProperties janusProperties;

    @Bean
    public JanusGraph janusGraph() {
        Map<String, Object> janusProps = janusProperties.getConfig();
        Map<String, Object> props = new HashMap<>();
        janusProps.forEach((k, v) -> props.put(k.replaceAll("_", "."), v));
        MapConfiguration config = ConfigurationUtil.loadMapConfiguration(props);
        return JanusGraphFactory.open(config);
    }

    @Bean
    public GraphTraversalSource graphTraversalSource() throws Exception {
        return traversal().withRemote("remote-graph.properties");
    }
}
