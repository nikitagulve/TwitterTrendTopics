/**
 * 
 */
package kmeans;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Sagar6903
 *
 */
public class DistanceHelper {

	/**
	 * Compute Jaccard's Coefficient of Similarity
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double computeJaccardsCoefficient(Point p1, Point p2){
		Collection<String> tweet1 = new TreeSet<String>(Arrays.asList(p1.getTweet().split(" ")));
		
		Collection<String> tweet2 = new TreeSet<String>(Arrays.asList(p2.getTweet().split(" ")));
		
		Collection<String> intersectionOfTweets = new TreeSet<String>(tweet1);
		intersectionOfTweets.retainAll(tweet2);
		
		Collection<String> unionOfTweets = new TreeSet<String>(tweet1);
		unionOfTweets.addAll(tweet2);
		
		double jaccardsCoefficient = (double)intersectionOfTweets.size() / (double)unionOfTweets.size();
		return jaccardsCoefficient;
	}
	
	/**
	 * Compute MinHash Similarity 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double computeMinHash(Point p1, Point p2) {
		Set<String> set1 = new HashSet<String>(Arrays.asList(p1.getTweet().split(" ")));
		Set<String> set2 = new HashSet<String>(Arrays.asList(p2.getTweet().split(" ")));
		MinHash<String> minHash = new MinHash<String>(set1.size() + set2.size());
		return minHash.similarity(set1, set2);
	}
	
	/**
	 * Compute Cosine Similarity
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double computeCosineSimilarity(Point p1, Point p2) {
		DecimalFormat decimalFormat = new DecimalFormat("#.###");
		decimalFormat.setRoundingMode(RoundingMode.CEILING);
		LinkedHashMap<String, Integer> wordCount = new LinkedHashMap<String, Integer>();
		
		Collection<String> tweet1 = new ArrayList<String>(Arrays.asList(p1.getTweet().split(" ")));
		Collection<String> tweet2 = new ArrayList<String>(Arrays.asList(p2.getTweet().split(" ")));
		
		Collection<String> unionOfTweets = new TreeSet<String>(tweet1);
		unionOfTweets.addAll(tweet2);
		
		for(String word : unionOfTweets)
			wordCount.put(word, 0);
		
		for (String word : tweet1) {
			int count = wordCount.get(word);
			wordCount.put(word, count + 1);
		}
		
		Integer[] tweet1Vector = wordCount.values().toArray(new Integer[wordCount.size()]);
		for(Map.Entry<String, Integer> entry : wordCount.entrySet())
			wordCount.put(entry.getKey(), 0);
		
		for (String word : tweet2) {
			int count = wordCount.get(word);
			wordCount.put(word, count + 1);
		}
		
		Integer[] tweet2Vector = wordCount.values().toArray(new Integer[wordCount.size()]);
		
		double numerator = 0.0;
		double magnitudeVector1 = 0.0;
		double magnitudeVector2 = 0.0;
		for(int index = 0; index < tweet1Vector.length; index++){
			numerator += (tweet1Vector[index] * tweet2Vector[index]);
			magnitudeVector1 += Math.pow(tweet1Vector[index], 2);
			magnitudeVector2 += Math.pow(tweet2Vector[index], 2);
		}
		
		double denominator = Math.sqrt(magnitudeVector1) * Math.sqrt(magnitudeVector2);
		return Double.parseDouble(decimalFormat.format(numerator / denominator));
	}
}
