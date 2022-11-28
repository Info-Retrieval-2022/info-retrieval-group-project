package ie.tcd;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ie.tcd.docParser.*;

/**
 * Hello world!
 *
 */
public class app {

    private static String INDEX_DIRECTORY = "index";

    static ScoreDoc[] queryIndex(int idx, ArrayList<BooleanQuery> queries, int num_hits, IndexSearcher iSearcher) throws IOException, ParseException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        DirectoryReader iReader = DirectoryReader.open(directory);
        TopDocs docs = iSearcher.search(queries.get(idx), num_hits);
        ScoreDoc[] hits = docs.scoreDocs;
        iReader.close();
        directory.close();
        return hits;
    }

    // 1. Parses array list of queries and tokenizes each query using QueryParser
    public static ArrayList<ScoreDoc[]> getHits(IndexSearcher iSearcher, ArrayList<BooleanQuery>queries) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        int hitsperpage = 50;
 
        // For each parsed query, search the index, Store hits in an array of arrays "hits"
        ArrayList<ScoreDoc[]> hits = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            hits.add(queryIndex(i, queries, hitsperpage, iSearcher));
        }
        return hits;
    }

    // For each hit for each query, write output in format required for trec_eval

    public static void getResults(ArrayList<ScoreDoc[]> hits, IndexSearcher iSearcher, String name) throws IOException {
        FileWriter myWriter = new FileWriter(name);
        PrintWriter printWriter = new PrintWriter(myWriter);
        int c = 1;
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < hits.get(i).length; j++) {
                int docId = hits.get(i)[j].doc;
                Document d = iSearcher.doc(docId);
                printWriter.print((i + 401) + " 0 " + d.get("docno") + " " + c + " " + hits.get(i)[j].score + " STANDARD " + '\n');
                c++;
            }
        }
        System.out.println("results file ready");
        myWriter.close();
    }

    /**
     * @param file
     * @param analyzer
     * @return
     * @throws IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static ArrayList<BooleanQuery> createQueries(File file, Analyzer analyzer) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
        ArrayList<BooleanQuery> queries = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String string = "";
        for (int i=0; i<2; i++){
            reader.readLine();
        }
        while (string != null) {
            StringBuilder title = new StringBuilder();
            StringBuilder desc = new StringBuilder();
            StringBuilder narr = new StringBuilder();
            StringBuilder num = new StringBuilder();

            if (string.startsWith("<top>")) {
                string = reader.readLine();
                string = reader.readLine();
            }
            while (!string.startsWith("<title>")) {
                num.append(string).append(' ');
                string = reader.readLine();

            }
            while (!string.startsWith("<desc>")) {
                title.append(string).append(' ');
                string = reader.readLine();
            }
            string = reader.readLine();
            while (!string.startsWith("<narr>")) {
                desc.append(string).append(' ');
                string = reader.readLine();
            }
            string = reader.readLine();
            while (string != null && !string.startsWith("<top>")) {
                narr.append(string).append(' ');
                string = reader.readLine();
            }
            title = title.delete(0,8);
            num = num.delete(0,15);
			/* Run 1 */
            String queryStr = title.toString()+" "+desc.toString()+" "+narr.toString();
            /* Run 2 */
            // String queryStr = title.toString()+" "+narr.toString();
            
            Map<String, Float> boost = new HashMap<>();
            boost.put("headline", (float) 0.1);
            boost.put("text", (float) 0.9);

            queryStr = queryStr.replace("/", "\\/");
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[]{"headline", "text"}, analyzer, boost);
            queryParser.setAllowLeadingWildcard(true);
            Query query = queryParser.parse(queryStr);
            BooleanQuery booleanQuery = new BooleanQuery.Builder().add(query, BooleanClause.Occur.SHOULD).build();
            queries.add(booleanQuery);
            string = reader.readLine();
            }
        reader.close();
        return queries;
        }


    public static void main(String[] args) throws IOException {

        try {
        	/* Run 1 */
        	Analyzer analyzer = new EnglishAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        	/* Run 2 */
//       	Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            
            /* Run 1 */
            BM25Similarity bm25Similarity = new BM25Similarity();
            String name = "BM25";
//          config.setSimilarity(bm25Similarity);
            /* Run 2 */
            LMDirichletSimilarity LMDirichlet = new LMDirichletSimilarity();
            MultiSimilarity combined = new MultiSimilarity(new Similarity[]{bm25Similarity, LMDirichlet});
            name = "combined";
            config.setSimilarity(combined);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            IndexWriter iwriter = new IndexWriter(directory, config);
            
            System.out.println("PWD: " + System.getProperty("user.dir"));
            
            frparser.parseFR94("./Assignment Two/fr94", iwriter);
            latimes_parser.loadLaTimesDocs("./Assignment Two/latimes", iwriter);  
            ftLoader.parseFT("./Assignment Two/ft", iwriter);  
            fbparser.parsefb("./Assignment Two/fbis", iwriter); 
            iwriter.close();
            
            File file = new File("./topics");
            ArrayList<BooleanQuery> queries = createQueries(file, analyzer);
//          do a search if and only if indexes created successfully. 
            DirectoryReader dReader = DirectoryReader.open(directory);
            IndexSearcher iSearcher = new IndexSearcher(dReader);
            iSearcher.setSimilarity(combined);
            ArrayList<ScoreDoc[]> hits = getHits(iSearcher,queries);
            getResults(hits, iSearcher, name + "test.txt");
            directory.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
