package ie.tcd.docParser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class frparser {
    public static ArrayList<Document> parseFR94(String path) throws IOException {
    	ArrayList<Document> doclist = new ArrayList<>();
        System.out.println("Parsing fr94 documents...");
        File[] directories = new File(path).listFiles(File::isDirectory);
        String DOCNO_Str, DATE_Str, AGENCY_Str, SUMMARY_Str, ACTION_Str;
        for(File directory : directories){
            File[] files = directory.listFiles();
            for(File file : files) {
                org.jsoup.nodes.Document doc = Jsoup.parse(file, null, "");
                Elements elements = doc.select("DOC");
                for(Element element : elements){
                    // ignore child elements
                    // these elements are not relevant to valuable information
                    element.select("ADDRESS").remove();
                    element.select("SIGNER").remove();
                    element.select("SIGNJOB").remove();
                    element.select("BILLING").remove();
                    element.select("FRFILING").remove();
                    element.select("RINDOCK").remove();
                    DOCNO_Str = element.select("DOCNO").text();
                    ACTION_Str = element.select("ACTION").text();
                    SUMMARY_Str = element.select("SUMMARY").text();
                    DATE_Str = element.select("DATE").text();
                    AGENCY_Str = element.select("AGENCY").text();
                    System.out.println("reading file: " + DOCNO_Str);
                    Document document = new Document();
                    document.add(new StringField("docno", DOCNO_Str, Field.Store.YES));
                    document.add(new TextField("date", DATE_Str, Field.Store.YES));
                    document.add(new TextField("headline", ACTION_Str, Field.Store.YES));
                    document.add(new TextField("text", SUMMARY_Str, Field.Store.YES));
                    document.add(new TextField("pub", AGENCY_Str, Field.Store.YES));
                    doclist.add(document);
                }
            }
        }
        System.out.println("Parsing FR94 done...");
        return doclist;
	}
}
