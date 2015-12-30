/**
 * 
 */
package kmeans;

/**
 * @author Sagar6903
 *
 */
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.Fitzpatrick;

/**
 * Example application that load raw JSON tweets and 
 * cleans it
 * 
 */
public final class LiveTweetsCleaner {

	private static final String STOPWORDS_FILE_PATH = "data/stoplist.txt";
	static ArrayList<String> lstOfStopWords;
	static ArrayList<String> fitzpatrickList;
	static ArrayList<String> emojis;

	/***
	 * 
	 * @throws IOException 
	 * @throws TwitterException 
	 * @throws InterruptedException
	 */
	public static Point Start(int tweetId, String tweetText) throws IOException, TwitterException {
		
			BufferedReader br = new BufferedReader(new FileReader(STOPWORDS_FILE_PATH));
			lstOfStopWords = new ArrayList<String>();
			String line = "";
			while ((line = br.readLine()) != null) {
				lstOfStopWords.add(line);
			}
			br.close();

			// Read Emojis
			// Add all fitzpatrick modifiers
			fitzpatrickList = new ArrayList<String>();
			for (Fitzpatrick fitzpatrick : Fitzpatrick.values()) {
				fitzpatrickList.add(fitzpatrick.unicode);
			}
			// remove all emojis
			emojis = new ArrayList<String>();
			for (Emoji emoji : EmojiManager.getAll()) {
				emojis.add(emoji.getUnicode());
			}

			Status status = TwitterObjectFactory.createStatus(tweetText);
			String text = status.getText().replaceAll("(\\r|\\n|\\t)", " ");
			return process(tweetId, text);
		} 

	private static String refineTweet(String tweet) {
		// Remove apostrophe followed by any text
		tweet = tweet.replaceAll("'.+?\\s", " ");
		// Remove Links
		tweet = tweet.replaceAll("(http|https|ftp)://\\S+\\s?", "");
		// Remove all the punctuation except #
		tweet = tweet.replaceAll("(?!#)(?!@)\\p{Punct}", "");
		// Remove all twitter handle that occurs inside the tweet
		tweet = tweet.replaceAll("@[a-zA-Z0-9_]*", "");
		// For Mac - Remove some non-ascii characters
		tweet = tweet.replaceAll("ï¿½", "").replaceAll("ï¿½", "").replaceAll("ï¿½", "").replaceAll("ï¿½", ""); 
		
		// For Window OS Remove non-ascii characters
		tweet = tweet.replaceAll("”","").replaceAll("“", "").replaceAll("…","");
		tweet = tweet.replaceAll("[^\\u0000-\\uFFFF]", "");
		tweet = tweet.replaceAll("[^\\x00-\\x7F]", "");
		return tweet.toLowerCase().trim();
	}

	private static String removeStopwords(String tweet) throws IOException {
		ArrayList<String> words = new ArrayList<String>();
		words.addAll(Arrays.asList(tweet.split("\\s+")));

		// Remove Emojis
		words.removeAll(fitzpatrickList);
		words.removeAll(emojis);

		words.removeAll(lstOfStopWords);
		return words.toString().replaceAll(",", "").replaceAll("\\[", "").replaceAll("\\]", "");
	}

	public static Point process(int tweetId, String tweet) throws IOException {
		tweet = refineTweet(tweet);
		tweet = removeStopwords(tweet);
		Point incomingTweet = new Point(tweetId, tweet);
		return incomingTweet;
	}
}