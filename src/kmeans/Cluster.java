/**
 * 
 */
package kmeans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sagar6903
 *
 */
public class Cluster {

	private int Id;
	private List<Point> points;
	
	public Cluster(int Id){
		this.Id = Id;
		points = new ArrayList<Point>();
	}
	
	public boolean containsPoint(Point p){
		return points.contains(p);
	}
	
	public int getClusterId(){
		return Id;
	}
	
	public void addPointToCluster(Point p){
		points.add(p);
	}
	
	public void addPointsToCluster(List<Point> points){
		this.points = points;
	}
	
	public List<Point> getClusterPoints(){
		return points;
	}	
}
