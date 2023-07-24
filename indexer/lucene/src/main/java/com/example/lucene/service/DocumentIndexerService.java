package com.example.lucene.service;

import com.example.lucene.synonyms.ExpansionTerms;
import com.google.gson.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;


@Component
public class DocumentIndexerService {
    private Directory indexDirectory;
    private IndexWriter indexWriter;
    private static StandardAnalyzer analyzer = new StandardAnalyzer();

    private List<MultipartFile> queue = new ArrayList();
    private final static Logger logger = Logger.getLogger("DocumentIndexer");

    // The index directory needs to be set to allow indexing and retrieval of documents
    private static String indexDir = "/tmp/index";

    // key value list to store the return results
    private static List<Map<String, String>> keyValueList = new ArrayList<>();

    ExpansionTerms expansionTerms = new ExpansionTerms();

    public DocumentIndexerService() throws IOException {
        FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(dir, writerConfig);

    }

    public List<Map<String, String>> queryDocument(String query) throws IOException, ParseException {
        // index writer needs to be closed after the indexing
        closeIndexWriter();

        //===================================================
        // Search for files
        //===================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir).toPath()));
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(10,20);

        // check what is inside query
        // map the query to the expansion term
        // check if term inside expansion term
        // return the synonym


        // queryEscape prevents user from getting a lexical error if search with weird symbols
        String queryEscape = QueryParserUtil.escape(query);

        Query q = new QueryParser("contents", analyzer).parse(queryEscape);

        Map<String, String[]> map = expansionTerms.getExpansionTerms();

        // return expansionTerms based on the query
        String[] expansionTerms = map.get(query);
        // perform query expansion based on expansion term
        Query expandedQuery = expandQuery(q, analyzer, expansionTerms);

        searcher.search(expandedQuery, collector);

        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        logger.info("Found" + hits.length + "hits.");
        for(int i = 0 ; i < hits.length ; ++i ){
            Map<String, String> keyValueMap = new HashMap<>();
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            keyValueMap.put("index", String.valueOf(i+1));
            keyValueMap.put("url", d.get("url"));
            keyValueMap.put("content", d.get("content"));
            keyValueMap.put("title", d.get("title"));
            keyValueMap.put("score", String.valueOf(hits[i].score));
            keyValueList.add(keyValueMap);
        }

        return keyValueList;
    }

    public void indexDocument() throws IOException {

        int originalNumDocs = indexWriter.getDocStats().numDocs;

        for (MultipartFile f : queue) {
            try {
                Reader reader = new InputStreamReader(f.getInputStream());
                Document doc = new Document();

                //===================================================
                // add contents of file
                //===================================================

                // accessing the 'text' key in fileReader
                JsonElement jsonElement = JsonParser.parseReader(reader);
                String content = jsonElement.getAsJsonObject().get("text").getAsString();
                String fileUrl = jsonElement.getAsJsonObject().get("url").getAsString();
                String title = jsonElement.getAsJsonObject().get("title").getAsString();

//                logger.info("content:" +  content);
//                logger.info("fileUrl:" +  fileUrl);

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
        }

        int newNumDocs = indexWriter.getDocStats().numDocs;

        logger.info("index success: " + (newNumDocs - originalNumDocs));

        queue.clear();
    }
    public String addFiles(MultipartFile file) throws IOException {

        String fileName = file.getOriginalFilename().toLowerCase();
        logger.info("what is filename: " + fileName);
//        }

        if (!file.isEmpty()) {
            System.out.println(file + " does not exist.");
        }
        //===================================================
        // Only index files in this certain format
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
    private static Query expandQuery(Query query, Analyzer analyzer, String[] expansionTerms) throws IOException, ParseException {
        Query expandedQuery = query;

        // process what to take


        // Create a BooleanQuery to combine the original query and expanded terms
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        queryBuilder.add(query, BooleanClause.Occur.MUST);

        // Expand the query terms
        for (String term : expansionTerms) {
            TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(term));
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                String expandedTerm = charTermAttribute.toString();

                // Create a TermQuery for each expanded term
                Query termQuery = new TermQuery(new Term("contents", expandedTerm));
                queryBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
            }

            tokenStream.end();
            tokenStream.close();
        }

        // Build the final expanded query
        expandedQuery = queryBuilder.build();

        return expandedQuery;
    }

    public Directory getIndexDirectory() {
        return indexDirectory;
    }
}
