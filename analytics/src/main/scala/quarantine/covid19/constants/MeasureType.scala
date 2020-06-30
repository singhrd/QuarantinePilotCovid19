
package quarantine.covid19.constants


object MeasureType extends Enumeration {
  type MeasureType = Value
  
  val DailyHigh = Value("highest daily cases per 100k today")
  val BiweeklyPercentChange = Value("highest % increase in weekly average of daily cases per 100k - compared to two weeks ago")
  val MACrossover = Value("daily cases per 100k value crossing above the weekly average in last two weeks")
  
  def name(measure: MeasureType.Value): String = measure.toString()
}

