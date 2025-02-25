package com.acme.jga.graph.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateVertexDto {
    String name;
    List<VertexProperty> properties;
}
