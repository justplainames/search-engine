package com.example.lucene.controller;

import com.example.lucene.service.DocumentIndexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
//        documentIndexerService.(file);
//        String fileNameOriginal = file.getOriginalFilename();
//        String fileNameOrigin = file.getOriginalFilename();
//        String dir = "/tmp/";
//
//        File destDir = new File(dir + fileNameOrigin);
//
//        file.transferTo(destDir);

        if (!file.isEmpty()){
//            byte[] bytes = file.getBytes();
//            Path path = Paths.get(UPLOAD_FOLDER + file.getOriginalFilename());
//            Files.write(path, bytes);
//            logger.info("what is path: " + path);
//            redirectAttributes.addFlashAttribute("message",
//                    "success upload " + file.getOriginalFilename()
//                    );


            String results = documentIndexerService.addFiles(file);
            documentIndexerService.indexDocument();
            return results;
        }
        else{
            return "no file uploaded";
        }
//        if (!file.isEmpty()){
//            String fileName = documentIndexerService.addFiles(file);
//            if (!fileName.isEmpty()){
//                documentIndexerService.indexDocument();
//            }
//            else{
//                return "filename invalid";
//            }
//        }
//        else{
//            return "No file uploaded";
//        }

    }

}
