package com.example.lucene.service;

import com.google.gson.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@Component
public class DocumentIndexerService {
    private Directory indexDirectory;
    private IndexWriter indexWriter;
    private static StandardAnalyzer analyzer = new StandardAnalyzer();

    private List<MultipartFile> queue = new ArrayList();
    private final static Logger logger = Logger.getLogger("DocumentIndexer");

    private static String indexDir = "/tmp/index";


    public DocumentIndexerService() throws IOException {
        FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(dir, writerConfig);

    }

    public List<Map<String, String>> queryDocument(String query) throws IOException, ParseException {
        closeIndexWriter();

        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir).toPath()));
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(5,20);
        Map<String, String> keyValueMap = new HashMap<>();
        List< Map<String, String>> keyValueList = new ArrayList<>();
        Query q = new QueryParser("contents", analyzer).parse(query);

        searcher.search(q, collector);

        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        logger.info("Found" + hits.length + "hits.");

        for(int i = 0 ; i < hits.length ; ++i ){
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            keyValueMap.put("index", String.valueOf(i+1));
            keyValueMap.put("url", d.get("url"));
            keyValueMap.put("title", d.get("title"));
            keyValueMap.put("score", String.valueOf(hits[i].score));
            keyValueList.add(keyValueMap);
        }

        return keyValueList;
    }

    public void indexDocument() throws IOException {
//        addFiles(new File(fileName));

        int originalNumDocs = indexWriter.getDocStats().numDocs;

        for (MultipartFile f : queue) {
//            FileReader fr = null;
//            Reader reader = null;
            try {
                Reader reader = new InputStreamReader(f.getInputStream());
                Document doc = new Document();

                //===================================================
                // add contents of file
                //===================================================
//                fr = new FileReader(f);

                // accessing the 'text' key in fileReader
                JsonElement jsonElement = JsonParser.parseReader(reader);
//                System.out.println("what is json element: " + jsonElement);
                String content = jsonElement.getAsJsonObject().get("text").getAsString();
                String fileUrl = jsonElement.getAsJsonObject().get("url").getAsString();
                String title = jsonElement.getAsJsonObject().get("title").getAsString();

                logger.info("content:" +  content);
                logger.info("fileUrl:" +  fileUrl);

                doc.add(new TextField("contents", content, Field.Store.YES));
//                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getOriginalFilename(), Field.Store.YES));
                doc.add(new StringField("url", fileUrl, Field.Store.YES));
                doc.add(new StringField("title", title, Field.Store.YES));

                indexWriter.addDocument(doc);
                logger.info("Added doc: " + f);
            } catch (Exception e) {
                logger.info("could not add doc: " + f);
            }
//            finally {
//                reader.close();
//            }
        }

        int newNumDocs = indexWriter.getDocStats().numDocs;
//        System.out.println("");
//        System.out.println("************************");
//        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
//        System.out.println("************************");
//        System.out.println("index success");
        logger.info("index success: " + (newNumDocs - originalNumDocs));

        queue.clear();
    }
    public String addFiles(MultipartFile file) throws IOException {

        String fileName = file.getOriginalFilename().toLowerCase();
//        byte[] fileContent = file.getBytes();
//        String content = new String(file.getBytes());
        logger.info("what is filename: " + fileName);
//        logger.info("what is content: " + content);
//        logger.info("what is content: " + content);
//        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
//            String title = br.readLine();
//            logger.info(title);
//        }

        if (!file.isEmpty()) {
            System.out.println(file + " does not exist.");
        }
        //===================================================
        // Only index text files
        //===================================================
        if (fileName.endsWith(".json") || fileName.endsWith(".html") ||
                fileName.endsWith(".xml") || fileName.endsWith(".txt")) {
            queue.add(file);
            logger.info("added to queue");
        } else {
            System.out.println("Skipped " + fileName);
        }

        return fileName;

    }

    public void closeIndexWriter() throws IOException {
        indexWriter.close();
    }

    public Directory getIndexDirectory() {
        return indexDirectory;
    }
}
