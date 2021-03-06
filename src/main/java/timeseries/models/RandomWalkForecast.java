package timeseries.models;

import java.awt.Color;
import java.awt.Font;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.markers.None;

import com.google.common.primitives.Doubles;

import stats.distributions.Normal;
import timeseries.TimeSeries;

/**
 * A forecast for a random walk model.
 * @author Jacob Rachiele
 *
 */
public final class RandomWalkForecast implements Forecast {

  private final Model model;
  private final TimeSeries forecast;
  private final TimeSeries upperValues;
  private final TimeSeries lowerValues;
  private final double criticalValue;
  private final TimeSeries fcstErrors;

  public RandomWalkForecast(final RandomWalk model, final int steps, final double alpha) {
    this.model = model;
    this.forecast = model.pointForecast(steps);
    this.criticalValue = new Normal(0, model.residuals().stdDeviation()).quantile(1 - alpha / 2);
    this.fcstErrors = getFcstErrors();
    this.upperValues = computeUpperPredictionValues(steps, alpha);
    this.lowerValues = computeLowerPredictionValues(steps, alpha);
  }
  
  @Override
  public final TimeSeries forecast() {
    return this.forecast;
  }

  @Override
  public final TimeSeries upperPredictionValues() {
    return this.upperValues;
  }
  
  @Override
  public final TimeSeries lowerPredictionValues() {
    return this.lowerValues;
  }
  
  @Override
  public final TimeSeries computeUpperPredictionValues(final int steps, final double alpha) {
    double[] upperPredictionValues = new double[steps];
    double criticalValue = new Normal(0, model.residuals().stdDeviation()).quantile(1 - alpha / 2);
    for (int t = 0; t < steps; t++) {
      upperPredictionValues[t] = forecast.at(t) + criticalValue * Math.sqrt(t + 1);
    }
    return new TimeSeries(forecast.timePeriod(), forecast.observationTimes().get(0),
        upperPredictionValues);
  }

  /* (non-Javadoc)
   * @see timeseries.models.Forecast#lowerPredictionInterval(int, double)
   */
  @Override
  public final TimeSeries computeLowerPredictionValues(final int steps, final double alpha) {
    double[] upperPredictionValues = new double[steps];
    double criticalValue = new Normal(0, model.residuals().stdDeviation()).quantile(1 - alpha / 2);
    for (int t = 0; t < steps; t++) {
      upperPredictionValues[t] = forecast.at(t) - criticalValue * Math.sqrt(t + 1);
    }
    return new TimeSeries(forecast.timePeriod(), forecast.observationTimes().get(0),
        upperPredictionValues);
  }
  
  private TimeSeries getFcstErrors() {
    double[] errors = new double[forecast.n()];
    for (int t = 0; t < errors.length; t++) {
      errors[t] = criticalValue * Math.sqrt(t + 1);
    }
    return new TimeSeries(forecast.timePeriod(), forecast.observationTimes().get(0), errors);
  }

  @Override
  public final void plot() {
    new Thread(() -> {
      final List<Date> xAxis = new ArrayList<>(forecast.observationTimes().size());
      final List<Date> xAxisObs = new ArrayList<>(model.timeSeries().n());
      for (OffsetDateTime dateTime : model.timeSeries().observationTimes()) {
        xAxisObs.add(Date.from(dateTime.toInstant()));
      }
      for (OffsetDateTime dateTime : forecast.observationTimes()) {
        xAxis.add(Date.from(dateTime.toInstant()));
      }

      List<Double> errorList = Doubles.asList(fcstErrors.series());
      List<Double> seriesList = Doubles.asList(model.timeSeries().series());
      List<Double> forecastList = Doubles.asList(forecast.series());
      final XYChart chart = new XYChartBuilder().theme(ChartTheme.GGPlot2).height(800).width(1200)
          .title("Random Walk Past and Future").build();

      XYSeries observationSeries = chart.addSeries("Past", xAxisObs, seriesList);
      XYSeries forecastSeries = chart.addSeries("Future", xAxis, forecastList, errorList);

      observationSeries.setMarker(new None());
      forecastSeries.setMarker(new None());

      observationSeries.setLineWidth(0.75f);
      forecastSeries.setLineWidth(1.5f);

      chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line).setErrorBarsColor(Color.RED);
      observationSeries.setLineColor(Color.BLACK);
      forecastSeries.setLineColor(Color.BLUE);

      JPanel panel = new XChartPanel<>(chart);
      JFrame frame = new JFrame("Random Walk Past and Future");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
    }).start();
  }

  @Override
  public final void plotForecast() {
    new Thread(() -> {
      final List<Date> xAxis = new ArrayList<>(forecast.observationTimes().size());
      for (OffsetDateTime dateTime : forecast.observationTimes()) {
        xAxis.add(Date.from(dateTime.toInstant()));
      }

      List<Double> errorList = Doubles.asList(fcstErrors.series());
      List<Double> forecastList = Doubles.asList(forecast.series());
      final XYChart chart = new XYChartBuilder().theme(ChartTheme.GGPlot2).height(600).width(800)
          .title("Random Walk Forecast").build();

      chart.setXAxisTitle("Time");
      chart.setYAxisTitle("Forecast Values");
      chart.getStyler().setAxisTitleFont(new Font("Arial", Font.PLAIN, 14));
      chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line).setErrorBarsColor(Color.RED)
      .setChartFontColor(new Color(112, 112, 112));     
      
      XYSeries forecastSeries = chart.addSeries("Forecast", xAxis, forecastList, errorList);
      forecastSeries.setMarker(new None());
      forecastSeries.setLineWidth(1.5f);
      forecastSeries.setLineColor(Color.BLUE);

      JPanel panel = new XChartPanel<>(chart);
      JFrame frame = new JFrame("Random Walk Forecast");
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.add(panel);
      frame.pack();
      frame.setVisible(true);
    }).start();
  }
}
