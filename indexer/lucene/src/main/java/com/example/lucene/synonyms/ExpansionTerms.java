package com.example.lucene.synonyms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpansionTerms {
    private Map<String, String[]> expansionTerms; // Declare the HashMap
    public ExpansionTerms() {
        expansionTerms = new HashMap<>(); // Initialize the HashMap in the constructor

        // terms for computer
        expansionTerms.put("computer", new String[]{"science",
                "network",
                "machine",
                "digital",
                "analog",
                "industrial",
                "software",
                "calculator",
                "data processor",
                "electronic brain",
                "electronic computer",
                "microcomputer",
                "minicomputer",
                "mainframe",
                "peripheral"});

        // terms for glasgow
        expansionTerms.put("glasgow", new String[]{"scotland",
                "glesga",
                "royal conservatoire of scotland",
                "historic",
                "european capital of culture",
                "glasgow city",
                "romans",
                "welsh",
                "the dear green place",
                "the second city of the empire",
                "the city of the clyde",
                "the merchant city",
                "the city of bridges",
                "the city of science",
                "the city of music"
        });

        // terms for united
        expansionTerms.put("united", new String[]{"combined",
                "affiliated",
                "banded together",
                "concerted",
                "pooled",
                "unified",
                "allied",
                "amalgamated",
                "associated"
        });

        //terms for kingdom
        expansionTerms.put("kingdom", new String[]{"monarchic",
                "king",
                "queen",
                "british",
                "kingdom of god",
                "realm",
                "domain",
                "dominion",
                "territory",
                "jurisdiction",
                "sphere",
                "empire",
                "principality",
                "state",

        });

        //terms for library
        expansionTerms.put("library", new String[]{"material",
                "digital access",
                "printed",
                "dvd",
                "archive",
                "book depository",
                "bookroom",
                "collection",
                "information center",
                "media center",
                "repository",
                "study",
                "stacks"
        });

        expansionTerms.put("fog", new String[]{"mist",
                "haze",
                "smog",
                "vapor",
                "cloud",
                "gloom",
                "murk",
                "pea soup",
                "smudge"
        });
        expansionTerms.put("empires", new String[]{"realms",
                "dominions",
                "states",
                "powers",
                "confederations",
                "federations",
                "unions",
                "kingdoms",
                "principalities"
        });
        expansionTerms.put("doctor", new String[]{"physician",
                "engineering",
                "medical doctor",
                "medic",
                "curer",
                "healer",
                "practitioner",
                "specialist",
                "expert",
                "authority"
        });
        expansionTerms.put("hospital", new String[]{"clinic",
                "infirmary",
                "medical center",
                "nursing home",
                "sick bay",
                "health center",
                "care center",
                "treatment center",
                "rehabilitation center"
        });
        expansionTerms.put("bachelor", new String[]{"unmarried man",
                "eligible bachelor",
                "bachelorette",
                "spinster",
                "celibate",
                "unattached",
                "unpartnered",
                "single",
                "free agent"
        });
        expansionTerms.put("degree", new String[]{"level",
                "grade",
                "rank",
                "standing",
                "station",
                "position",
                "status",
                "measure",
                "extent"
        });
        expansionTerms.put("internet", new String[]{"world wide web",
                "engineering",
                "cyberspace",
                "information superhighway",
                "the net",
                "the web",
                "online",
                "digital world",
                "virtual world",
                "the cloud"
        });
        expansionTerms.put("things", new String[]{"stuff",
                "engineering",
                "articles",
                "objects",
                "items",
                "belongings",
                "paraphernalia",
                "effects",
                "commodities",
                "goods"
        });
        expansionTerms.put("information", new String[]{"data",
                "knowledge",
                "intelligence",
                "news",
                "learning",
                "facts",
                "wisdom",
                "clue",
                "hint"
        });
        expansionTerms.put("info", new String[]{"data",
                "details",
                "facts",
                "intelligence",
                "knowledge",
                "news",
                "tidbits",
                "pointers",
                "clues"
        });
        expansionTerms.put("retrieval", new String[]{"extraction",
                "recovery",
                "recall",
                "summoning",
                "abstraction",
                "gathering",
                "collection",
                "selection",
                "ascertainment"
        });
        expansionTerms.put("retrieve", new String[]{"fetch",
                "regain",
                "recall",
                "recover",
                "recollect",
                "remember",
                "summon",
                "track down",
                "unearth"
        });
        expansionTerms.put("universe", new String[]{"cosmos",
                "world",
                "creation",
                "macrocosm",
                "nature",
                "realm",
                "sphere",
                "system",
                "whole"
        });
        expansionTerms.put("university", new String[]{"academy",
                "college",
                "higher education institution",
                "institute",
                "learning institution",
                "school",
                "seminary",
                "varsity",
                "alma mater"
        });
        // You can also add key-value pairs to the HashMap here if needed:
        // stringMap.put("Key1", "Value1");
        // stringMap.put("Key2", "Value2");
        // ...
    }

    public Map<String, String[]> getExpansionTerms() {
        return expansionTerms;
    }
}
