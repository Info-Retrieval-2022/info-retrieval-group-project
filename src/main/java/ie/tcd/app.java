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
import org.apache.lucene.search.BooleanQuery.Builder;
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
import java.util.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ie.tcd.docParser.*;

/**
 * Hello world!
 *
 */
public class app {

    private static String INDEX_DIRECTORY = "index";
    private static int run_choice = 1;
    static ScoreDoc[] queryIndex(int idx, ArrayList<BooleanQuery> queries, int num_hits, IndexSearcher iSearcher) throws IOException, ParseException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        DirectoryReader iReader = DirectoryReader.open(directory);
        TopDocs docs = iSearcher.search(queries.get(idx), num_hits);
        ScoreDoc[] hits = docs.scoreDocs;
        iReader.close();
        directory.close();
        return hits;
    }


    public static List<String> StringSplitter(String text) {
        text = text.replace("e.g."," ");
        text = text.replace("i.e."," ");
        text = text.replace("U.S. ","United States");
        Pattern pattern = Pattern.compile("\\.|\\;");
        Matcher matcher = pattern.matcher(text);

        List<String> sentences = new ArrayList<>();
        int start = 0;
        while (matcher.find()) {
            sentences.add(text.substring(start, matcher.start() + 1));
            start = matcher.start() + 1;
        }

        return sentences;
    }
    public static List<String> getRelevant(String narr){

        List<String> sentences = StringSplitter(narr);
        List<String> relevantSentences = new ArrayList<>();
        for (String sentence : sentences) {
            if (!(sentence.toLowerCase().contains("not relevant"))&&!(sentence.toLowerCase().contains("irrelevant"))) {
                sentence = sentence.replace(" relevant ", " ");
                relevantSentences.add(sentence);
            }
        }
        return relevantSentences;
    }

    public static List<String> getIrrelevant(String narr){
        List<String> sentences = StringSplitter(narr);
        List<String> irrelevantSentences = new ArrayList<>();
        for (String sentence : sentences) {
            if ((sentence.toLowerCase().contains("not relevant"))||(sentence.toLowerCase().contains("irrelevant"))){
                sentence = sentence.replace(" not relevant ", " ");
                irrelevantSentences.add(sentence);
            }
        }
        return irrelevantSentences;
    }

    // 1. Parses array list of queries and tokenizes each query using QueryParser
    public static ArrayList<ScoreDoc[]> getHits(IndexSearcher iSearcher, ArrayList<BooleanQuery>queries) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
        int hitsperpage = 1000;

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
        System.out.println("results file ready: "+ name);
        myWriter.close();
    }

    public static String[] get_rel_list(String Title, String relevant) {
        String[] rel = relevant.split("\\s+");
        String[] tit = Title.split("\\s+");
        String[] words = new String[rel.length + tit.length];
        for (int i = 0; i < rel.length; i++) {
            words[i] = rel[i].replaceAll("[^\\w]", "");
        }

        for (int i = rel.length; i < words.length; i++) {
            words[i] = tit[i-rel.length].replaceAll("[^\\w]", "");
        }
        return words;
    }

    public static String trimIrrelevant(String Title, String relevant, String irrelevant){
        String[] words = get_rel_list(Title, relevant);
        for (String word : words) {
            if (irrelevant.contains(word)) {
                irrelevant = irrelevant.replace(" " + word + " ", " ");
            }
        }
        irrelevant = irrelevant.replace("not relevant", " ");
        return irrelevant;
    }

    public static String trimRelevant(String relevant){
        if (relevant.contains(", not be")) {
            relevant = StringUtils.substringBefore(relevant, ", not be");
        }
        if (relevant.contains(", not in")) {
            relevant = StringUtils.substringBefore(relevant, ", not in");
        }
        if (relevant.contains(", not merely")) {
            relevant = StringUtils.substringBefore(relevant, ", not merely");
        }
        if (relevant.contains("as opposed to")) {
            relevant = StringUtils.substringBefore(relevant, "as opposed to");
        }
        if (relevant.contains("other than")){
            String b4 = StringUtils.substringBefore(relevant, "other than");
            String after = StringUtils.substringAfter(relevant, "in");
            relevant = b4+after;
        }
        return relevant;
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
            String queryStrTitle = title.toString();
            String queryStrDesc = desc.toString();
            String queryStrNarr = narr.toString();
            List<String> rels = getRelevant(queryStrNarr);
            for (int i = 0; i< rels.size();i++){
                rels.set(i, trimRelevant(rels.get(i)));
            }

            List<String> irrels = getIrrelevant(queryStrNarr);
            queryStrNarr = String.join(", ", rels);
            String queryStrIrrel = String.join(", ", irrels);
            queryStrIrrel = trimIrrelevant(queryStrTitle, queryStrNarr, queryStrIrrel);



            Map<String, Float> boost = new HashMap<>();
            boost.put("headline", 0.1f);
            boost.put("text", 0.9f);

            Map<String, Float> negboost = new HashMap<>();
            negboost.put("headline", 0.05f);
            negboost.put("text", 0.95f);

            MultiFieldQueryParser negqueryParser = new MultiFieldQueryParser(new String[]{"headline", "text"}, analyzer, negboost);
            negqueryParser.setAllowLeadingWildcard(true);
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(new String[]{"headline", "text"}, analyzer, boost);
            queryParser.setAllowLeadingWildcard(true);

            Query queryTitle = queryParser.parse(QueryParser.escape(queryStrTitle));
            Query queryDesc = queryParser.parse(QueryParser.escape(queryStrDesc));
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            booleanQuery.add(new BoostQuery(queryTitle,5f), BooleanClause.Occur.SHOULD);
            booleanQuery.add(new BoostQuery(queryDesc,4f), BooleanClause.Occur.SHOULD);
            if (queryStrNarr.length()>0) {
                Query queryNarr = queryParser.parse(QueryParser.escape(queryStrNarr));
                booleanQuery.add(new BoostQuery(queryNarr, 3f),BooleanClause.Occur.SHOULD);
            }

            // Irrelevant prt of query hurting map currently- Can comment out this if.
            // Exclude this query to score 0.3 for MAP
//            if (queryStrIrrel.length()>0) {
//                Query queryIrrel = negqueryParser.parse(QueryParser.escape(queryStrIrrel));
//                booleanQuery.add(new BoostQuery(queryIrrel, 1f),BooleanClause.Occur.MUST_NOT);
//            }
            queries.add(booleanQuery.build());
            string = reader.readLine();
        }
        //   System.out.println(queries);
        reader.close();
        return queries;
    }



    //    1.to run the run5(best) setup and create index, use cmd:
//    # java -jar target/assxx.jar 5 c
//    2.to run the run5(best) setup without create index, use cmd:
//    # java -jar target/assxx.jar 5
//    or
//    # java -jar target/assxx.jar
    public static void main(String[] args) throws IOException {

        try {
            String QUERY_RESULT_NAME = "RUN5"; //default

            if(args.length >=1) {
                run_choice = Integer.parseInt(args[0]);
                QUERY_RESULT_NAME = "RUN" + args[0];
            }

            /* Run 1 */
            Analyzer analyzer = new EnglishAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
            /* Run 2 */
//        	Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            /* Run 1 */
            BM25Similarity bm25Similarity = new BM25Similarity();

//          config.setSimilarity(bm25Similarity);
            /* Run 2 */
            LMDirichletSimilarity LMDirichlet = new LMDirichletSimilarity();
            MultiSimilarity combined = new MultiSimilarity(new Similarity[]{bm25Similarity, LMDirichlet});

            config.setSimilarity(combined);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

            // Create Index optional
            if((args.length >=2) && args[1].equals("c"))
            {
                IndexWriter iwriter = new IndexWriter(directory, config);
                System.out.println("PWD: " + System.getProperty("user.dir"));
                frparser.parseFR94("./Assignment Two/fr94", iwriter);
                latimes_parser.loadLaTimesDocs("./Assignment Two/latimes", iwriter);
                ftLoader.parseFT("./Assignment Two/ft", iwriter);
                fbparser.parsefb("./Assignment Two/fbis", iwriter);
                iwriter.close();
            }

            File file = new File("./topics");
            ArrayList<BooleanQuery> queries = createQueries(file, analyzer);
//          do a search if and only if indexes created successfully.
            DirectoryReader dReader = DirectoryReader.open(directory);
            IndexSearcher iSearcher = new IndexSearcher(dReader);
            iSearcher.setSimilarity(combined);
            ArrayList<ScoreDoc[]> hits = getHits(iSearcher,queries);
            System.out.println("Searching.....");
            getResults(hits, iSearcher, QUERY_RESULT_NAME + ".txt");
            directory.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
