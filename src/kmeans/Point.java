/**
 * 
 */
package kmeans;

/**
 * @author Sagar6903
 * 
 */
public class Point {

	private int tweetId;
	private String tweet;
	private int clusterId;
	private boolean isVisited;
	private int groundTruthLabel;

	public Point(int tweetId, String tweet){
		this.tweetId = tweetId;
		this.tweet = tweet;
	}
	
	public Point(int tweetId, String tweet, int clusterId) {
		this.tweetId = tweetId;
		this.tweet = tweet;
		this.clusterId = clusterId;
	}
	
	public Point(int tweetId, int groundTruthLabel, String tweet) {
		this.tweetId = tweetId;
		this.tweet = tweet;
		this.groundTruthLabel = groundTruthLabel;
	}

	public int getTweetId() {
		return tweetId;
	}

	public String getTweet() {
		return tweet;
	}
	
	public void setAssignment(int clusterId){
		this.clusterId = clusterId;
	}
	
	public void setVisited(boolean visited){
		isVisited = visited;
	}
	
	public boolean isVisited(){
		return isVisited;
	}
	
	public int getAssignedClusterId(){
		return clusterId;
	}
	
	public int getGroundTruthLabel() {
		return groundTruthLabel;
	}
	
	public void setGroundTruthLabel(int groundTruthLabel) {
		this.groundTruthLabel = groundTruthLabel;
	}
	
	@Override
	public String toString(){
		return "{tweetId:" + this.tweetId + ", " + "tweet:" + this.tweet + ", "
				+ "groundTruth:" + this.groundTruthLabel + ", " + "assignedCluster:" + this.clusterId + "}" + "\n";
	}
}
