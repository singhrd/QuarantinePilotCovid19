package quarantine.covid19.core

/**
 * We derive different useful metrics for tracking the progress and the impact of the epidemic via annotations
 * 
 * At any given time an Annotation captures for a given location/time the following metrics 
 * at the current time and the average over the last movingAverageWindowSize interval
 * 
 *  @param date                        The reference date for the Annotation. Usually the latest date of CovidSnapshots  this Annotation was derived from 
 *  @param locale                      The reference locale for the Annotation
 *  @param lat                         The reference latitude for the locale of the Annotation
 *  @param long                        The reference latitude for the locale of the Annotation
 *  @param movAvgConfDailyPer100k      Basically average over the daily [CovidSnapshot] confirmedPer100k
 *  @param movAvgConfCumulativePer100k Basically average over the cumulative [CovidSnapshot] confirmedPer100k\movingAverageTotalDeathsPer100k
 *  @param movAvgEstimatedAlpha        Array of average spread rate over movingAverageWindowSize
 *                                     (Log[confirmed cases at time k] - Log[confirmed cases at time k - delta])/delta
 *  @param movAvgCFR                   Array of average CFR over movingAverageWindowSize
 *                                     CFR = total deaths / total confirmed as of date
 *  @param movAvgCFRTA                 Array of average CFR time adjusted over movingAverageWindowSize
 *                                     CFRTA = total deaths at time t / total confirmed at t - 2 weeks
 *  @param movAvgGrowthRate            Array of average dailyGrowthRate over movingAverageWindowSize
 *                                     ([daily confirmed cases at date]/[daily confirmed cases at date - delta days])^(1/delta)
 * @author rajdeep
 */
case class Annotation(date: String, 
                       locale: String, 
                       lat: String, 
                       long: String, 
                       movAvgConfirmedDailyPer100k:Array[(String, Double)], 
                       movAvgConfirmedCumulativePer100k:Array[(String, Double)],
                       movAvgEstimatedAlpha:Array[(String,Double)], 
                       movAvgCFR: Array[(String,Double)],  
                       movAvgCFRTA: Array[(String,Double)],  
                       movAvgGrowthRate: Array[(String,Double)])

                       
