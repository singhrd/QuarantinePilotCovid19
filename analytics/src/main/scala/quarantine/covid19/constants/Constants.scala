package quarantine.covid19.constants

object Constants {
  
  val OneHundred = 100.0

  val OneHundredThousand = 100000.0
  
  val OneMillion = 1000000.0
  
  
  val DefaultCountyRefDate = "01/22/2020"
  
  val EpidemicControlThresholdPer100k = 0.5
  
  val CommaDelimiter = ","
  val DoubleQuotes = "\""

  val DefaultDelimiter = CommaDelimiter
  val DefaultLat = "ReplaceMe"
  val DefaultLong = "ReplaceMe"

  val DaysInMs = (1000 * 60 * 60 * 24).toLong;
  
  val DefaultDeltaInDays = 4
  
  val DefaultDeltaLookUpInDaysForAlert = 14 
  // Source https://www.thelancet.com/journals/laninf/article/PIIS1473-3099(20)30243-7/fulltext
  val DefaultOutcomeTimeDays = 7
  
  val DefaultMovingAverageWindowInDaysSet = List(7, 21)
  
  val DefaultMovingAverageWindowInDaysForAlert = 21
  
  val defaultMetrics = List(MetricType.SpreadRate, MetricType.DailyGrowth,
                            MetricType.CFR, 
                            MetricType.DailyPer100k,MetricType.CumulativePer100k)
                            
  val mappingNamesMismatch = Map[String, String](("North Macedonia" -> "Macedonia"), 
                                                 ("Taiwan*" -> "Taiwan"),
                                                  ("Congo (Brazzaville)" -> "DR Congo"),
                                                  ("Korea_South" -> "Korea"))                          
  /**
   * 
   */
  def alertTemplate(metric: String, windowSize: String, deltaInDays: Int, changeInPercent: Double): String =  {
    val changeDescription = changeInPercent > 0 match {
      case true => " increased by " + scala.math.abs(changeInPercent)
      case false =>  " decreased by " + scala.math.abs(changeInPercent)
    }
    
    windowSize + " " +  metric + " " + changeDescription + " in the last " + deltaInDays + " days."
  }

  
}