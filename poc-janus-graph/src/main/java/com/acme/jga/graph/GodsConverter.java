package com.acme.jga.graph;

import com.acme.jga.graph.parsing.pojo.God;
import com.acme.jga.graph.parsing.pojo.GodMetaData;
import com.acme.jga.graph.rest.dtos.VertexReadDto;
import lombok.NoArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@NoArgsConstructor
public class GodsConverter {

    public static Map<Object, Object> godToPropertyMap(God g) {
        Map<Object, Object> properties = new HashMap<>();
        properties.put(GodMetaData.NAME, g.getName());
        properties.put(GodMetaData.SHORT_NAME, g.getShortName());
        properties.put(GodMetaData.GENDER, g.getGender());
        if (g.getDescription() != null) {
            properties.put(GodMetaData.DESCRIPTION, g.getDescription());
        }
        if (g.getCategory() != null) {
            properties.put(GodMetaData.CATEGORY, g.getCategory());
        }
        return properties;
    }

    public static VertexReadDto vertexToDto(Vertex v) {
        VertexReadDto vertexReadDto = new VertexReadDto();
        Iterator<VertexProperty<Object>> vertextIterator = v.properties();
        while (vertextIterator.hasNext()) {
            VertexProperty<Object> vprop = vertextIterator.next();
            switch (vprop.label()) {
                case "uuid":
                    vertexReadDto.setUuid(vprop.value().toString());
                    break;
                case "shortName":
                    vertexReadDto.setShortName(vprop.value().toString());
                    break;
                case "gender":
                    vertexReadDto.setGender(vprop.value().toString());
                    break;
                case "description":
                    vertexReadDto.setDescription(vprop.value().toString());
                    break;
                case "category":
                    vertexReadDto.setCategory(vprop.value().toString());
                    break;
                case "father":
                    vertexReadDto.setFather(vprop.value().toString());
                    break;
                case "mother":
                    vertexReadDto.setMother(vprop.value().toString());
                    break;
            }
        }
        return vertexReadDto;
    }
}
