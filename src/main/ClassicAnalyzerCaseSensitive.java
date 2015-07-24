package main;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.analyzer.MorphologyFilter;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

public class ClassicAnalyzerCaseSensitive 
	extends StopwordAnalyzerBase
	{
	  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
	  private int maxTokenLength = 255;
	  public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	  
	  public ClassicAnalyzerCaseSensitive(CharArraySet stopWords)
	  {
	    super(stopWords);
	  }
	  
	  public ClassicAnalyzerCaseSensitive()
	  {
	    this(STOP_WORDS_SET);
	  }
	  
	  public ClassicAnalyzerCaseSensitive(Reader stopwords)
	    throws IOException
	  {
	    this(loadStopwordSet(stopwords));
	  }
	  
	  public void setMaxTokenLength(int length)
	  {
	    this.maxTokenLength = length;
	  }
	  
	  public int getMaxTokenLength()
	  {
	    return this.maxTokenLength;
	  }
	  
	  protected Analyzer.TokenStreamComponents createComponents(String fieldName)
	  {
	    final ClassicTokenizer src = new ClassicTokenizer();
	    src.setMaxTokenLength(this.maxTokenLength);
	    TokenStream tok = new ClassicFilter(src);
	    //tok = new LowerCaseFilter(tok);
	    tok = new StopFilter(tok, this.stopwords);
	    
	    
	    /*LuceneMorphology luceneMorph;
		try {
			luceneMorph = new RussianLuceneMorphology();
			tok = new MorphologyFilter(tok, luceneMorph);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	   
		tok = new PorterStemFilter(tok);
		
	    return new Analyzer.TokenStreamComponents(src, tok )
	    {
	      protected void setReader(Reader reader)
	        throws IOException
	      {
	        src.setMaxTokenLength(ClassicAnalyzerCaseSensitive.this.maxTokenLength);
	        super.setReader(reader);
	      }
	    };
	  }
}
