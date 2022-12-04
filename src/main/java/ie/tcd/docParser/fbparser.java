package ie.tcd.docParser;

import java.io.IOException;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.StopAnalyzer; 
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.jsoup.Jsoup;  
import org.jsoup.nodes.Element;  
import org.jsoup.select.Elements;  

import java.io.File;
 
public class fbparser
{
    public static ArrayList<Document> parsefb(String input, IndexWriter iwriter) throws IOException {
    	ArrayList<Document> doclist = new ArrayList<>();
		int Fcnt = 0;
		File dir = new File(input);
		File[] directoryListing = dir.listFiles();
			for (File child : directoryListing) {
				if((child.getName()).startsWith("fb")){
				int Icnt = 0;

				
				++Fcnt;

				org.jsoup.nodes.Document doc = Jsoup.parse(child,"utf-8");//assuming register.html file in e drive  
		
				Elements ele = doc.getElementsByTag("DOC");
				Icnt = 0;

				for(Element link : ele){
					Document document = new Document();

					// System.out.println(link.getElementsByTag("DOCNO").text() +"\n");
				//	String id = link.getElementsByTag("DOCNO").text();
				//	document.add(new StringField("docno", id, Field.Store.YES));

					//System.out.println(link.getElementsByTag("DATE1").text()+"\n");
				//	String date = link.getElementsByTag("DATE1").text();
				//	document.add(new TextField("date", date, Field.Store.YES));

					// System.out.println(link.getElementsByTag("TI").text()+"\n");
					String headline = link.getElementsByTag("TI").text();
					document.add(new TextField("headline", headline, Field.Store.YES));

					// System.out.println(link.getElementsByTag("TEXT").text()+"\n");
					String text = link.getElementsByTag("TEXT").text();
					document.add(new TextField("text", text, Field.Store.YES));

					// System.out.println(link.getElementsByAttributeValue("P", "104").text()+"\n");
				//	String pub = link.getElementsByAttributeValue("P", "104").text();
				//	document.add(new TextField("pub", pub, Field.Store.YES));
				//	doclist.add(document);
					iwriter.addDocument(document);
					++Icnt;

				}
				System.out.println(child.getName()+ " Docnos "+Integer.toString(Icnt));
			}
		}
		return doclist;
    }
}
