package com.acme.jga.graph.config;

import org.apache.commons.configuration2.MapConfiguration;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.util.system.ConfigurationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JanusConfig {
    @Autowired
    private JanusProperties janusProperties;

    @Bean
    public JanusGraph janusGraphFactory() {
        Map<String, Object> janusProps = janusProperties.getConfig();
        Map<String, Object> props = new HashMap<>();
        janusProps.forEach((k, v) -> props.put(k.replaceAll("_", "."), v));
        MapConfiguration config = ConfigurationUtil.loadMapConfiguration(props);
        return JanusGraphFactory.open(config);
    }
}
