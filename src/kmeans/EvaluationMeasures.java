/**
 * 
 */
package kmeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sagar6903
 *
 */
public class EvaluationMeasures {

	public static double computePurity(List<Cluster> clusters, int numberOfPoints){
		double purity = 0.0;
		int clusterLabel = 0;
		for(Cluster cluster : clusters){
			Map<Integer, Integer> labelCount = new HashMap<Integer,Integer>();
			for(Point p : cluster.getClusterPoints()){
				if(labelCount.containsKey(p.getGroundTruthLabel())){
					int count = labelCount.get(p.getGroundTruthLabel());
					labelCount.put(p.getGroundTruthLabel(), (count + 1));
				}
				else
					labelCount.put(p.getGroundTruthLabel(), 1);
			}
			// get the count of most repeated class label amongst all the points in the cluster.
			clusterLabel += Collections.max(labelCount.values());
		}
		purity = clusterLabel/ (double)numberOfPoints;
		return purity;
	}
	
	public static double computeNormalizedMutualInformation(List<Cluster> clusters, double N){
		double normalizedMutualInformation = 0.0;
		double I = 0.0;
		double HOmega = 0.0;
		double Hc = 0.0;
		
		Map<Integer, Integer> wk = new HashMap<Integer, Integer>();
		Map<Integer, Integer> cj = new HashMap<Integer, Integer>();
		List<Point> clusterPoints = new ArrayList<>();
		for (Cluster c : clusters) {
			// Add all clusters points to a list
			clusterPoints.addAll(c.getClusterPoints());

			// Find cj :- This function computes the total number of points based on
			// class labels per clustes basis
			for (Point p : c.getClusterPoints()) {
				if (cj.containsKey(p.getAssignedClusterId())) {
					int count = cj.get(p.getAssignedClusterId());
					cj.put(p.getAssignedClusterId(), count + 1);
				} else
					cj.put(p.getAssignedClusterId(), 1);
			}
		}

		// Find wk :- If there are 3 labels ( lets say 1,2,3 ) for the dataset, then this funtion computes
		// wk(1, count) wk(2,count) and wk(3, count) for ALL the clusters
		for (Point p : clusterPoints) {
			if (wk.containsKey(p.getGroundTruthLabel())) {
				int count = wk.get(p.getGroundTruthLabel());
				wk.put(p.getGroundTruthLabel(), count + 1);
			} else
				wk.put(p.getGroundTruthLabel(), 1);
		}
		
		// Iterate over all clusters
		for(Cluster cluster : clusters){

			Map<Integer, Integer> classLabelMap = new HashMap<Integer, Integer>();
			Map<Integer, Integer> cCount = new HashMap<Integer, Integer>();
			int classLabelCount = 0;
			
			// Iterate over all the points within the cluster to find I
			for (Point p : cluster.getClusterPoints()) {
				if (classLabelMap.containsKey(p.getGroundTruthLabel())) {
					int count = classLabelMap.get(p.getGroundTruthLabel());
					classLabelMap.put(p.getGroundTruthLabel(), (count + 1));
				} else
					classLabelMap.put(p.getGroundTruthLabel(), 1);
			}
			
			
			for (Map.Entry<Integer, Integer> entry : classLabelMap.entrySet()) {
				classLabelCount += entry.getValue();
			}
			cCount.put(cluster.getClusterId(), classLabelCount);
			
			// compute I
			for (Map.Entry<Integer, Integer> entry : classLabelMap.entrySet()) {
				double denominator = Math.log((N * entry.getValue())
						/ (wk.get(entry.getKey()) * cCount.get(cluster
								.getClusterId())))
						/ Math.log(2);
				I += (entry.getValue() / N) * denominator;
			}
		}
		
		// Compute Entropy H(Omega) 
		HOmega = computeEntropy(wk, N);

		// Compute Entropy H(C)
		Hc = computeEntropy(cj, N);

		// Compute NMI 
		normalizedMutualInformation = I / Math.sqrt(HOmega * Hc);
		return normalizedMutualInformation;
	}
	
	private static double computeEntropy(Map<Integer, Integer> map, double N){
		double entropy = 0.0;
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			entropy += -(entry.getValue() / N)
					* (Math.log(entry.getValue() / N)) / Math.log(2);
		}
		return entropy;
	}

}
