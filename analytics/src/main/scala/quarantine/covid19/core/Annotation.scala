package quarantine.covid19.core

/**
 * We derive different useful metrics for tracking the progress and the impact of the epidemic via annotations
 * 
 * At any given time an Annotation captures for a given location/time the following metrics 
 * at the current time and the average over the last movingAverageWindowSize interval
 * deathRate: total deaths / total confirmed as of date in this annotation
 * growthRateDaily: daily confirmed today / daily confirmed yesterday as of date in this annotation
 * positiveRate: total confirmed / total tested as of date in this annotation
 * movingAverageDeathRate: total deaths / total confirmed as of date in this annotation
 * movingAveragePositiveRate: total deaths / total confirmed as of date in this annotation
 * movingAverageGrowthRate
 * @author rajdeep
 */
case class Annotation(date: String, 
                       province_state: String, 
                       country: String, 
                       lat: String, 
                       long: String, 
                       deathRate: Double, 
                       growthRateDaily: Option[Double] = None, // 
                       positiveRate: Option[Double] = None, // total confirmed / total tested as of date
                       movingAverageDeathRate: Option[Double] = None, // average of deathRate over movingAverageWindowSize
                       movingAverageGrowthRate: Option[Double] = None, // average of growthRateDaily over movingAverageWindowSize
                       movingAveragePositiveRate: Option[Double] = None, // average of positiveRate over movingAverageWindowSize
                       movingAverageWindowSize: Option[Int] = None, // the time interval over which we are computing averages
                       estimatedAlpha: Option[Double] = None)
                       
