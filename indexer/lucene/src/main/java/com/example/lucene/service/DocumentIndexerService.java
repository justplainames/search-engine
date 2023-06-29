package com.example.lucene.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class DocumentIndexerService {
    private Directory indexDirectory;
    private IndexWriter indexWriter;
    private static StandardAnalyzer analyzer = new StandardAnalyzer();

    private List<File> queue = new ArrayList();
    private final static Logger logger = Logger.getLogger("LuceneSearcher");

    public DocumentIndexerService() throws IOException {
        String indexDir = "/tmp/index";
        FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());

//        indexDirectory = new ByteBuffersDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(dir, writerConfig);

    }

    public void indexDocument(String fileName) throws IOException {
        addFiles(new File(fileName));

        int originalNumDocs = indexWriter.getDocStats().numDocs;
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                //===================================================
                // add contents of file
                //===================================================
                fr = new FileReader(f);

                // accessing the 'text' key in fileReader
                JsonElement jsonElement = JsonParser.parseReader(fr);
//                System.out.println("what is json element: " + jsonElement);
                String content = jsonElement.getAsJsonObject().get("text").getAsString();
                String fileUrl = jsonElement.getAsJsonObject().get("url").getAsString();

                doc.add(new TextField("contents", content, Field.Store.YES));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                doc.add(new StringField("url", fileUrl, Field.Store.YES));

                indexWriter.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = indexWriter.getDocStats().numDocs;
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");
        System.out.println("index success");

        queue.clear();
    }
    private void addFiles(File file) throws IOException {

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String title = br.readLine();
            logger.info(title);
        }

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }

        // Check if input is a single file or directory
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            //===================================================
            // Only index text files
            //===================================================
            if (filename.endsWith(".json") || filename.endsWith(".html") ||
                    filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    public void closeIndexWriter() throws IOException {
        indexWriter.close();
    }

    public Directory getIndexDirectory() {
        return indexDirectory;
    }
}
