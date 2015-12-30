package IPLSA;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Class to run on test data and evaluate quality of algorithm using measures like
 * purity and NMI
 * 
 * @author yogi
 *
 */
public class TestDriver {
	
	private static final String CONF_LABEL_PATH = "TestTweets/testTweets1";
	
	public static void main(String args[]) throws NumberFormatException, IOException {
		Utility utility = new Utility();
		Plsa myPlsa = new Plsa(3);
		List<Tweet> tweets = utility.readTestTweets("TestTweets/1.txt");
		myPlsa.train(tweets, 100);
		
		List<List<Double>> beta = myPlsa.getTopicWordPros();		
		List<List<Double>> theta = myPlsa.getDocTopics();
		myPlsa.getTopKBeta(beta);		
		
		System.out.println("((((((( Cluster popular is " + myPlsa.getDocumentsPerTopic(theta) + ")))))))");
		
		for(int i=2;i<=3;i++) {
			System.out.println("*********************Iteration " + i + " started*********************");
			tweets = utility.readTestTweets("TestTweets/" + i + ".txt");
			myPlsa.addNewTweets(tweets, 100);
			beta = myPlsa.getTopicWordPros();			
			theta = myPlsa.getDocTopics();
			myPlsa.getTopKBeta(beta);
			System.out.println("((((((( Cluster popular is " + myPlsa.getDocumentsPerTopic(theta) + ")))))))");
			System.out.println("*********************Iteration " + i + " completed*********************");
		}
		
		myPlsa.readConfLabel(CONF_LABEL_PATH);
		myPlsa.evaluation(theta);
		
		Map<Integer, List<Integer>> clusterTimeCount = myPlsa.getClusterTimeCount();
		
		CreateGraph cg = new CreateGraph();
		cg.plotGraph(clusterTimeCount);
	}

}
