package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import org.example.docParser.*;

/**
 * Hello world!
 *
 */
public class App {

    private static String INDEX_DIRECTORY = "../index";

    // Limit the number of search results we get
    private static int MAX_RESULTS = 10;


    private static void create_index(ArrayList<Document> docs) {
        try {
            // Analyzer that is used to process TextField
            Analyzer analyzer = new StandardAnalyzer();

            // Open the directory that contains the search index
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

            // Set up an index writer to add process and save documents to the index
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter iwriter = new IndexWriter(directory, config);

            // Write all the documents in the linked list to the search index
            iwriter.addDocuments(docs);

            // Commit everything and close
            iwriter.close();
            directory.close();
            System.out.println("Index Created at: " + System.getProperty("user.dir"));

        }
        catch (Exception e) {

        }
    }

    private static void do_query(){
        try {
            // Open the folder that contains our search index
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

            // create objects to read and search across the index
            DirectoryReader ireader = DirectoryReader.open(directory);
            IndexSearcher isearcher = new IndexSearcher(ireader);

            // builder class for creating our query
            BooleanQuery.Builder query = new BooleanQuery.Builder();

            // Some words that we want to find and the field in which we expect
            // to find them
            Query term1 = new TermQuery(new Term("text", "Gorbachev"));
            Query term2 = new TermQuery(new Term("text", "glasnost"));
//	        Query term3 = new TermQuery(new Term("content", "criticism"));

            // construct our query using basic boolean operations.
            query.add(new BooleanClause(term1, BooleanClause.Occur.SHOULD   ));   // AND
            query.add(new BooleanClause(term2, BooleanClause.Occur.SHOULD));
//	        query.add(new BooleanClause(term3, BooleanClause.Occur.MUST_NOT)); // NOT

            // Get the set of results from the searcher
            ScoreDoc[] hits = isearcher.search(query.build(), MAX_RESULTS).scoreDocs;

            // Print the results
            System.out.println("Documents: " + hits.length);
            for (int i = 0; i < hits.length; i++)
            {
                Document hitDoc = isearcher.doc(hits[i].doc);
                System.out.println(i + ") " + hitDoc.get("docno") + " " + hits[i].score);
            }

            // Close everything we used
            ireader.close();
            directory.close();
        }
        catch (Exception e) {

        }
    }

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
        StandardAnalyzer analyzer = new StandardAnalyzer();
        WhitespaceAnalyzer whitespaceAnalyzer = new WhitespaceAnalyzer();
        SimpleAnalyzer simpleAnalyzer = new SimpleAnalyzer();

        int hitsperpage = 50;
        int id = 0;

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
                printWriter.print((i + 1) + " 0 " + d.get("docno") + " " + c + " " + hits.get(i)[j].score + " STANDARD " + '\n');
                c++;
            }
        }
        System.out.println("results file ready");
        myWriter.close();
    }

    public static ArrayList<BooleanQuery> createQueries(File file) throws IOException {
        ArrayList<BooleanQuery> queries = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String string = "";
        for (int i=0; i<2; i++){
            reader.readLine();
        }
        int c = 1;
        while (string != null) {
            StringBuilder title = new StringBuilder();
            StringBuilder desc = new StringBuilder();
            StringBuilder narr = new StringBuilder();
            StringBuilder num = new StringBuilder();

            Document doc = new Document();

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
//           doc.add(new TextField("id", Integer.toString(c), Field.Store.YES));
            title = title.delete(0,8);
            num = num.delete(0,15);
            String temp = title.toString();
            temp = temp.replace(",","");
            String[] titles = temp.split("\\s+");

            if(titles.length==1){
                Term t = new Term("text", String.valueOf(title));
                TermQuery tq_tit = new TermQuery(t);
                BooleanQuery booleanQuery= new BooleanQuery.Builder()
                        .add(tq_tit, BooleanClause.Occur.MUST)
                        .build();
                queries.add(booleanQuery);
            }
            if(titles.length==2){
                Term t1 = new Term("text", String.valueOf(titles[0]));
                Term t2 = new Term("text", String.valueOf(titles[1]));
                TermQuery tq1 = new TermQuery(t1);
                TermQuery tq2 = new TermQuery(t2);
                BooleanQuery booleanQuery= new BooleanQuery.Builder()
                        .add(tq1, BooleanClause.Occur.SHOULD)
                        .add(tq2, BooleanClause.Occur.SHOULD)
                        .build();
                queries.add(booleanQuery);
            }
            if(titles.length==3){
                Term t1 = new Term("text", String.valueOf(titles[0]));
                Term t2 = new Term("text", String.valueOf(titles[1]));
                Term t3 = new Term("text", String.valueOf(titles[2]));
                TermQuery tq1 = new TermQuery(t1);
                TermQuery tq2 = new TermQuery(t2);
                TermQuery tq3 = new TermQuery(t3);
                BooleanQuery booleanQuery= new BooleanQuery.Builder()
                        .add(tq1, BooleanClause.Occur.SHOULD)
                        .add(tq2, BooleanClause.Occur.SHOULD)
                        .add(tq3, BooleanClause.Occur.SHOULD)
                        .build();
                queries.add(booleanQuery);
            }

            if(titles.length==4){
                Term t1 = new Term("text", String.valueOf(titles[0]));
                Term t2 = new Term("text", String.valueOf(titles[1]));
                Term t3 = new Term("text", String.valueOf(titles[2]));
                Term t4 = new Term("text", String.valueOf(titles[3]));
                TermQuery tq1 = new TermQuery(t1);
                TermQuery tq2 = new TermQuery(t2);
                TermQuery tq3 = new TermQuery(t3);
                TermQuery tq4 = new TermQuery(t4);
                BooleanQuery booleanQuery= new BooleanQuery.Builder()
                        .add(tq1, BooleanClause.Occur.SHOULD)
                        .add(tq2, BooleanClause.Occur.SHOULD)
                        .add(tq3, BooleanClause.Occur.SHOULD)
                        .add(tq4, BooleanClause.Occur.SHOULD)
                        .build();
                queries.add(booleanQuery);
            }



//            Term num_term = new Term("num", String.valueOf(num));
//            Term tit_term = new Term("title", String.valueOf(title));
//            Term desc_term = new Term("desc", String.valueOf(desc));
//            Term narr_term = new Term("narr", String.valueOf(narr));
//
//            TermQuery tq_num = new TermQuery(num_term);
//            TermQuery tq_tit = new TermQuery(tit_term);
//            TermQuery tq_desc = new TermQuery(desc_term);
//            TermQuery tq_narr = new TermQuery(narr_term);

//            BooleanQuery booleanQuery
//                    = new BooleanQuery.Builder()
//                    .add(tq_tit, BooleanClause.Occur.MUST)
//                    .add(tq_desc, BooleanClause.Occur.MUST)
//                    .add(tq_narr, BooleanClause.Occur.MUST)
//                    .build();

//            BooleanQuery booleanQuery
//                    = new BooleanQuery.Builder()
//                    .add(tq_tit, BooleanClause.Occur.SHOULD)
//                    .build();

            string = reader.readLine();
            c++;
            }

        return queries;
        }


    public static void main(String[] args) throws IOException {

        try {
            Analyzer analyzer = new StandardAnalyzer();

            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            DirectoryReader dReader = DirectoryReader.open(directory);
            IndexSearcher iSearcher = new IndexSearcher(dReader);
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            BM25Similarity bm25Similarity = new BM25Similarity();
            String name = "BM25";
            config.setSimilarity(bm25Similarity);
            iSearcher.setSimilarity(bm25Similarity);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            ArrayList<Document> fr_docs = frparser.parseFR94("C:\\masters\\CS7IS3\\assignment_2\\Assignment Two\\fr94\\");
            ArrayList<Document> la_docs = laTimes_parser.loadLaTimesDocs("C:\\masters\\CS7IS3\\assignment_2\\Assignment Two\\latimes\\");
            ArrayList<Document> ft_docs = ftLoader.parseFT("C:\\masters\\CS7IS3\\assignment_2\\Assignment Two\\ft\\");
            ArrayList<Document> fb_docs = fbparser.parsefb("C:\\masters\\CS7IS3\\assignment_2\\Assignment Two\\fbis\\");

            ArrayList<Document> all_docs = new ArrayList<Document>();

            all_docs.addAll(la_docs);
            all_docs.addAll(fr_docs);
            all_docs.addAll(ft_docs);
            all_docs.addAll(fb_docs);

            create_index(all_docs);

            File file = new File("C:\\masters\\CS7IS3\\assignment_2\\topics");
            ArrayList<BooleanQuery> queries = createQueries(file);
            ArrayList<ScoreDoc[]> hits = getHits(iSearcher,queries);
            getResults(hits, iSearcher, name + "test.txt");
            directory.close();
            //do_query();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}