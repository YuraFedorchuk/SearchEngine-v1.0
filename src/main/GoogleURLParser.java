package main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
 
public class GoogleURLParser {


	
	
	
	
	public static ArrayList<String> getPagesHTML(ArrayList<String> urls) {
		ArrayList<String> res = new ArrayList<String>();
		for ( String url: urls ) {
			if ( url.startsWith("http://") || url.startsWith("https://") ) {
				System.out.println( "Parsing " + url );
				res.add( getPageHTML(url) );
			}
		}
		return res;
	}
  
	
	
	
	
	
	
  
	public static String getPageHTML(String urlText) {
		
		for (char c: urlText.toCharArray() ) {
			if ( DataHandler.isCyrillic(c) ) {
				try {
					urlText = urlText.replace(new String(c+""), URLEncoder.encode(new String(c+""), "UTF-8") );
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		Document doc;
		try {
				doc = Jsoup.connect( urlText ).get();
				return Jsoup.parse( doc.html().toString() ).text().replaceAll("([!?,/()]+)|([0-9]{0,10})", "");
		} catch (Exception e) {
			System.out.println("The page " + urlText + " is temporary unavailable");
		}
		return null;  
	}
	  
  
	
	
	
	
  
  
	public static ArrayList<String> getDataFromGoogle(String query) {
		
		ArrayList<String> result = new ArrayList<String>();	
		
		String request = "https://www.google.com/search?q=" + query + "&num=10";
		System.out.println("Sending request..." + request);
		
		// get maximum 50 results from Google
		//for (int i = 0; i < 5; i++ ) {
			
			//request += "&start=" + (i*10);
			try {
				// need http protocol, set this as a Google bot agent
				Document doc = Jsoup.connect(request).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
													 .timeout(20000)
													 .get();
		 
				// get all links
				Elements links = doc.select("a[href]");
				for (Element link : links) {
		 
					String url = link.attr("href");		
					
					// format the url Google link
					if ( url.startsWith("/url?q=") && !url.startsWith("/url?q=http://webcache.googleusercontent.com/search")) {
						
						// remove "/url?q=" string in the beginning
						url = url.substring(7);
						
						// encode URL 2 times
						// first time removes spaces
						// second time encodes
						try {
							url = URLDecoder.decode(URLDecoder.decode(url, "UTF-8"), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						
						
						if ( url.contains("&") ) {
							url = url.substring( 0, url.indexOf('&') );
						}
						
						result.add( url );
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		//}
		return result;
	}
 
  
	
	

	
	
}