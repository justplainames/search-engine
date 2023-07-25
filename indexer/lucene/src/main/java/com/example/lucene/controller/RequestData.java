package com.example.lucene.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;


public class RequestData {
    private List<InnerDocument> searchResult;

    // Constructor
    public RequestData(List<InnerDocument> searchResult) {
        this.searchResult = searchResult;
    }

    // Default constructor
    public RequestData() {
        // You can leave it empty or initialize any fields here if needed
    }

    // Static factory method with @JsonCreator
    @JsonCreator
    public static RequestData createRequestData(
            @JsonProperty("searchResult") List<InnerDocument> searchResult) {
        return new RequestData(searchResult);
    }

    // Getter for searchResult
    public List<InnerDocument> getSearchResult() {
        return searchResult;
    }

    // Setter for searchResult
    public void setSearchResult(List<InnerDocument> searchResult) {
        this.searchResult = searchResult;
    }
}
