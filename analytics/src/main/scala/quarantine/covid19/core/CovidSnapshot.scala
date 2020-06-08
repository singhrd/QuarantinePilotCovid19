package quarantine.covid19.core

/** 
 *  This schema will be used for either Daily and Cumulative Covid Snapshot at any location/time.
 *  We will include the test data also in this if it is available.
 *  Our main source is the csv from tableau.
 *  
 *  @param date: The reference date for the CovidSnapshot. 
 *               The data was either observed/collected or updated on this date.
 *  @param province_state: The reference province for the CovidSnapshot
 *  @param country: The reference country for the CovidSnapshot
 *  @param lat: The reference latitude for the province_state or Country of the CovidSnapshot
 *  @param long: The reference latitude for the province_state or Country of the CovidSnapshot
 *  @param confirmed: Number of confirmed cases on/by the date for the location of this CovidSnapshot
 *  @param recovered: Number of recovered cases on/by the date for the location of this CovidSnapshot
 *  @param active: Number of active cases on/by the date for the location of this CovidSnapshot
 *  @param deaths: Number of deaths on/by the date for the location of this CovidSnapshot
 *  @param source: Source for the CovidSnapshot
 *  @param tests: Number of tests performed on/by the date for the location of this CovidSnapsho
 *  
 *  @author rajdeep
 */


case class CovidSnapshot(date: String, // what format mm/dd/yyyy
                       locale: String, 
                       lat: String, // aa.bb
                       long: String, // aa.bb
                         confirmed: Long, 
                         confirmedNormalized: Double, // rename
                         confirmedNormalizedPD: Double,
                         recovered: Long,
                         recoveredNormalized: Double,
                         active: Long, // not meaningful for daily snapshots - set to 0 by default
                         activeNormalized: Double,
                         deaths: Long, 
                         deathsNormalized: Double,
                         epidemiccontrolThreshold: Double,
                       source: String = "tableau",
                       tests: Option[Long] = None)
                       
