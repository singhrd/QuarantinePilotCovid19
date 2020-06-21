package quarantine.covid19.core

/** 
 *  This schema will be used for either Daily and Cumulative Covid Snapshot at any location/time.
 *  We will include the test data also in this if it is available.
 *  Our main source is the csv from tableau.
 *  
 *  @param date              The reference date for the CovidSnapshot. The data was either observed/collected or updated on this date.
 *  @param locale            The reference locale for the CovidSnapshot
 *  @param lat               The reference latitude for the locale of the CovidSnapshot
 *  @param long              The reference latitude for the locale or Country of the CovidSnapshot
 *  @param confirmed         Number of confirmed cases on/by the date for the location of this CovidSnapshot
 *  @param confirmedPer100k  confirmed*100000/population_locale
 *  @param confirmedPer100kN confirmedPer100k*100/population_density_of_locale
 *  @param recovered          Number of recovered cases on/by the date for the location of this CovidSnapshot
 *  @param recoveredPer100k   recovered*100000/population_locale
 *  @param active             Number of active cases on/by the date for the location of this CovidSnapshot
 *  @param activePer100k      active*100000/population_locale
 *  @param deaths             Number of deaths on/by the date for the location of this CovidSnapshot
 *  @param deathsPer100k      deaths*100000/population_locale
 *  @param source             Source for the CovidSnapshot
 *  @param tests              Number of tests performed on/by the date for the location of this CovidSnapsho
 *  
 *  @author rajdeep
 */


case class CovidSnapshot(date: String, // format mm/dd/yyyy
                         locale: String, 
                         lat: String, // aa.bb
                         long: String, // aa.bb
                         confirmed: Long, 
                         confirmedPer100k: Double, 
                         confirmedPer100kN: Double,
                         recovered: Long,
                         recoveredPer100k: Double,
                         active: Long, // not meaningful for daily snapshots - set to 0 by default
                         activePer100k: Double,
                         deaths: Long, 
                         deathsPer100k: Double,
                         epidemiccontrolThreshold: Double,
                         source: String = "tableau",
                         tests: Option[Long] = None)
                       
