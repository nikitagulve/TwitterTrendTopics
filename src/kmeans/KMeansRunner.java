/**
 * 
 */
package kmeans;

import java.io.IOException;


/**
 * @author Sagar6903
 *
 */
public class KMeansRunner {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		// Modified K-Means entry point
		// The constructor KMeans(K, isStaticRun, isLiveStreaming)
		// In case you want to run on data collected over 58 hours, set the isStatic Flag to true. By default it 
		// read the files from KMeansData\\ProcessedData folder.
		// In case you want to cluster on live streaming data, set the isLiveStreaming Flag to true.
		// K is the number of clusters
		KMeans k = new KMeans(20, true, false);
		k.start();
	}
}
