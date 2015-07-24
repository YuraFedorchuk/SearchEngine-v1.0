package main;


import java.io.Console;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.ru.RussianLightStemmer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.Directory;
import org.jsoup.Jsoup;


public class Main {
	@SuppressWarnings("unchecked")
	public static void main(String [] args) throws Exception {
		
		System.out.print("Input query: ");
		 Scanner sc = new Scanner(System.in);
	     String currentQuery = sc.next();
		
		
		ArrayList<String> urls = GoogleURLParser.getDataFromGoogle( "\""+currentQuery+"\"" );	
		ArrayList<String> docs = GoogleURLParser.getPagesHTML( urls );
		
	
		Directory dir = IndexSearcher.createIndex(docs);
			
		
		
		LinkedHashMap<String, Integer> termSurroundings = IndexSearcher.getTermSurroundings( dir, IndexSearcher.getQueryPositions( dir, currentQuery ), 70 );
		
		termSurroundings = DataHandler.sortHashMapByValues( termSurroundings );
		termSurroundings = DataHandler.removeEnglishTerms( termSurroundings );
		
		
		termSurroundings = DataHandler.getFirstNResults( termSurroundings, 10 );
		
		LinkedHashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
		
		 for ( Map.Entry<String, Integer> term : termSurroundings.entrySet()) {
			 temp.put(term.getKey(), term.getValue());
		 }
		
		LinkedHashMap<String, Integer> averageResults = IndexSearcher.getBestAverageNeighbourResult( dir, temp );
		
		System.out.print("\nResults for query " + currentQuery + ": ");
		
		System.out.println( IndexSearcher.getResult( termSurroundings, averageResults ) );
		
		
	}	
}
