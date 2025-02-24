package com.acme.jga.graph.es;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
@Slf4j
public class ElasticsearchAuthenticator implements org.janusgraph.diskstorage.es.rest.util.RestClientAuthenticator {
    private String[] args;
    private String instanceId;
    private String userName;
    private String secrets;


    public ElasticsearchAuthenticator(String[] args) {
        this.args = args;
        if (this.args != null) {
            Arrays.stream(args).forEach(a -> {
                if (a.startsWith("userName")) {
                    this.userName = a.substring(a.indexOf('=') + 1);
                } else if (a.startsWith("secrets")) {
                    this.secrets = a.substring(a.indexOf('=') + 1);
                } else {
                    this.instanceId = a;
                }
            });
        }
    }

    @Override
    public void init() throws IOException {
        log.info("test");
    }

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(this.userName, this.secrets));
        httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        return httpAsyncClientBuilder;
    }

    @Override
    public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
        return builder;
    }
}
