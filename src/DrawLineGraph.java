import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class DrawLineGraph {
	JFrame jf = new JFrame("");
	XYSeries series1 = new XYSeries("First");
	XYSeriesCollection dataset = new XYSeriesCollection();
	JFreeChart chart;
	ChartPanel chartPanel;

	DrawLineGraph(RunFrame rf) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jf.setFocusableWindowState(false);
		dataset.addSeries(series1);
		chart = ChartFactory.createXYLineChart(jf.getTitle(), "Time(m)",
				"Temperature of T3", dataset, PlotOrientation.VERTICAL, false,
				false, false);
		chart.getXYPlot().getRangeAxis().setUpperBound(180);
		chart.getXYPlot().getRangeAxis().setLowerBound(0);
		chart.getXYPlot().getDomainAxis().setUpperBound(Configure.getGraphLength());
		chartPanel = new ChartPanel(chart);
		jf.setContentPane(chartPanel);
		jf.pack();
		jf.setVisible(true);
		jf.setBounds(0, (int) (screenSize.getHeight() * 2 / 3 - 40),
				(int) (screenSize.getWidth()),
				(int) (screenSize.getHeight() / 3));
		update(0);
	}

	public void update(double d) {
		series1.add((double) (series1.getItemCount()) / 20, d);
		dataset.seriesChanged(null);
		chartPanel.chartChanged(null);
	}

	public void close() {
		jf.dispose();
	}

	public void saveGraph() {
		/*
		 * File f = null; if (!new File("graphs").exists()) new
		 * File("graphs").mkdir(); Date date=new Date(); if
		 * (OSValidator.isUnix()) { f=new
		 * File("graphs/"+date.toString()+".png"); }else
		 * if(OSValidator.isWindows()){ f=new
		 * File("graphs\\"+date.toString()+".png"); }else{ return; } try {
		 * ChartUtilities.saveChartAsPNG(f, chartPanel.getChart(),
		 * chartPanel.getWidth(), chartPanel.getWidth()); } catch (IOException
		 * e) { e.printStackTrace(); }
		 */
	}

}
