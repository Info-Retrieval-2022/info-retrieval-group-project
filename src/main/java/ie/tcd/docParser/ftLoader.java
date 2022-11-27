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

public class ftLoader {
    private static ArrayList<Document> doclist;

    public static ArrayList<Document> parseFT(String path) throws IOException {
        System.out.println("Parsing ft...");
        String docno, byline, text, date, headline, pub, dateline;
        doclist = new ArrayList<>();
        File[] directories = new File(path).listFiles(File::isDirectory);
        for(File directory : directories){
            File[] files = directory.listFiles();
            for(File file : files){
                org.jsoup.nodes.Document document = Jsoup.parse(file, null, "");
                Elements elements = document.select("DOC");
                for(Element element : elements){
                    element.select("PAGE").remove();
                    element.select("PROFILE").remove();
                    docno = element.select("DOCNO").text();
                    date = element.select("DATE").text();
                    headline = element.select("HEADLINE").text();
                    pub = element.select("PUB").text();
                    dateline = element.select("DATELINE").text();
                    byline = element.select("BYLINE").text();
                    text = element.select("TEXT").text();
                    System.out.println(docno);
                    Document doc = new Document();
                    doc.add(new StringField("docno", docno, Field.Store.YES));
                    doc.add(new TextField("date", date, Field.Store.YES));
                    doc.add(new TextField("headline", headline, Field.Store.YES));
                    doc.add(new TextField("pub", pub, Field.Store.YES));
          //          doc.add(new TextField("dateline", dateline, Field.Store.YES));
          //          doc.add(new TextField("byline", byline, Field.Store.YES));
                    doc.add(new TextField("text", text, Field.Store.YES));
                    doclist.add(doc);
                }
            }
        }
        System.out.println("Parsing FT done...");
        return doclist;
    }
}
