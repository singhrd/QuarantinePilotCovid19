package quarantine.covid19.constants

object Constants {
  
  val OneHundred = 100.0

  val OneHundredThousand = 100000.0
  
  val OneMillion = 1000000.0
  
  
  val DefaultCountyRefDate = "01/22/2020"
  
  val DefaultSanDiegoZipCodeDate = "03/30/2020"
  
  val EpidemicControlThresholdPer100k = 0.5
  
  val EpidemicContainedThresholdPer100k = 1.0
  
  val CommaDelimiter = ","
  val DoubleQuotes = "\""

  val DefaultDelimiter = CommaDelimiter
  val DefaultLat = "REPLACE_ME"
  val DefaultLong = "REPLACE_ME"

  val DaysInMs = (1000 * 60 * 60 * 24).toLong;
  
  val DefaultDeltaInDays = 4
  
  val minThreshold = 5
  
  val DefaultDeltaLookUpInDaysForAlert = 14
  
  // Source https://www.thelancet.com/journals/laninf/article/PIIS1473-3099(20)30243-7/fulltext
  val DefaultOutcomeTimeDays = 7
  
  val DefaultMovingAverageWindowInDaysSet = Array(7, 14, 21)
  
  val DefaultMovingAverageWindowInDaysForAlert = 21
  
  val DefaultMapWindowInDays = 14
  
  val defaultMetrics = List(MetricType.SpreadRate, MetricType.DailyGrowth,
                            MetricType.CFR, MetricType.CFRTA,
                            MetricType.DailyPer100k,MetricType.CumulativePer100k, MetricType.Cumulative, MetricType.Daily)
                        
  val mapMeasureToDescription = Map[MeasureType.Value, String]((MeasureType.DailyHigh, " highest weekly average of daily cases per 100k today"),
                                                                (MeasureType.DailyLow, " lowest weekly average of daily cases per 100k today"),
                                                                (MeasureType.BiweeklyPercentChangeUptrend," highest increase in biweekly % change in weekly average of daily cases per 100k"),
                                                                (MeasureType.BiweeklyPercentChangeDowntrend," highest decrease in biweekly % change in weekly average of daily cases per 100k"),
                                                                (MeasureType.MACrossoverUptrend," uptrend - daily cases per 100k value crossed the weekly average in last two weeks"),
                                                                (MeasureType.MACrossoverDowntrend," downtrend - daily cases per 100k value crossed the weekly average in last two weeks"))
      
  val mappingNamesMismatch = Map[String, String](("North Macedonia" -> "Macedonia"), 
                                                 ("Taiwan*" -> "Taiwan"),
                                                  ("Congo (Brazzaville)" -> "DR Congo"),
                                                  ("Korea_South" -> "Korea"))                          


  
  val stateNameMap = Map[String, String]()
}