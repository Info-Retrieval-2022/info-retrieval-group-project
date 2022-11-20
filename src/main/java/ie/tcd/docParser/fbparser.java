package ie.tcd.dalyc24;

import java.io.IOException;

import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
    
    private static String INDEX_DIRECTORY = "../index";

    public static void main(String[] args) throws IOException
    {
        Analyzer analyzer;
        Scanner sc= new Scanner(System.in);
        System.out.println("Analyzers - 1: Standard 2: Stop 3: WhiteSpace 4: English");
        String choice = sc.next();
        choice = choice.trim(); 
        if(choice.equals("1")){
            analyzer = new StandardAnalyzer();
        }
        else if(choice.equals("2")){
            analyzer = new StopAnalyzer();
        }
        else if(choice.equals("3")){
            analyzer = new WhitespaceAnalyzer();
        }
        else if(choice.equals("4")){
            analyzer = new EnglishAnalyzer();
        }
        else{
            System.out.println(choice+" Invalid Choice" );
            return;
        }
        
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        
        IndexWriter iwriter = new IndexWriter(directory, config); 
        
		int Fcnt = 0;
		File dir = new File("../fbis");
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
					String id = link.getElementsByTag("DOCNO").text();
					document.add(new TextField("id", id, Field.Store.YES));

					// System.out.println(link.getElementsByTag("DATE1").text()+"\n");
					String date = link.getElementsByTag("DATE1").text();
					document.add(new TextField("date", date, Field.Store.YES));

					// System.out.println(link.getElementsByTag("TI").text()+"\n");
					String headline = link.getElementsByTag("TI").text();
					document.add(new TextField("headline", headline, Field.Store.YES));

					// System.out.println(link.getElementsByTag("TEXT").text()+"\n");
					String text = link.getElementsByTag("TEXT").text();
					document.add(new TextField("text", text, Field.Store.YES));

					// System.out.println(link.getElementsByAttributeValue("P", "104").text()+"\n");
					String pub = link.getElementsByAttributeValue("P", "104").text();
					document.add(new TextField("publication", pub, Field.Store.YES));

					// System.out.println("-----------");
					iwriter.addDocument(document);

					++Icnt;

				}

				System.out.println(child.getName()+ " Docnos "+Integer.toString(Icnt));

			}

		}

        
        iwriter.close();
        directory.close();
    }
}
