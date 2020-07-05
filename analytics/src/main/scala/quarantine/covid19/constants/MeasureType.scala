
package quarantine.covid19.constants


object MeasureType extends Enumeration {
  type MeasureType = Value
  
  
  
  val DailyHigh = Value("DailyHigh")
  val DailyLow = Value("DailyLow")
  
  val BiweeklyPercentChangeUptrend = Value("BiweeklyPercentChangeUptrend")
  val BiweeklyPercentChangeDowntrend = Value("BiweeklyPercentChangeDowntrend")
  
  val MACrossoverUptrend = Value("MACrossoverUptrend")

  val MACrossoverDowntrend = Value("MACrossoverDowntrend")
  def name(measure: MeasureType.Value): String = measure.toString()
}

