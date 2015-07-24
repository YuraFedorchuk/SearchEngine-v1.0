package main;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

public class IndexSearcher {
	
	/*
	 * Creates index 
	 * @param String array: each element is document text
	 */
	
	public static Directory createIndex( ArrayList<String> docs ) throws IOException {
		
		// Create new stop-words set of English + Russians stop-words
		CharArraySet engRus = RussianAnalyzer.getDefaultStopSet();
		for ( Object word: StopAnalyzer.ENGLISH_STOP_WORDS_SET ) {
			engRus.add( word );
		}
		
		
		// ClassicAnalyzer is used to index email correctly
		ClassicAnalyzerCaseSensitive analyzer = new ClassicAnalyzerCaseSensitive( engRus );

		
		// Create index in RAM
		Directory index = new RAMDirectory();
		
		IndexWriterConfig config = new IndexWriterConfig( analyzer );
		IndexWriter w = new IndexWriter( index, config );
		
		
		// Indexing documents
		int i = 0;
		for (String doc: docs) {
			System.out.println("Indexing... DOC #"+(i+1));
			if ( doc != null ) {
				addDoc(w, doc, (i+1)+"" );
			} else {
				System.out.println("DOC #"+(i+1)+" is not indexed! May be caused by Internet connection problem.");
			}
			i++;
		}
		
		w.close();
		
		return index;
	}
	
	
	
	
	
	
	/*
	 * Add Document to index. Storing docIds, frequencies and positions
	 * @param IndexWriter entity
	 * @param Field title = document number
	 * @param Field content = document text
	 */	
	
	private static void addDoc( IndexWriter w, String title, String content ) throws IOException {
		
		Document doc = new Document();
		
		FieldType ft = new FieldType();
		ft.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS );
		ft.setTokenized(true);
	
		
		doc.add( new TextField( "title", title, Field.Store.YES	 ) );
		doc.add( new Field( "content", content, ft  ) );
		
		w.addDocument(doc);
	}
	
	
	
	
	
	
	/*
	 * Read created Index from Directory
	 * @param indexing directory
	 */
	
	public static void readIndex( Directory indexDir ) throws IOException {
	   
		IndexReader ir = DirectoryReader.open(indexDir);
	    
	    Fields fields =  MultiFields.getFields(ir);
	    System.out.println("TOTAL DOCUMENTS : " + ir.numDocs());

	    for ( String field : fields ) {
	        Terms terms = fields.terms(field);
	        TermsEnum termsEnum = terms.iterator(null);
	        
	        BytesRef text;
	        LinkedHashMap<String, Integer> textFreq = new LinkedHashMap<String, Integer>();
	        while ( (text = termsEnum.next()) != null ) {
	        	
	        	System.out.println("Text:  " + text.utf8ToString() + ";    frequency:   " + termsEnum.totalTermFreq() );
	        	
	        	DocsAndPositionsEnum docPosEnum = termsEnum.docsAndPositions(null, null);
		        
	        	while ( docPosEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS ) {
	        		//if (text.utf8ToString().equals("imafos_t@ukr.net")) {
	        		//System.out.print("     Doc #" + (docPosEnum.docID()+1) + ":");
		        	int freq = docPosEnum.freq();
		            for( int i = 0; i < freq; i++ ) {
		                int position = docPosEnum.nextPosition();
		                //System.out.print(" pos #: " + position + ";");
		            //}
		                
	        		}
		            //System.out.println();
		        }
	       
	        }
	    }
	}
	
	
	
	
	
	/*
	 * Find all terms in all documents, that are stored in position +-coeff to term
	 * used with function getTermSurroundings( directory, term ), which returns ArrayList of <term, frequency>
	 * 
	 * @param index directory
	 * @param HashMap for query term <docid, ArrayList<positions>>
	 * @param coefficient for measuring number of positions
	 * 		  more and less than query (default = 50)
	 */
	
	public static LinkedHashMap<String, Integer> getTermSurroundings( 	Directory indexDir, 
								LinkedHashMap<Integer, ArrayList<Integer>> termDocPos,
								int coeff ) throws IOException {
		   
		IndexReader ir = DirectoryReader.open(indexDir);
	    
	    Fields fields =  MultiFields.getFields(ir);
	    System.out.println("TOTAL DOCUMENTS : " + ir.numDocs());
	    LinkedHashMap<String, Integer> res = new LinkedHashMap<String, Integer>();
        
        
        
        
        Iterator it = termDocPos.entrySet().iterator();
		
		// query term docIds loop
		while ( it.hasNext() ) {
			Map.Entry pair = (Map.Entry)it.next();
			//System.out.println( pair.getKey() + "    " + pair.getValue() );
			
		
			ArrayList<Integer> queryPositions = (ArrayList<Integer>) pair.getValue();
			// query docId positions loop
			for (Integer pos: queryPositions) {
				
				
				
				/* INDEX LOOP */
			    for ( String field : fields ) {
			        Terms terms = fields.terms(field);
			        TermsEnum termsEnum = terms.iterator(null);
			        
			        BytesRef text;
			      
			        				
					
			        // all index terms loop
			        while ( (text = termsEnum.next()) != null ) {
			        	
			        	DocsAndPositionsEnum docPosEnum = termsEnum.docsAndPositions(null, null);
				        
			        	// all single term docId-[positions] loop
			        	while ( docPosEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS ) {
			        		
			        			if ( docPosEnum.docID() == (Integer)pair.getKey() ) {
			        				
			        				int freq = docPosEnum.freq();
						            for( int i = 0; i < freq; i++ ) {
						                int position = docPosEnum.nextPosition();
						                
						                if ( position > (pos - coeff) && 
						                     position < (pos + coeff) ) {
						                	 
						                	res.put( text.utf8ToString(), (int)termsEnum.totalTermFreq() );
						                	
						                }
						            }
			        			}				            
				        }
			        } // END While text=termsEnum.next() ...
	        
				} // END INDEX LOOP
			        
			} // END For pos
			it.remove();
		} // END While it.hasNext()
	        
	    return res;    
	}
	
	
	
	
	
	
	
	/*
	 * Return HashMap of <docId, positions[]> for query Term
	 * Find documents and positions in the index, where query is located
	 * 
	 * @param index directory
	 * @param query Term for search
	 */
	
	public static LinkedHashMap<Integer, ArrayList<Integer>> getQueryPositions( Directory indexDir, String query ) throws IOException {
		IndexReader ir = DirectoryReader.open(indexDir);
	    
	    Fields fields =  MultiFields.getFields(ir);
	    
	    LinkedHashMap<Integer, ArrayList<Integer>> res = new LinkedHashMap<Integer, ArrayList<Integer>>();
	    for ( String field : fields ) {
	    
	    	Terms terms = fields.terms(field);
	        TermsEnum termsEnum = terms.iterator(null);
	        
	        BytesRef text;
	       
	       
	        while( ( text = termsEnum.next() ) != null) {
	        	
	        	if ( text.utf8ToString().equals( query) ) {
	        		
	        		DocsAndPositionsEnum docPosEnum = termsEnum.docsAndPositions(null, null);
		        	while ( docPosEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS ) {
		        	
		        		int freq = docPosEnum.freq();
			        	ArrayList<Integer> positions = new ArrayList<Integer>();
			        	for(int i = 0; i < freq; i++) {
			                int position = docPosEnum.nextPosition();
			               
			                positions.add( position );
			            }
			        	
			            res.put( docPosEnum.docID() , positions );
			        
		        	}   	
	        	
	        	} 
	        }
	    }
	    
 	    return res;
	}
	
	
	
	
	/*
	 *  Find all neighbours in specific document with specific positions
	 *  The neighbours are terms, that are located nearby current position - back and forward
	 *  Push term in result if it belongs to toCompare ArrayList (these are the result ten terms surrounding email more frequent)
	 *  Return LinkerHashMap <uniqueId, term>. Unique Id is used for make similar terms be different in a list
	 *  
	 *  @param index directory
	 *  @param document ID
	 *  @param list of positions
	 *  @param checklist for filtering
	 */
	
	public static LinkedHashMap<String, String> getNeighboursByDocPos( Directory indexDir,
														Integer docId, 
														ArrayList<Integer> pos, 
														ArrayList<String> toCompare ) throws IOException {
		IndexReader ir = DirectoryReader.open(indexDir);
	    
	    Fields fields =  MultiFields.getFields(ir);
	       
	    LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();
	    
	    for ( String field : fields ) {
	        Terms terms = fields.terms(field);
	        TermsEnum termsEnum = terms.iterator(null);
	        
	        BytesRef text;
	        
	        while ( (text = termsEnum.next()) != null ) {
	        	
	        	
	        	DocsAndPositionsEnum docPosEnum = termsEnum.docsAndPositions( null, null );
		        
	        	while ( docPosEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS ) {
	        	
		        			int freq = docPosEnum.freq();
		        			if (docPosEnum.docID() == docId ) {		
			                	for( int i = 0; i < freq; i++ ) {
					                int position = docPosEnum.nextPosition();
					                for (Integer p: pos) {
					                	if ( position == p+1 ) {
					                		if ( toCompare.contains( text.utf8ToString() ) ) {
					                			res.put( UUID.randomUUID().toString(), getTermByDocPos(indexDir, docId, p+1) );
					                		}
					                	}
					                	if ( position == p-1 ) {
					                		if ( toCompare.contains( text.utf8ToString() ) ) {
					                			res.put( UUID.randomUUID().toString(), getTermByDocPos(indexDir, docId, p-1) );
					                		}
					                	}
					                }
			                	}
		                }           
		        }
	        }
	    }
	    return res;
	}
	
	
	
	
	
	
	
	/*
	 * Get Term in document by position
	 * Find term in index by docId and position
	 * 
	 * @param index directory
	 * @param document ID
	 * @param term position
	 */
	
	public static String getTermByDocPos(Directory indexDir, Integer docId, Integer pos) throws IOException {
		IndexReader ir = DirectoryReader.open(indexDir);
	    
	    Fields fields =  MultiFields.getFields(ir);

	    for ( String field : fields ) {
	        Terms terms = fields.terms(field);
	        TermsEnum termsEnum = terms.iterator(null);
	        
	        BytesRef text;
	        LinkedHashMap<String, Integer> textFreq = new LinkedHashMap<String, Integer>();
	        while ( (text = termsEnum.next()) != null ) {
	        	
	        	DocsAndPositionsEnum docPosEnum = termsEnum.docsAndPositions(null, null);
		        
	        	while ( docPosEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS ) {
	        		if ( docPosEnum.docID() == docId ) {
		        		int freq = docPosEnum.freq();
			            for( int i = 0; i < freq; i++ ) {
			                int position = docPosEnum.nextPosition();
			                if ( position == pos ) {
			                	return text.utf8ToString();
			                }
		        		}
	        		}    
		        }
	       
	        }
	    }
	    return null;
	}
	
	
	
	
	
	
	
	/*
	 * For each term from target calculate the most frequent neighbour
	 * 
	 * @param index directory
	 * @param list of terms
	 */
	
	public static LinkedHashMap<String, Integer> getBestAverageNeighbourResult( Directory dir, LinkedHashMap<String, Integer> target ) throws IOException {
		ArrayList<String> toCompare = new ArrayList<String>();
		Iterator it = target.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			String word = (String) pair.getKey();
			toCompare.add(word);
			it.remove();
		
		}
		
		LinkedHashMap<String, Integer> res = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> tempres = new LinkedHashMap<String, Integer>();
		int ii = 0;
		for (String s: toCompare) {	
				
			LinkedHashMap<Integer, ArrayList<Integer>> temp = IndexSearcher.getQueryPositions(dir, s);
			
			LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
			
			Integer key = 0;
			ArrayList<Integer> tab = new ArrayList<Integer>();
			for ( Map.Entry<Integer, ArrayList<Integer>> entry : temp.entrySet() ) {
			    key = entry.getKey();
			    tab = entry.getValue();
			    LinkedHashMap<String, String> concat = IndexSearcher.getNeighboursByDocPos(dir, key, tab, toCompare);
			
			    for ( Map.Entry<String, String> e : concat.entrySet()) {
			    	result.put( e.getKey(), e.getValue() );
			    	
			    }
			    
			}
			
			tempres = DataHandler.sortHashMapByValues( DataHandler.removeIdenticTermsAndCalculateFrequency( result ) );
			
			if (!tempres.isEmpty()) {			
				res.put( ii+tempres.entrySet().iterator().next().getKey(), tempres.entrySet().iterator().next().getValue() );
			} else {
				res.put(ii+"null", null);
			}
			ii++;
		}
		return res;
	}
	
	
	
	
	
	
	
	
	
	/*
	 * Get the search result
	 * Return the pair, which is more often met in documents nearby
	 * Likely to be final result as name and surname
	 * 
	 *  @param list of terms (transformated to ArrayList<String>, Integer is not used)
	 *  @param list of neighbours and frequency
	 */
	
	
	public static String getResult(LinkedHashMap<String, Integer> terms, LinkedHashMap<String, Integer> neighbours) {
		 ArrayList<Integer> firstTermFreq = new ArrayList<Integer>();
		 for ( Map.Entry<String, Integer> term : terms.entrySet()) {
			 if (term.getValue() == null) {		
				 firstTermFreq.add(0);
			 } else { 
				 firstTermFreq.add(term.getValue());
			 }
		 }
		 
		 
		 ArrayList<Integer> secondTermFreq = new ArrayList<Integer>();
		 for ( Map.Entry<String, Integer> term : neighbours.entrySet()) {
			 if (term.getValue() == null) {
				 secondTermFreq.add(0);
			 } else { 
				 secondTermFreq.add(term.getValue());
			 }
		 }
		 
		 
		 ArrayList<String> t1 = new ArrayList<String>();
		 for ( Map.Entry<String, Integer> term : terms.entrySet()) {
			 t1.add(term.getKey());
		 }
		 
		 ArrayList<String> t2 = new ArrayList<String>();
		 for ( Map.Entry<String, Integer> term : neighbours.entrySet()) {
			 t2.add(term.getKey());
		 }
		 
		 int max = 0;
		 String res1 = "";
		 String res2 = "";
		 for (int i = 0; i < firstTermFreq.size(); i++) {
			 int f = firstTermFreq.get(i);
			 int s = secondTermFreq.get(i);
			 
			 int avg = (f + s) / 2 - Math.abs(f-s);
			 
			 if (avg > max) {
				 max = avg;
				 res1 = t1.get(i);
				 res2 = t2.get(i).substring(1);
			 }
		 }
		 
		
		 
		 return res1 + " " + res2;
	}
}
