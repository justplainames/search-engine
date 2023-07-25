package com.example.lucene.service;

import com.example.lucene.synonyms.ExpansionTerms;
import com.google.gson.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;


@Component
public class DocumentIndexerService {
    private Directory indexDirectory;
    private IndexWriter indexWriter;
    private static EnglishAnalyzer analyzer = new EnglishAnalyzer();

    private List<MultipartFile> queue = new ArrayList();
    private final static Logger logger = Logger.getLogger("DocumentIndexer");

    // The index directory needs to be set to allow indexing and retrieval of documents
    private static String indexDir = "/tmp/index";

    // key value list to store the return results
    private static List<Map<String, String>> keyValueList = new ArrayList<>();

    private static List<Map<String, String>> urlValueList = new ArrayList<>();

    ExpansionTerms expansionTerms = new ExpansionTerms();

    public DocumentIndexerService() throws IOException {
        FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(dir, writerConfig);

    }
    public List<Map<String, String>> urlQuerySelector(List<String> urls) throws IOException, ParseException {
//        public List<Map<String, String>> urlQuerySelector() throws IOException, ParseException {
        closeIndexWriter();
//        logger.info(String.valueOf(urls));
        String[] fields = {"title","url", "contents", "abstract", "description", "keywords"};; // Fields to search
        TopScoreDocCollector collector = TopScoreDocCollector.create(10,20);
      
        try{
            Analyzer analyzer = new EnglishAnalyzer();
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            // Create individual queries for each URL
            BooleanQuery.Builder urlQueryBuilder = new BooleanQuery.Builder();
            for (String url : urls) {
                Query urlQuery = queryParser.parse(QueryParser.escape(url));
                urlQueryBuilder.add(urlQuery, BooleanClause.Occur.SHOULD);
            }
            // Combine individual queries with OR operator
            Query finalQuery = urlQueryBuilder.build();

            // Retrieve the matching documents
            TopDocs topDocs = searcher.search(finalQuery, 10); // Adjust the number of desired results (e.g., top 10)

            // Collect the document IDs of the matching documents
            Map<Integer, Document> retrievedDocuments = new HashMap<>();
            for (ScoreDoc hit : topDocs.scoreDocs) {
                int docId = hit.doc;
                Document document = searcher.doc(docId);
                retrievedDocuments.put(docId, document);
            }

            // Get contents of the retrieved documents
            Map<String, Integer> termFrequencies = new HashMap<>();
            for (Document doc : retrievedDocuments.values()) {
                String content = doc.get("contents");
                // Analyze the content to get the terms
                // You can use the same analyzer you used during indexing
                // Example:
                TokenStream tokenStream = analyzer.tokenStream("contents", content);
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
                    termFrequencies.put(term, termFrequencies.getOrDefault(term, 0) + 1);
                }
                tokenStream.close();
            }

            // Calculate TF-IDF scores for each term
            int totalDocuments = reader.maxDoc();
            Map<String, Float> tfIdfScores = new HashMap<>();
            for (Map.Entry<String, Integer> entry : termFrequencies.entrySet()) {
                String term = entry.getKey();
                int termFrequency = entry.getValue();

                // Calculate Document Frequency (DF)
                org.apache.lucene.index.Term termInstance = new org.apache.lucene.index.Term("content", term);
                int documentFrequency = reader.docFreq(termInstance);

                // Calculate Inverse Document Frequency (IDF)
                double idf = Math.log((double) totalDocuments / (double) (documentFrequency + 1));

                // Calculate TF-IDF score
                float tfIdfScore = (float) (termFrequency * idf);
                tfIdfScores.put(term, tfIdfScore);
            }

            List<Map.Entry<String, Float>> sortedTerms = new ArrayList<>(tfIdfScores.entrySet());
            sortedTerms.sort((o1, o2) -> Float.compare(o2.getValue(), o1.getValue()));

            // Take top 10 most valuable terms
            int topTermsCount = Math.min(15, sortedTerms.size());
            List<String> topTerms = new ArrayList<>();
            for (int i = 0; i < topTermsCount; i++) {
                topTerms.add(sortedTerms.get(i).getKey());
            }

            // New Search with the top 10 terms
            for (String term : topTerms) {
                String queryEscape = QueryParserUtil.escape(term);
                Query q = new MultiFieldQueryParser(fields, analyzer, Map.of(
                        "title", 0.3f,
                        "contents", 1f,
                        "description", 1f))
                        .parse(queryEscape);
                searcher.search(q, collector);
            }

            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            logger.info("Found" + hits.length + "hits.");
            for(int i = 0 ; i < hits.length ; ++i ){
                Map<String, String> keyValueMap = new HashMap<>();
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                keyValueMap.put("index", String.valueOf(i+1));
                keyValueMap.put("url", d.get("url"));
//            keyValueMap.put("content", d.get("contents"));
                keyValueMap.put("title", d.get("title"));
                keyValueMap.put("score", String.valueOf(hits[i].score));
                urlValueList.add(keyValueMap);
            }
            // Close the reader
            reader.close();
        }catch(IOException | ParseException e) {
            e.printStackTrace();
        }

        return urlValueList;
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

        // Query q = new QueryParser("contents", analyzer).parse(queryEscape);


        // Create a MultiFieldQueryParser with boosts
        String[] fields = {"title", "contents", "abstract", "description", "keywords"};
        Query q = new MultiFieldQueryParser(fields, analyzer, Map.of(
            "title", 0.3f,
            "contents", 1f,
            "description", 1f))
            .parse(queryEscape);

        Map<String, String[]> expansionMap = expansionTerms.getExpansionTerms();
        logger.info("expansionMap:" + expansionMap);

        String[] expansionTerms = expansionMap.get(query);
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
            keyValueMap.put("description", d.get("description"));
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
                String abstract_ = jsonElement.getAsJsonObject().get("meta_abstract").getAsString();
                String keywords = jsonElement.getAsJsonObject().get("meta_keywords").getAsString();
                String description = jsonElement.getAsJsonObject().get("meta_description").getAsString();

//                logger.info("content:" +  content);
//                logger.info("fileUrl:" +  fileUrl);

                doc.add(new TextField("contents", content, Field.Store.YES));
                doc.add(new TextField("title", title, Field.Store.YES));
                doc.add(new TextField("abstract", abstract_, Field.Store.YES));
                doc.add(new TextField("keywords", keywords, Field.Store.YES));
                doc.add(new TextField("description", description, Field.Store.YES));
//                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                doc.add(new StringField("url", fileUrl, Field.Store.YES));


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

    public static boolean checkKeyExists(Map<String, String[]> expansionMap, String key) {
        boolean containsKey = false;
        for (Map.Entry<String, String[]> expansionEntry : expansionMap.entrySet()) {
            if (expansionEntry.getKey().equals(key)) {
                containsKey = true;
                break;
            }
        }
        return containsKey;
    }
}
