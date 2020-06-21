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
  
  // Source https://www.thelancet.com/journals/laninf/article/PIIS1473-3099(20)30243-7/fulltext
  val DefaultOutcomeTimeDays = 7
  
  val DefaultMovingAverageWindowSet = List(7,21)
  
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