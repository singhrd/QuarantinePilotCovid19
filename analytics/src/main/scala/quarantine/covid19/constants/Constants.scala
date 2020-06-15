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
  
  val DefaultMovingAverageWindowSet = List(7,21)
  
}