package quarantine.covid19.core

/**
 * We derive different useful metrics for tracking the progress and the impact of the epidemic via annotations
 * 
 * At any given time an Annotation captures for a given location/time the following metrics 
 * at the current time and the average over the last movingAverageWindowSize interval
 * 
 *  @param date: The reference date for the Annotation. Usually the latest date of CovidSnapshots 
 *               this Annotation was derived from 
 *  @param province_state: The reference province for the Annotation
 *  @param country: The reference country for the Annotation
 *  @param lat: The reference latitude for the province_state or Country of the Annotation
 *  @param long: The reference latitude for the province_state or Country of the Annotation
 *  @param movingAverageEstimatedAlpha: Array of average spread rate over movingAverageWindowSize
 *  @param movingAverageDeathRate: Array of average cumulativeDeathRate over movingAverageWindowSize
 *  @param movingAverageGrowthRate: Array of average dailyGrowthRate over movingAverageWindowSize
 * 
 * @author rajdeep
 */
case class Annotation(date: String, 
                       province_state: String, 
                       country: String, 
                       lat: String, 
                       long: String, 
// spreadRate in the logistic/exponential equation 
//                         (Log[confirmed cases at time k] - Log[confirmed cases at time k - delta])/delta, delta = 1 by default                       
                       movingAverageEstimatedAlpha:Array[(String,Double)],
//cumulativeDeathRate: total deaths / total confirmed as of date in this Annotation
                       movingAverageDeathRate: Array[(String,Double)], 
// dailyGrowthRate: ((daily confirmed cases at date)/(daily confirmed cases at date - delta days))^(1/delta), delta = 1 by default
                       movingAverageGrowthRate: Array[(String,Double)])
//                       cumulativePositiveRate: Option[Double] = None, 
//                       movingAveragePositiveRate: Option[Double] = None, 

                       
