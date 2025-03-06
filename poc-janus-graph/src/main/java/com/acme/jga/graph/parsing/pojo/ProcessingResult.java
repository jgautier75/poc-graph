package com.acme.jga.graph.parsing.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessingResult {
    private boolean success;
    private String message;
}
