# TwitterTrendTopics

About the Project
**************************************************************************************************************

Twitter represents a rich social media where people share their thoughts and interests. Twitter data can be used to find out what’s currently trending among wide number of users. The trending topics could be news of nationwide interest or a tweet from influential figure that caught lot of people’s attention. The tweets represent text data. It’s a challenging data mining task because it is done on real time data and huge data. To limit our scope, we are interested in Boston area only and we accept tweets in English language. Our approach is to solve this problem using historic data and compare against live data to find the current trending topics. We also limit our topics to unigrams to reduce the complexity. 

About the Folder Structure
**************************************************************************************************************

TwitterTrendTopics - Eclipse folder with the code
  —-src/IPLSA
   —- PlsaRunner.java - Runs IPLSA on the data provided. Input: Folder and total number of files(should be greater than 5)
   —- TestDriver.java - Runs Plsa for test data
   —- CreateGraph.java - Class to create line and bar charts for the results
   —- TopicValue.java - Class to hold the count for each topic/cluster
   —- Tweet.java - Data structure for a tweet
   —- utility.java - provided some functions like readFile, calculatePurity, calculateNMI etc
  —-src/Kmeans
   —- KMeansRunner.java - Entry point for KMeans Algorithm. Input: K, isStaticRunner  
      boolean flag, isLiveStreamRunner boolean flag (see more details below)
   —- KMeans.java - Class implementing the entire Modified K-Means alogrithm 
   —- Cluster.java - Class for representing the cluster of tweets
   —- Point.java - Class for representing each tweets as a point
   —- LiveTweetsCleaner.java - Cleaner Class for unprocessed raw tweets
   —- DistanceHelper.java - Class for computing similarity measure: Jaccards, Cosine 
     and MinHash
   —- EvaluationMeasures.java - Class for implementing Purity and NMI
   —- IOUtility.java - Handles all the input output operations for the entire run of 
     the algorithm
   —- MinHash.java - Implements entire MinHash Similarity measure
  —-src/Preprocess
   —- LoadRawJSON.java - Loads the collected tweets and process and cleans the text
  —-src/Collection
   —- SimpleStream.java - Streams the public tweets from Twitter in boston area. Provide a file to redirect output of console
  —- emoji-java-2.2.0.jar
  —- json-20140107.jar
  —- twitter4j-core-4.0.4.jar
  —- twitter4j-stream-4.0.4.jar
  —- weka.jar
  —-jcommon-1.0.23.jar
  —-jfreechart-1.0.19.jar
  —-EachHour - Unprocessed raw tweets for 58 hours
  —-EachHourProcessed - Cleaned tweets for 58 hours
  —-IPLSAData - Data for IPLSA Run
  —-KmeansData - Data for Kmeans run
  —-OutputsandResults - Output for both algorithm runs
  —-TestTweets - Tweets for Evaluation purpose
README

OUTPUT
**************************************************************************************************************

The output is stored in the respective algorithm folder inside the output folder.

About the Algorithms
**************************************************************************************************************

1) IPLSA

In this, we have tried to implement Incremental PLSA. For starting the algorithm, some files are provided as background and normal PLSA is run over it.

PlsaRunner runs multiple iterations of incremental PLSA over the files considering each file as new Tweets after a time, i.e. 1.txt -> Hour 1, 2.txt -> Hour 2 and so on.

After the first run, we try to add new tweets and predict clusters for them based on the words in the new tweets. The p(w|z) is updated and p(z|d) is randomized for all new tweets, where d is a tweet, w is a word and z is a topic/cluster.

The M-step updated the p(w|z) based on the factor alpha set to 0.8. and gives weight to new tweets more.

At each iteration new file with tweets is added and em is run to get the popular clusters. The code prints out Top 10 words from each cluster and prints the top 3 popular clusters at each iteration.

After all iterations are completed, it prints out the graph for the clusters based on difference of new documents added to that cluster.


TestDriver.java -> Used to run the code on test data and calculate purity of clusters and NMI.

*************************************************************************************
2)  Modified K-Means

All K-Means related source code can be found under kmeans package. All the results of this algorithm can be found under KMeansResults folder, while the dataset used can be found under KMeansData folder.

In order to run this program successfully, you can import entire project in eclipse with all dependency and kick start the KMeansRunner.java. If you want to run this from console, then you have to include manually all the dependencies in the classpath correctly and then kick off KMeansRunner.java using javac command. 

Note:

Entry point to the Modified K-Means algorithm is KMeansRunner.java. 
  Inside the main method Kmeans algorithm is initialized using the constructor (numberOfClusters, isStaticRunner, isLiveStreamRunner).
		numberOfClusters = K, number of clusters you want
		isStaticRunner = Boolean flag, set true to run algorithm for data collected over 2 days
		isLiveStreamRunner = Boolean flag, set true to run it on live streaming data
 
There are 3 different similarity measure implemented: Jaccards, Cosine and MinHash. By default Jaccards would be used. In case if you want to choose any other one, just uncomment the respective lines in Kmeans.java, checkForTweetsSimilarity () function Line 177. In addition you have to change the output location (refer below step) as well in Kmeans.java file, start () function Line 82.
 
If you decide to run this algorithm on data collected for over 2 days (i.e. isStaticRunner = true) and the similarity measure to be Jaccards, the results would be stored in the subfolder Jaccards under KMeansResults folder. Similarly for Cosine, it would be stored in the subfolder Cosine and for MinHash, it would be under MinHash.
 
For Running Evaluation Tweets, you have to make modification to start () function in KMeans.java to point to the correct file located under TestTweets folder. Also make changes to the output location.

Note that the running time of the algorithm is pretty significant since it is dealing with large dataset, performing many computation and reiterating over and over till convergence.

