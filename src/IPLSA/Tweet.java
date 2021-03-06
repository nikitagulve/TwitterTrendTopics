package IPLSA;

import java.util.List;
/**
 * 
 * Represents a Tweet
 * @author niki
 *
 */
public class Tweet {

    private List<String> words;

    public boolean containsWord(String word) {
    	return words.contains(word);
    }
    
    public Tweet(List<String> words) {
        this.words = words;        
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }   

}
