package IPLSA;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Class to create Line and Bar Charts for the clusters
 * @author yogi
 *
 */
public class CreateGraph {
	public void plotGraph(Map<Integer, List<Integer>> clusterTimeCount)
			throws IOException {
		CategoryDataset line_chart_dataset = createDataSet(clusterTimeCount);
		
		JFreeChart lineChartObject = ChartFactory.createLineChart(
				"Tweets Vs Time", "Time", "Number of Tweets",
				line_chart_dataset, PlotOrientation.VERTICAL, true, true,
				false);
		
		JFreeChart barChartObject = ChartFactory.createBarChart(
				"Tweets Vs Time", "Time", "Number of Tweets",
				line_chart_dataset, PlotOrientation.VERTICAL, true, true,
				false);

		int width = 640; /* Width of the image */
		int height = 480; /* Height of the image */
		File lineChart = new File("LineChart.jpeg");
		ChartUtilities.saveChartAsJPEG(lineChart, lineChartObject, width,
				height);
		
		File barChart = new File("BarChart.jpeg");
		ChartUtilities.saveChartAsJPEG(barChart, barChartObject, width,
				height);
	}

	/**
	 * Creates the dataset for the graph
	 * @param clusterTimeCount
	 * @return
	 */
	private CategoryDataset createDataSet(
			Map<Integer, List<Integer>> clusterTimeCount) {
		DefaultCategoryDataset line_chart_dataset = new DefaultCategoryDataset();
		
		for(int c:clusterTimeCount.keySet()) {
			List<Integer> counts = clusterTimeCount.get(c);
			int prev = 0;
			for(int i=1;i<=counts.size();i++) {
				line_chart_dataset.addValue(counts.get(i-1) - prev, "Cluster "+ c, Integer.toString(i));
				prev = counts.get(i-1);
			}
		}
		
		return line_chart_dataset;
	}
}
