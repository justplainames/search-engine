import com.google.gson.JsonElement;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import com.example.lucene.service.CustomSimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class TextFileIndexer {
    private static StandardAnalyzer analyzer = new StandardAnalyzer();

    private IndexWriter writer;
    private List<File> queue = new ArrayList();


    public static void main(String[] args) throws IOException {
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
        CustomSimilarity customSimilarity = new CustomSimilarity();
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(customSimilarity);
        TopScoreDocCollector collector = TopScoreDocCollector.create(5, 20);

        s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }
                Query q = new QueryParser("contents", analyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Found " + hits.length + " hits.");
                for(int i=0;i<hits.length;++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("url") + " score=" + hits[i].score);
                }

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
//                System.out.println("what is json element: " + jsonElement);
                String content = jsonElement.getAsJsonObject().get("text").getAsString();
                String fileUrl = jsonElement.getAsJsonObject().get("url").getAsString();
                String title = jsonElement.getAsJsonObject().get("title").getAsString();

                doc.add(new TextField("contents", content, Field.Store.YES));
                doc.add(new TextField("title", title, Field.Store.YES));
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

//    private void addJsonFile(File file) throws FileNotFoundException {
//        try(FileReader fileReader = new FileReader(file)) {
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//
//            StringBuilder jsonContent = new StringBuilder();
//
//            String line;
//            while((line = bufferedReader.readLine()) != null){
//                jsonContent.append(line);
//            }
//
//            // parse json content using GSON
//            Gson gson = new Gson();
//            JsonParser jsonParser = new JsonParser();
//            JsonObject jsonObject = new JsonObject();
//
//            // Access the json data
//            String text = jsonObject.get("text").getAsString();
//            System.out.println("what is json text :" + text);
////            queue.add(text);

//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//    }

    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}