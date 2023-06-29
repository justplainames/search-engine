package com.example.lucene.controller;

import com.example.lucene.service.DocumentIndexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class DocumentController {
    private DocumentIndexerService documentIndexerService;

    @Autowired
    public DocumentController(DocumentIndexerService documentIndexerService){
        this.documentIndexerService = documentIndexerService;
    }

    /**
     * Simple check that the REST service is running
     * @return message to inform the lucene service is running
     */
    @RequestMapping(value="/", method= RequestMethod.GET)
    @ResponseStatus(value= HttpStatus.OK)
    public String checkService() {
        return "Lucene services are up and running.";
    }

    /**
     * Indexing the content
     * @param content - Refers the filename or file directory
     * @throws IOException
     */
    @PostMapping("/documents")
    @ResponseStatus(value= HttpStatus.OK)
    public void indexDocument(@RequestParam(value = "content") String content) throws IOException {
        documentIndexerService.indexDocument(content);

    }
}
