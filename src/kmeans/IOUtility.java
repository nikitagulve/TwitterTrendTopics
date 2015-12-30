/**
 * 
 */
package kmeans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sagar6903
 *
 */
public class IOUtility {
	
	/**
	 * Print KMeans output to file
	 * @param topics
	 * @param filePath
	 * @param purity
	 * @param nmi
	 * @throws IOException
	 */
	public static void printOutputToFile(List<Cluster> topics, String filePath, Double purity, Double nmi) throws IOException {
		if(topics.size() != 0){
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			out.println("==============================================================================================================================");
			out.println("Total Clusters : " + topics.size());
			out.println();
			if(purity != null && nmi != null){
				out.println("Purity : " + purity);
				out.println("NMI : " + nmi);
			}
			int npoints = 0;
			for (Cluster c : topics) {
				npoints += c.getClusterPoints().size();
			}
			out.println("No. Of Tweets Clustered : " + npoints);
			out.println();
			for (Cluster c : topics) {
				out.println("Cluster : " + c.getClusterId());
				out.println("Total tweets in this cluster : " + c.getClusterPoints().size());
				out.println();
				out.println(c.getClusterPoints().toString());
				out.println();
			}
			out.println("==============================================================================================================================");
			out.close();
		}
	}

	/**
	 * Print top 3 densed cluster to a file
	 * @param topics
	 * @param filePath
	 * @throws IOException
	 */
	public static void printTop3DensedCluster(List<Cluster> topics, String filePath) throws IOException{
		if(topics.size() != 0){
			Collections.sort(topics, new Comparator<Cluster>() {
				@Override
				public int compare(Cluster p1, Cluster p2) {
					return p1.getClusterPoints().size() - p2.getClusterPoints().size();
				}
			});
			Collections.reverse(topics);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			out.println("Top 3 Densed Cluster : ");
			for(int top = 0; top < 3; top++){
				out.println("Cluster Id - " + topics.get(top).getClusterId());
				out.println(topics.get(top).getClusterPoints().toString());
				out.println();
				out.println("----------------------------------------------------------------------");
				out.println();
			}
			out.close();
		}
	}
}