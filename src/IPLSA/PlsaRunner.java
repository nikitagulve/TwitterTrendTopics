package IPLSA;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PlsaRunner {
	
	public static void main(String args[]) throws NumberFormatException, IOException {
		Utility utility = new Utility();
		if(args.length < 2) {
			System.out.println("Usage:PlsaRunner <folder> <NumFiles>");
			System.exit(1);
		}
		
		String folder = args[0];
		int totalFiles = Integer.parseInt(args[1]);
		int numClusters = 4;
		Plsa myPlsa = new Plsa(numClusters);
		
		// These are the tweets over approx. half hour. They trained using normal PLSA
		List<Tweet> tweets = utility.readTweets(folder+"/1.txt");
		tweets.addAll(utility.readTweets(folder+"/2.txt"));
		tweets.addAll(utility.readTweets(folder+"/3.txt"));
		tweets.addAll(utility.readTweets(folder+"/4.txt"));
		tweets.addAll(utility.readTweets(folder+"/5.txt"));
		myPlsa.train(tweets, 100);
		List<List<Double>> beta = myPlsa.getTopicWordPros();
		List<List<Double>> theta = myPlsa.getDocTopics();
		myPlsa.getTopKBeta(beta);
		
		// Processes new data which is collected for every 15 mins. 
		System.out.println("((((((( Cluster popular is " + myPlsa.getDocumentsPerTopic(theta) + ")))))))");
		
		for(int i=6;i<=totalFiles;i++) {
			System.out.println("*********************Iteration " + (i-4) + " started*********************");
			tweets = utility.readTweets(folder + "/" + i + ".txt");
			myPlsa.addNewTweets(tweets, 100);
			beta = myPlsa.getTopicWordPros();
			
			theta = myPlsa.getDocTopics();
			myPlsa.getTopKBeta(beta);
			System.out.println("((((((( Cluster popular is " + myPlsa.getDocumentsPerTopic(theta) + ")))))))");
			System.out.println("*********************Iteration " + (i-4) + " completed*********************");
		}
		
		Map<Integer, List<Integer>> clusterTimeCount = myPlsa.getClusterTimeCount();
		
		CreateGraph cg = new CreateGraph();
		cg.plotGraph(clusterTimeCount);
	}

}
