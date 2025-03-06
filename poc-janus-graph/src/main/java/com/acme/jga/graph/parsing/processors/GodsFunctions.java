package com.acme.jga.graph.parsing.processors;

import com.acme.jga.graph.parsing.pojo.God;

import java.util.List;

public class GodsFunctions {
    public static boolean parentExists(String parentCode, List<God> gods) {
        if (parentCode == null || parentCode.isEmpty()) {
            return true;
        } else {
            return gods.stream().anyMatch(g -> g.getShortName() != null && !g.getShortName().isEmpty() && g.getShortName().equals(parentCode));
        }
    }

}
