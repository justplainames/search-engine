package com.example.lucene.controller;

import com.example.lucene.service.DocumentIndexerService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class DocumentController {
    private final static Logger logger = Logger.getLogger("DocumentController");

//    private static String UPLOAD_FOLDER = "/tmp/index";
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
     *
     * @param file - Refers the filename or file directory
     * @return
     * @throws IOException
     */
    @PostMapping("/documents")
    @ResponseStatus(value= HttpStatus.OK)
    public String indexDocument(@RequestParam(value = "file") MultipartFile file,
                                RedirectAttributes redirectAttributes
                                ) throws IOException {

        if (!file.isEmpty()){
            String results = documentIndexerService.addFiles(file);
            documentIndexerService.indexDocument();
            return results;
        }
        else{
            return "no file uploaded";
        }

    }
    @GetMapping("/query")
    public List<Map<String, String>> queryDocument(@RequestParam(value = "query") String query) throws IOException, ParseException {
        List<Map<String, String>> response = documentIndexerService.queryDocument(query);
//        System.out.println("response:" + response);
        return response;
    }

    @PostMapping("/urlQuery")
        public List<Map<String, String>> urlQuerySelector(@RequestBody RequestData requestData) throws IOException, ParseException {

//        List<InnerDocument> searchResultItems = requestData.getSearchResult();
        List<String> urls = requestData.getSearchResult();
        logger.info("urls: " + requestData.getSearchResult());

//        for (InnerDocument item : searchResultItems) {
//            String url = item.getUrl();
//            urls.add(url);
//        }

        List<Map<String, String>> response = documentIndexerService.urlQuerySelector(urls);
        return response;
    }

}
