package com.acme.jga.graph.parsing.processors;

import com.acme.jga.graph.PocGraph;
import com.acme.jga.graph.parsing.pojo.God;
import com.acme.jga.graph.parsing.pojo.GodsList;
import com.acme.jga.graph.parsing.pojo.ProcessingResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GodsFeeder {

    public GodsList loadGods(String jsonResourceFile) throws IOException {
        log.info("Loading gods from [{}]", jsonResourceFile);
        try (InputStream is = PocGraph.class.getClassLoader().getResourceAsStream(jsonResourceFile)) {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            StringBuilder sb = new StringBuilder();
            lines.stream().forEach(sb::append);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.readValue(sb.toString(), GodsList.class);
        }
    }

    public List<ProcessingResult> checkGenealogyConsistency(List<God> gods) {
        final List<ProcessingResult> processingResults = new ArrayList<>();
        log.info("Checking genealogy consistency");
        gods.forEach(god -> {
            processingResults.add(checkParentExistence(god.getFather(), god.getName(), "father", gods));
            processingResults.add(checkParentExistence(god.getMother(), god.getName(), "mother", gods));
        });

        return processingResults;
    }

    private ProcessingResult checkParentExistence(String parentName, String godName, String parentType, List<God> gods) {
        boolean parentExists = GodsFunctions.parentExists(parentName, gods);
        log.info("Checked genealogy of [" + godName + "] for parent type [" + parentType + "], success: " + parentExists);
        ProcessingResult result = new ProcessingResult(true, "ok");

        if (!parentExists) {
            result.setSuccess(false);
            result.setMessage("Unable to find " + parentType + " named [" + parentName + "] for god named [" + godName + "]");
        }
        return result;
    }


}
