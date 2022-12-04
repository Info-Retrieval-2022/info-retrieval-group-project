package ie.tcd.docParser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class latimes_parser {

    public static ArrayList<Document> loadLaTimesDocs(String pathToLADocs, IndexWriter iwriter) throws IOException {
        System.out.println("Loading LATIMES ...");
        ArrayList<Document> laDocs = new ArrayList<>();

        File folder = new File(pathToLADocs);
        File[] allFiles = folder.listFiles();

        for (File file : allFiles) {

            org.jsoup.nodes.Document allContent = Jsoup.parse(file, null, "");
            System.out.println("reading file: " + file.getName());
            Elements docs = allContent.select("DOC");

            for (Element doc : docs) {
                String docNo, date, headline, text;
                docNo = (doc.select("DOCNO").text());
                date = (doc.select("Date").select("P").text());
                headline = (doc.select("HEADLINE").select("P").text());
                text = (doc.select("TEXT").select("P").text());
              //  laDocs.add(createDocument(docNo, date, headline, text));
                iwriter.addDocument(createDocument(docNo, date, headline, text));
            }
        }
        System.out.println("Loading LATIMES Done!");
        return laDocs;
    }

    private static org.apache.lucene.document.Document createDocument(String docNo, String date, String headline, String text) {
    	String publication = "LA Times";
    	
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
   //     document.add(new StringField("docno", docNo, Field.Store.YES));
   //     document.add(new TextField("date", date, Field.Store.YES));
        document.add(new TextField("headline", headline, Field.Store.YES));
        document.add(new TextField("text", text, Field.Store.YES));
   //     document.add(new TextField("pub", publication, Field.Store.YES));
        return document;
    }

  
}