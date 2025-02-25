package com.acme.jga.graph.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VertexProperty {
    private String key;
    private Object value;
}
