package ie.tcd;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import ie.tcd.docParser.*;

public class app {

	private static String INDEX_DIRECTORY = "../index";
	
	// Limit the number of search results we get
    private static int MAX_RESULTS = 10;


    public static void main(String[] args) throws IOException {

        try {
			ArrayList<Document> fr_docs = frparser.parseFR94("C:/myProjects/eclipse/Assignment Two/Assignment Two/fr94");			
			ArrayList<Document> la_docs = latimes_parser.loadLaTimesDocs("G:/My Drive/1 Masters/Information Retrieval & Web Search/Assignments/Group 12/Assignment Two/Assignment Two/latimes/");
			ArrayList<Document> ft_docs = ftLoader.parseFT("G:/My Drive/1 Masters/Information Retrieval & Web Search/Assignments/Group 12/Assignment Two/Assignment Two/ft/");
			ArrayList<Document> fb_docs = fbparser.parsefb("G:/My Drive/1 Masters/Information Retrieval & Web Search/Assignments/Group 12/Assignment Two/Assignment Two/fbis/");
			
			ArrayList<Document> all_docs = new ArrayList<Document>();
			
			all_docs.addAll(la_docs);
			all_docs.addAll(fr_docs);
			all_docs.addAll(ft_docs);
			all_docs.addAll(fb_docs);
			
			create_index(all_docs);
			
			do_query();
			
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
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
}