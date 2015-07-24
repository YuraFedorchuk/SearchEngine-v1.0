package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.store.Directory;

public class DataHandler {

	
	
	
	
	
	/*
	 * Sort HashMap by values
	 * 
	 * @param unsorted HashMap
	 */
	
	public static LinkedHashMap<String, Integer> sortHashMapByValues(LinkedHashMap<String, Integer> passedMap) {
	   List mapKeys = new ArrayList(passedMap.keySet());
	   List mapValues = new ArrayList(passedMap.values());
	   
	   Comparator cmp = Collections.reverseOrder();
	   
	   Collections.sort(mapValues, cmp);
	   Collections.sort(mapKeys, cmp);

	   LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();

	   Iterator valueIt = mapValues.iterator();
	   while (valueIt.hasNext()) {
	       Object val = valueIt.next();
	       Iterator keyIt = mapKeys.iterator();

	       while (keyIt.hasNext()) {
	           Object key = keyIt.next();
	           String comp1 = passedMap.get(key).toString();
	           String comp2 = val.toString();

	           if (comp1.equals(comp2)){
	               passedMap.remove(key);
	               mapKeys.remove(key);
	               sortedMap.put( (String)key, (Integer)val );
	               break;
	           }

	       }

	   }
	   return sortedMap;
	}
	
	
	
	/*
	 * Return first N results of HashMap
	 * 
	 * @param target HashMap
	 */
	
	public static LinkedHashMap<String, Integer> getFirstNResults(LinkedHashMap<String, Integer> aim, Integer n) {
		LinkedHashMap<String, Integer> res = new LinkedHashMap<String, Integer>();
		Iterator it = aim.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			if ( i == n )
				break;
			Map.Entry pair = (Map.Entry)it.next();
			String word = (String) pair.getKey();
			res.put(word, (Integer)pair.getValue() );
			it.remove();
			i++;
		}
		return res;
	}
	
	
	
	
	
	
	
	/*
	 * Remove English words from HashMap
	 * 
	 * @param target HashMap
	 */
	
	public static LinkedHashMap<String, Integer> removeEnglishTerms( LinkedHashMap<String, Integer> target ) {
		LinkedHashMap<String, Integer> res = new LinkedHashMap<String, Integer>();
		Iterator it = target.entrySet().iterator();
		while (it.hasNext()) {
			
			Map.Entry pair = (Map.Entry)it.next();
			String word = (String) pair.getKey();
			word = word.replaceAll("[a-zA-Z.@_]+", "");
			if ( !word.equals("") && Character.isUpperCase(word.charAt(0)) ) {
				res.put (word, (Integer)pair.getValue() );
			}
			it.remove();
		}
		return res;
	}
	
	
	
	
	
	
	
	/*
	 * Remove same entries and calculate number of entries in HashMap
	 * In other words - delete duplicates and count how much duplicates was in there
	 * Return HashMap <term, frequency>
	 * 
	 * @param target HashMap (with unique Id and lots of duplicates)
	 */
	
	public static LinkedHashMap<String, Integer> removeIdenticTermsAndCalculateFrequency( LinkedHashMap<String, String> target ) {
		
		LinkedHashMap<String, Integer> res = new LinkedHashMap<String, Integer>();
		
		
		ArrayList<String> temp = new ArrayList<String>();
		for ( Map.Entry<String, String> entry : target.entrySet()) {
			temp.add( entry.getValue() );
		}
		
		
		for (String s: temp) {
			int counter = 0;
			String word = "";
			for ( Map.Entry<String, String> entry : target.entrySet() ) {
				if ( s.equals( entry.getValue() ) ) {
					counter++;
				}
			}
			res.put(s, counter);
		}
		
		return res;
	}
	
	
	
	
	
	
	/*
	 * Check if symbol is cyrillic
	 * 
	 * @param symbol
	 */
	static boolean isCyrillic(char c) {
		return Character.UnicodeBlock.CYRILLIC.equals(Character.UnicodeBlock.of(c));
	}
	
}
