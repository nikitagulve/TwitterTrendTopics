package IPLSA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * utility class
 * @author yogi
 *
 */
public class Utility {
	
	/**
	 * Class to read Tweets from the file
	 * @param filename
	 * @return
	 */
	public List<Tweet> readTweets(String filename) {
		File file = new File(filename);
		BufferedReader br = null;
		List<Tweet> tweets = new ArrayList<Tweet>();

		try {
			br = new BufferedReader(new FileReader(file));
			String text;
			String[] lineSplit;
			while ((text = br.readLine()) != null) {
				text.trim();
				if (text != null || !text.isEmpty() ) {
					lineSplit = text.split(" ");
					tweets.add(new Tweet(Arrays.asList(lineSplit)));
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return tweets;
	}
	
	/**
	 * Class to read the test tweets for evaluation
	 * @param filename
	 * @return
	 */
	public List<Tweet> readTestTweets(String filename) {
		File file = new File(filename);
		BufferedReader br = null;
		List<Tweet> tweets = new ArrayList<Tweet>();

		try {
			br = new BufferedReader(new FileReader(file));
			String text;
			String[] lineSplit;
			while ((text = br.readLine()) != null) {
				if (text != null || !text.isEmpty()) {
					lineSplit = text.trim().split("\t")[0].split(" ");
					tweets.add(new Tweet(Arrays.asList(lineSplit)));
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return tweets;
	}

	/**
	 * Function to read all the words of the tweets
	 * @param docs
	 * @return
	 */
	List<String> getAllWords(List<Tweet> docs) {
		Set<String> uniqWords = new HashSet<String>();
		for (Tweet doc : docs) {
			for (String word : doc.getWords()) {
				if (!uniqWords.contains(word)) {
					uniqWords.add(word);
				}
			}
		}

		return new LinkedList<String>(uniqWords);
	}
	
	/**
	 * Merges two lists by avoiding duplciates
	 * @param all
	 * @param curr
	 * @return
	 */
	List<String> mergeLists(List<String> all, List<String> curr) {
		for (String word : curr) {
			if (!all.contains(word)) {
				all.add(word);
			}
		}
	return new LinkedList<String>(all);
	}

	/**
	 * Function to update p(w|z) matrix
	 * @param prob
	 * @param newWords
	 */
	public void updateTopicTermProb(List<Double> prob, int newWords) {
		if (newWords < 1) {
			return;
		}
		
		// Factor to normalize previous probabilties
		double factor = 0.8;
		double factor_new = 1 - factor;
		

		double total = 0;
		
		for(int i=0;i<prob.size();i++) {
			prob.set(i, prob.get(i)*factor);
		}
		
		Random r = new Random();
		for (int i = 0; i < newWords; i++) {
			// avoid zero
			double p = r.nextDouble();
			while(p == 0.0) {
				p = r.nextDouble();
			}
			prob.add(p);

			total += p;
		}

		// normalize
		for (int i = prob.size()-newWords; i < prob.size(); i++) {
			prob.set(i, factor_new*(prob.get(i)/total));
		}
		
	}

	/**
	 * Calculate purity of clusters
	 * @param wkcj
	 * @param tweets
	 * @return
	 */
	public double calculatePurity(double[][] wkcj,int tweets) {
		double sum =0;
		for(int k=0;k<wkcj[0].length;k++) {
			double max = 0;
			for(int j=0;j<wkcj.length;j++) {
				max = Math.max(wkcj[j][k],max);
			}
			sum+= max;
		}
		
		return sum/tweets;
	}

	/**
	 * Calculates NMI for clusters
	 * 
	 * @param wkcj
	 * @param docSize
	 * @param topicNum
	 * @param wk
	 * @param cj
	 * @return
	 */
	public double calculateNMI(double[][] wkcj, int docSize,int topicNum, double[] wk, double[] cj) {
		double iwc = 0;
		for (int k = 0; k < topicNum; k++) {
			for (int j = 0; j < topicNum; j++) {
				if ((docSize * wkcj[k][j]) / (wk[k] * cj[j]) == 0) {
					continue;
				}
				double log = Math.log((docSize * wkcj[k][j]) / (wk[k] * cj[j]));
				iwc += (wkcj[k][j] * log) / docSize;
			}
		}

		double hw = 0;
		for (int k = 0; k < topicNum; k++) {
			if (wk[k] == 0)
				continue;
			double log = Math.log(wk[k]) - Math.log(docSize);
			hw -= (wk[k] * log) / docSize;
		}

		double hc = 0;
		for (int k = 0; k < cj.length; k++) {
			if (cj[k] == 0)
				continue;
			double log = Math.log(cj[k]) - Math.log(docSize);
			hc -= (cj[k] * log) / docSize;
		}
		return iwc / Math.sqrt(hw * hc);
		
	}
}
