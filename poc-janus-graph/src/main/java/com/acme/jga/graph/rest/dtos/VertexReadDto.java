package com.acme.jga.graph.rest.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VertexReadDto {
    private String uuid;
    private String shortName;
    private String gender;
    private String description;
    private String category;
    private String father;
    private String mother;
}
