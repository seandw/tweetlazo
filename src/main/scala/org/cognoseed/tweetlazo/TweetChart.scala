package org.cognoseed.tweetlazo

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import org.cognoseed.tweetlazo.GoalCounter.CountsSinceLastReply

import scalafx.application.Platform
import scalafx.scene.chart.XYChart.{Data, Series}
import scalafx.scene.chart.{CategoryAxis, NumberAxis, StackedBarChart}
import scalafx.scene.layout.Priority

/** A slightly specialized [[scalafx.scene.chart.StackedBarChart]] for this application. */
class TweetChart(val hashtag: String) extends StackedBarChart(new CategoryAxis, new NumberAxis) {

  val tweetSeries = new Series[String, Number] {
    name = s"#$hashtag non-goal tweets"
  }

  val goalSeries = new Series[String, Number] {
    name = s"#$hashtag goal tweets"
  }

  data = Seq(goalSeries.delegate, tweetSeries.delegate)
  categoryGap = 1
  maxHeight = Double.MaxValue
  vgrow = Priority.Always

  def update(counts: CountsSinceLastReply) = {
    val time = DateTimeFormatter.ofPattern("HH:mm").format(LocalTime.now)
    Platform.runLater {
      goalSeries.getData.add(Data(time, counts.goalTweets))
      tweetSeries.getData.add(Data(time, counts.tweets-counts.goalTweets))
    }
  }

}
