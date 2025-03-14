package com.acme.jga.graph.parsing.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class God {
    private String name;
    private String shortName;
    private String description;
    private String gender;
    private String father;
    private String mother;
    private String category;
    private String married;
}
