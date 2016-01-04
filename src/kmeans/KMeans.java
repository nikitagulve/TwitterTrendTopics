/**
 * 
 */
package kmeans;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import kmeans.Cluster;
import kmeans.DistanceHelper;
import kmeans.IOUtility;
import kmeans.Point;

/**
 * @author Sagar6903
 * 
 */
public class KMeans {

	private int numberOfClusters;
	private boolean isLiveStreamRunner = false;
	private boolean isStaticRunner = false;
	private static final double threshold = 0.3;
	private ArrayList<Cluster> topics = new ArrayList<Cluster>();
	ArrayList<Cluster> iterativeTopics = null;
	private int clusterId = 0;
	int liveTweetId = 0;
	boolean isFirst = true;
	HashMap<Integer, Double> clusterSimilarityAvgMap = new HashMap<Integer, Double>();

	public KMeans(int numberOfClusters,boolean isStaticRunner, boolean isLiveStreamRunner) {
		this.numberOfClusters = numberOfClusters;
		this.isStaticRunner = isStaticRunner;
		this.isLiveStreamRunner = isLiveStreamRunner;
	}
	
	/**
	 * Entry point for KMeans
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void start() throws IOException, InterruptedException {
		if(!isStaticRunner)
			run(new ArrayList<Point>());
		else {
			@SuppressWarnings("unused")
			int totalPoints = 0;
			List<Point> points = new ArrayList<>();
	        String line;
	        int tweetIdCounter = 1;
	        // Read the files from KMeansData\\Processed Folder. Modify for loop start and end as per your need
			for (int file = 1; file < 25; file++) {
				// For Evaluation Tweets : Uncomment below line and comment for loop
				//BufferedReader br = new BufferedReader(new FileReader("TestTweets\\testTweets.txt"));
				BufferedReader br = new BufferedReader(new FileReader("KMeansData\\ProcessedData\\" + file + ".txt"));
				while ((line = br.readLine()) != null) {
					if (!line.isEmpty()) {
						Point p = new Point(tweetIdCounter++, line);
						points.add(p);
					}
				}
				br.close();
				totalPoints += points.size();
				run(points);
				points.clear();
			}
			// Print Evaluation Tweets Result in to a file.
			//IOUtility.printOutputToFile(topics, "KMeansResults\\Cosine\\TopicsEvaluation.txt", EvaluationMeasures.computePurity(topics, totalPoints), EvaluationMeasures.computeNormalizedMutualInformation(topics, totalPoints));

			// Print Static Tweets Clustering in a file. Also print Top 3 densed cluster to a file
			IOUtility.printOutputToFile(topics, "KMeansResults\\Jaccards\\TrendingTopics1-24Hr.txt", null, null);
			IOUtility.printTop3DensedCluster(topics, "KMeansResults\\Jaccards\\Top3-1-24Hr.txt");
			//IOUtility.printOutputToFile(topics, "KMeansResults\\Cosine\\TrendingTopics1-24Hr.txt", null, null);
			//IOUtility.printTop3DensedCluster(topics, "KMeansResults\\Cosine\\Top3-1-24Hr.txt");
			//IOUtility.printOutputToFile(topics, "KMeansResults\\MinHash\\TrendingTopics1-24Hr.txt", null, null);
			//IOUtility.printTop3DensedCluster(topics, "KMeansResults\\MinHash\\Top3-1-24Hr.txt");
		}
	}

	/**
	 * Run Modified KMeans Algorithm
	 * @param points
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void run(List<Point> points) throws IOException, InterruptedException {
		
		iterativeTopics = new ArrayList<Cluster>(topics);
		topics.clear();
		clusterId = 0;		
		for (Point newTweet : points) {
			if (topics.size() == 0) {
				// Create New Cluster
				Cluster newTopic = new Cluster(++clusterId);
				newTopic.addPointToCluster(newTweet);
				newTweet.setAssignment(newTopic.getClusterId());
				topics.add(newTopic);
			} else {
				boolean isAssigned = false;

				for (Cluster topic : topics) {
					if (checkForTweetsSimilarity(newTweet, topic.getClusterPoints(), topic.getClusterId())) {
						// Assign the point (newTweet) to the cluster.
						topic.addPointToCluster(newTweet);
						// Set Assignment for newTweet to identify which cluster it belongs to
						newTweet.setAssignment(topic.getClusterId());
						isAssigned = true;
						break;
					}
				}

				if (!isAssigned) {
					if (clusterId == numberOfClusters) {
						// Since numberOfCluster = K, we try to assign the tweet to 
						// the best similar cluster ignoring the threshold value
						int Id = getClosestClusterWithMaxSimilarity();
						newTweet.setAssignment(Id);
						topics.get(Id - 1).addPointToCluster(newTweet);
					} else {
						// Create New Cluster
						Cluster newTopic = new Cluster(++clusterId); 
						newTopic.addPointToCluster(newTweet);
						newTweet.setAssignment(newTopic.getClusterId());
						topics.add(newTopic);
					}
				}
			}
		} // No more Tweets to Cluster

		// Check For Convergence by computing Cluster Mean only in case of Live Tweets Clustering
		if (topics.size() != 0 && !HasConverged())
			// update cluster points here
			run(updatePoints()); 
		else if (isLiveStreamRunner) {
			// Print Live Tweets Clustering to a file
			IOUtility.printOutputToFile(topics, "KMeansResults\\trendingTopicsUsingLiveStreaming.txt", null, null);
			System.out.println("Twitter Streaming start...");
			if (isFirst)
				startStreaming();
			isFirst = false;
		}
	}

	/**
	 * Compute whether two tweets are similar or not
	 * You can specify which similarity measure to use
	 * There are 3 of them : Jaccards, MinHash, Cosine 
	 * @param incomingTweet
	 * @param clusteredTweets
	 * @param clusterId
	 * @return
	 */
	private boolean checkForTweetsSimilarity(Point incomingTweet, List<Point> clusteredTweets, int clusterId) {
		boolean IsSimilar = true;
		double total = 0.0;
		for (Point tweet : clusteredTweets) {
			double similarity = DistanceHelper.computeJaccardsCoefficient(incomingTweet, tweet);
			//double similarity = DistanceHelper.computeCosineSimilarity(incomingTweet, tweet);
			//double similarity = DistanceHelper.computeMinHash(incomingTweet,tweet);
			total += similarity;
			if (similarity >= threshold)
				continue;
			else
				IsSimilar = false;
		}
		clusterSimilarityAvgMap.put(clusterId, total / (double) clusteredTweets.size());
		if (!IsSimilar)
			return false;
		return true;
	}

	/**
	 * Determine the cluster to which the incoming tweet is 
	 * closest of all.
	 * @return
	 */
	private int getClosestClusterWithMaxSimilarity() {
		int keyOfMaxValue = Collections.max(clusterSimilarityAvgMap.entrySet(),
				new Comparator<Entry<Integer, Double>>() {
					@Override
					public int compare(Entry<Integer, Double> entry1, Entry<Integer, Double> entry2) {
						return entry1.getValue() > entry2.getValue() ? 1 : -1;
					}
				}).getKey();

		return keyOfMaxValue;
	}

	/**
	 * Check for convergence for KMeans
	 * @return
	 */
	private boolean HasConverged() {
		if ((iterativeTopics.size() != 0 || topics.size() != 0) && iterativeTopics.size() == topics.size()){
			for(int index = 0; index < topics.size(); index++) {
				if (AreTopicMeanSame(iterativeTopics.get(index), topics.get(index)))
						continue;
				else
					return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Compute Cluster Mean and check whether two clusters are same or not
	 * @param c1
	 * @param c2
	 * @return
	 */
	private boolean AreTopicMeanSame(Cluster c1, Cluster c2){
		if(Math.abs(computeMean(c1.getClusterPoints()) - computeMean(c2.getClusterPoints())) <= 0.00001)
			return true;
		return false;
	}

	/***
	 * Compute Mean of the cluster by creating 
	 * word distribution of all the tweets present in that
	 * cluster.
	 * @param tweets
	 * @return
	 */
	private double computeMean(List<Point> tweets){
		HashMap<String, Integer> wordDictionary = new HashMap<String, Integer>();
		int total = 0;
		for(Point p : tweets){
			String[] words = p.getTweet().split(" ");
			
			for(String word : words){
				if(wordDictionary.containsKey(word)){
					int wordCount = wordDictionary.get(word);
					wordDictionary.put(word, wordCount + 1);
				} else
					wordDictionary.put(word, 1);
			}
		}
		
		for(int value : wordDictionary.values()){
			total += value;
		}
		return (double) total / (double) tweets.size();
	}
	
	/**
	 * In case, if convergence is not met, we update the points
	 * Eg: If in live streaming run1 : Let say numberOfTweets : 500
	 *        in live streaming run2 : Let say numberOfTweets : 450
	 *        While running Kmeans, if convergence is not met, then
	 *        KMeans is ran again on '950' number of Tweets in total
	 * @return
	 */
	private List<Point> updatePoints() {
		HashSet<Point> mergedSet = new HashSet<Point>();
		
		if (iterativeTopics.size() != 0) {
			for(Cluster topic : iterativeTopics){
				mergedSet.addAll(topic.getClusterPoints());
			}
		}
		
		for (Cluster topic : topics) {
			mergedSet.addAll(topic.getClusterPoints());
		}
		ArrayList<Point> mergedPoints = new ArrayList<Point>(mergedSet);
		Collections.sort(mergedPoints, new Comparator<Point>() {
			@Override
			public int compare(Point p1, Point p2) {
				return p1.getTweetId() - p2.getTweetId();
			}
		});

		return mergedPoints;
	}
	
	
	/**
	 * Perform Live Streaming for online Kmeans Clustering
	 */
	private void startStreaming() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("KeeH2HM1s67NNvWWRLmk1MFr8");
		cb.setOAuthConsumerSecret("NUbIA4JHOmCrXShOcA6X9vwDGDJclLuKjECcc0IQjcfXBVswv8");
		cb.setOAuthAccessToken("45809142-ZbYLRZhbjUUb3F6St4CfP67H0rmexJTQT6NfLfG2X");
		cb.setOAuthAccessTokenSecret("GUAHc0DXqkA38qLGGEymUbXKj9T8kchvm7igK2Yi8DCeC");
		cb.setJSONStoreEnabled(true);
		
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {

			@Override
			public void onException(Exception arg0) {
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
			}
			
			List<Point> streamingPoints = new ArrayList<Point>();
			@Override
			public void onStatus(Status status) {
				try {
					String tweet = TwitterObjectFactory.getRawJSON(status);
					Point cleanedTweet = LiveTweetsCleaner.Start(++liveTweetId, tweet);
					if (!cleanedTweet.getTweet().isEmpty()){
						streamingPoints.add(cleanedTweet);
						System.out.println("Incoming Tweet Id : " + liveTweetId);
					}
					if(streamingPoints.size() % 500 == 0) {
						run(streamingPoints);
						streamingPoints.clear();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub

			}
		};
		FilterQuery fq = new FilterQuery();
		double[][] locations = { { -71.0, 42.0 }, { -70.0, 43.0 } };
		fq.locations(locations);
		fq.language("en");
		twitterStream.addListener(listener);
		twitterStream.filter(fq);
	}
}
