
import com.google.gson.JsonElement;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Paths;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class TextFileIndexer {
    private static EnglishAnalyzer analyzer = new EnglishAnalyzer();

    private IndexWriter writer;
    private List<File> queue = new ArrayList();


    public static void main(String[] args) throws IOException, org.apache.lucene.queryparser.classic.ParseException {  
        try {
            // Call the SpecificFilesRetrieval method and handle the ParseException
            SpecificFilesRetrieval();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
        String[] expansionTerms = {
            "hi",
            "thing",
            "he",
            "okonkwo",
            "from",
            "acheb",
            "on",
            "style",
            "when",
            "it",
            "ha",
            "which"
        };

        System.out.println("Enter the path where the index will be created: (e.g. /tmp/index or c:\\temp\\index)");

        String indexLocation = null;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String s = br.readLine();

        TextFileIndexer indexer = null;
        try {
            indexLocation = s;
            indexer = new TextFileIndexer(s);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the full path to add into the index (q=quit): (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
                System.out.println("[Acceptable file types: .json, .html, .html, .txt]");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                //try to add file into the index
                indexer.indexFileOrDirectory(s);
            } catch (Exception e) {
                System.out.println("Error indexing " + s + " : " + e.getMessage());
            }
        }

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created
        //===================================================
        indexer.closeIndex();

        //=========================================================
        // Now search
        //=========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation).toPath()));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1.5f, 1f));



        TopScoreDocCollector collector = TopScoreDocCollector.create(15, 20);

        s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                //=========================================================
                // Parse Default
                //=========================================================
                //Create parser
                // Query query = new QueryParser("contents", analyzer).parse(s);

                // // Perform query expansion
                // Query expandedQuery = expandQuery(q, analyzer, expansionTerms);

                //=========================================================
                // Parse Multifield
                //=========================================================
                // Create a MultiFieldQueryParser with boosts
                String[] fields = {"title", "contents", "abstract", "description", "keywords"};
                // MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
                long startTime = System.currentTimeMillis();
                Query query = new MultiFieldQueryParser(fields, analyzer, Map.of(
                    "title", 0.3f,
                    "contents", 1f,
                    "abstract", 1f,
                    "description", 1f, 
                    "keywords", 1f))
                    .parse(s);
                

                // Perform query expansion
                Query expandedQuery = expandQuery(query, analyzer, expansionTerms);
            
              
                searcher.search(expandedQuery, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;


        

                // QueryParser parser = new QueryParser("contents", analyzer);
                // Query query = parser.parse(s);

                // 4. display results
                List<Map<String, String>> dataList = new ArrayList<>();
                System.out.println("Found " + hits.length + " hits.");
                for(int i=0;i<hits.length;++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    // Explanation explanation = searcher.explain(query, docId);

                    System.out.println((i + 1) + ". Title: " + d.get("title") + " URL: " +d.get("url") +" score= " + hits[i].score);
                    // Map<String, String> dataMap = new HashMap<>();
                    // dataMap.put("url", d.get("url"));
                    // dataList.add(dataMap);
                    // Print the explanation
                    // System.out.println("Explanation: " + explanation.toString());
                    // System.out.println();
                    }
                
                // Map<String, List<Map<String, String>>> finalMap = new HashMap<>();
                // finalMap.put(s, dataList);
                // String filePath = "C:\\Users\\JustP\\OneDrive\\Documents\\SiT\\Y3T3\\CSC3010 Information Retrieval\\Project\\json_files";
                

                //  // Record the end time
                // long endTime = System.currentTimeMillis();

                // // Calculate elapsed time
                // long elapsedTimeMs = endTime - startTime;

                // // Print the elapsed time in milliseconds
                // System.out.println("Elapsed Time (ms): " + elapsedTimeMs);

                // // Step 4: Convert the finalMap to a JSON string
                // String jsonString = mapToJsonString(finalMap);

                // // Step 5: Save the JSON string to a file
                // saveJsonToFile(jsonString, "json\\"+s +".json");

                
                

            } catch (Exception e) {
                System.out.println("Error searching " + s + " : " + e.getMessage());
            }
        }

    }

    /**
     * Constructor
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException when exception creating index.
     */
    TextFileIndexer(String indexDir) throws IOException {
        // the boolean true parameter means to create a new index everytime,
        // potentially overwriting any existing files there.
        FSDirectory dir = FSDirectory.open(new File(indexDir).toPath());


        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.getDocStats().numDocs;
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
                // System.out.println("what is json element: " + jsonElement);
                String content = jsonElement.getAsJsonObject().get("text").getAsString();
                String fileUrl = jsonElement.getAsJsonObject().get("url").getAsString();
                String title = jsonElement.getAsJsonObject().get("title").getAsString();
                String abstract_ = jsonElement.getAsJsonObject().get("meta_abstract").getAsString();
                String keywords = jsonElement.getAsJsonObject().get("meta_keywords").getAsString();
                String description = jsonElement.getAsJsonObject().get("meta_description").getAsString();

                doc.add(new TextField("contents", content, Field.Store.YES));
                doc.add(new TextField("title", title, Field.Store.YES));
                doc.add(new TextField("abstract", abstract_, Field.Store.YES));
                doc.add(new TextField("keywords", keywords, Field.Store.YES));
                doc.add(new TextField("description", description, Field.Store.YES));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                doc.add(new StringField("url", fileUrl, Field.Store.YES));

                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = writer.getDocStats().numDocs;
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");
        System.out.println("index success");

        queue.clear();
    }

    private void addFiles(File file) {


        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
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

    private static void SpecificFilesRetrieval() throws IOException, org.apache.lucene.queryparser.classic.ParseException{
        String indexPath = "C:\\Users\\JustP\\OneDrive\\Documents\\SiT\\Y3T3\\CSC3010 Information Retrieval\\Project\\index";
        String[] fields = {"title","url", "contents", "abstract", "description", "keywords"};; // Fields to search
        String[] urlsToRetrieve = {
        "https://www.encyclopedia.com/humanities/dictionaries-thesauruses-pictures-and-press-releases/things",
        "https://www.encyclopedia.com/psychology/dictionaries-thesauruses-pictures-and-press-releases/thing",
        "https://www.encyclopedia.com/science/encyclopedias-almanacs-transcripts-and-maps/are-spooky-things-all-mind",
        "https://www.encyclopedia.com/science/news-wires-white-papers-and-books/things-are-clearer-hindsight",
        "https://www.encyclopedia.com/arts/culture-magazines/things-life",
        "https://www.encyclopedia.com/psychology/dictionaries-thesauruses-pictures-and-press-releases/thing-presentation",
        "https://www.encyclopedia.com/arts/culture-magazines/things-2",
        "https://www.encyclopedia.com/science/news-wires-white-papers-and-books/things-are-clearer-hindsight",
        "https://www.encyclopedia.com/arts/culture-magazines/things-life",
        "https://www.encyclopedia.com/arts/culture-magazines/things-happen-night"

        };

         try {
        
        
        // Step 1: Build a query to search for documents with specific URLs
         Analyzer analyzer = new EnglishAnalyzer();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);

         // Create individual queries for each URL
        BooleanQuery.Builder urlQueryBuilder = new BooleanQuery.Builder();
        for (String url : urlsToRetrieve) {
            Query urlQuery = queryParser.parse(QueryParser.escape(url));
            urlQueryBuilder.add(urlQuery, BooleanClause.Occur.SHOULD);
        }

        // Combine individual queries with OR operator
        Query finalQuery = urlQueryBuilder.build();

        // Step 2: Execute the query and retrieve the matching documents
        TopDocs topDocs = searcher.search(finalQuery, 10); // Adjust the number of desired results (e.g., top 10)

        // Collect the document IDs of the matching documents
        Map<Integer, Document> retrievedDocuments = new HashMap<>();
        for (ScoreDoc hit : topDocs.scoreDocs) {
            int docId = hit.doc;
            Document document = searcher.doc(docId);
            retrievedDocuments.put(docId, document);
        }

        // Step 3: Extract the content of the retrieved documents
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

        // Step 4 and 5: Calculate TF-IDF scores for each term in the context of the retrieved documents
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

        // Step 6: Sort the terms based on TF-IDF scores in descending order
        List<Map.Entry<String, Float>> sortedTerms = new ArrayList<>(tfIdfScores.entrySet());
        sortedTerms.sort((o1, o2) -> Float.compare(o2.getValue(), o1.getValue()));

        // Step 7: Select the top 10 most valuable terms
        int topTermsCount = Math.min(15, sortedTerms.size());
        List<String> topTerms = new ArrayList<>();
        for (int i = 0; i < topTermsCount; i++) {
            topTerms.add(sortedTerms.get(i).getKey());
        }

        // Step 8: Print the top 10 terms
        System.out.println("Top 10 Terms:");
        for (String term : topTerms) {
            System.out.println(term + ": " + tfIdfScores.get(term));
        }

        // Close the reader
        reader.close();} catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
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


    private static String mapToJsonString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Map) {
                sb.append(mapToJsonString((Map<?, ?>) value));
            } else if (value instanceof List) {
                sb.append(listToJsonString((List<?>) value));
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // Convert a List to a JSON string
    private static String listToJsonString(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            Object value = list.get(i);
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof Map) {
                sb.append(mapToJsonString((Map<?, ?>) value));
            } else if (value instanceof List) {
                sb.append(listToJsonString((List<?>) value));
            } else {
                sb.append(value);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    // Save the JSON string to a file
    private static void saveJsonToFile(String jsonString, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

