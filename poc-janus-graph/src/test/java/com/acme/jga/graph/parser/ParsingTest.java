package com.acme.jga.graph.parser;

import com.acme.jga.graph.parsing.pojo.GodsList;
import com.acme.jga.graph.parsing.pojo.ProcessingResult;
import com.acme.jga.graph.parsing.processors.GodsFeeder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ParsingTest {
    private final GodsFeeder godsFeeder = new GodsFeeder();

    @Test
    public void parse() throws IOException {
        GodsList godsList = godsFeeder.loadGods("gods.json");
        List<ProcessingResult> processingResults = godsFeeder.checkGenealogyConsistency(godsList.getGods());
        List<ProcessingResult> errors = processingResults.stream().filter( p -> !p.isSuccess()).toList();
        log.info("Genealogy errors (if any)");
        errors.forEach(e -> System.out.println(e.getMessage()));
    }

}
