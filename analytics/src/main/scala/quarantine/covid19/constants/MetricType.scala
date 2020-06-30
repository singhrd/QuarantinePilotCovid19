package quarantine.covid19.constants


object MetricType extends Enumeration {
  type MetricType = Value
  
  val SpreadRate = Value("Spread Rate")
  val DailyGrowth = Value("Daily Growth Rate")
  val CFR = Value("Confirmed Fatality Rate")
  val CFRTA = Value("Time Adjusted Confirmed Fatality Rate")
//  val Daily = Value("Daily cases")
  val DailyPer100k = Value("Confirmed Daily Cases per 100k")
  val CumulativePer100k = Value("Confirmed Cumulative Cases per 100k")
  
  def name(metric: MetricType.Value): String = metric.toString()
}

