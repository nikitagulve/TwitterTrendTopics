package Preprocess;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.Fitzpatrick;

/**
 * Example application that load raw JSON forms from statuses/ directory and
 * dump status texts.
 * 
 */
public final class LoadRawJSON {

	private static final String STOPWORDS_FILE_PATH = "data/stoplist.txt";
	static ArrayList<String> lstOfStopWords;
	static ArrayList<String> fitzpatrickList;
	static ArrayList<String> emojis;
	static BufferedWriter stext;
	static BufferedWriter oText;

	/**
	 * Usage: java twitter4j.examples.json.LoadRawJSON
	 * 
	 * @param args
	 *            String[]
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		try {
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

			//stext = new BufferedWriter(new FileWriter("EachHourProcessed/out.txt"));
			//oText = new BufferedWriter(new FileWriter("EachHourProcessed/Cleanout.txt"));

			for (int i = 1; i < 59; i++) {
				oText = new BufferedWriter(new FileWriter("EachHourProcessed/" + i + ".txt"));
				System.out.println(i);
				List<String> rawJSONs = readAllLines(new File("EachHour/" + i
						+ ".txt"));
				for (String rawJSON : rawJSONs) {
					Status status = TwitterObjectFactory.createStatus(rawJSON);
					String text = status.getText().replaceAll("(\\r|\\n|\\t)", " ");
					//stext.write(text);
					//stext.write("\n");
					process(text);
				}
				oText.close();
			}
			System.exit(0);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("Failed to store tweets: " + ioe.getMessage());
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		}
	}


	private static List<String> readAllLines(File fileName) throws IOException {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(isr);
			String line = null;
			List<String> rawJSONs = new ArrayList<String>();
			line = br.readLine();
			while ((line = br.readLine()) != null)
				rawJSONs.add(line);
			return rawJSONs;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ignore) {
				}
			}
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException ignore) {
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	
    private static String refineTweet(String tweet) {
    	// Remove 'xx
    	tweet = tweet.replaceAll("'.+?\\s", " "); 
    	
    	// Remove Links
		tweet = tweet.replaceAll("(https|http|ftp)://\\S+\\s?", ""); 
		tweet = tweet.replaceAll("(https|http|ftp)", "");
		
		// Remove all the punctuation except #
		tweet = tweet.replaceAll("(?!#)(?!@)\\p{Punct}", "");
		
		// Remove all twitter handle that occurs inside the tweet
		tweet = tweet.replaceAll("@[a-zA-Z0-9_]*", "");
		
		// Remove some non-ascii characters
		tweet = tweet.replaceAll("ï¿½", "").replaceAll("ï¿½", "")
				.replaceAll("ï¿½", "").replaceAll("ï¿½", ""); 
		
		// For Window OS 
		tweet = tweet.replaceAll("”","").replaceAll("“", "").replaceAll("…","");
		tweet = tweet.replaceAll("[^\\u0000-\\uFFFF]", "");

		// Remove all Non-Ascii Characters
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
		return words.toString().replaceAll(",", "").replaceAll("\\[", "")
				.replaceAll("\\]", "");
	}

	public static void process(String tweet) {
		try {
			tweet = refineTweet(tweet);
			tweet = removeStopwords(tweet);
			oText.write(tweet);
			oText.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}