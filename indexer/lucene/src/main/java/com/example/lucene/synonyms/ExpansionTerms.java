package com.example.lucene.synonyms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpansionTerms {
    private Map<String, String[]> expansionTerms; // Declare the HashMap
    public ExpansionTerms() {
        expansionTerms = new HashMap<>(); // Initialize the HashMap in the constructor

        expansionTerms.put("computer", new String[]{"science", "engineering"});
        expansionTerms.put("glasgow", new String[]{"science", "engineering"});
        expansionTerms.put("united", new String[]{"science", "engineering"});
        // You can also add key-value pairs to the HashMap here if needed:
        // stringMap.put("Key1", "Value1");
        // stringMap.put("Key2", "Value2");
        // ...
    }

    public Map<String, String[]> getExpansionTerms() {
        return expansionTerms;
    }
}
