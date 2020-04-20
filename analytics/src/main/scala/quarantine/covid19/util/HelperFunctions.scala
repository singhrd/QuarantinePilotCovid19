package quarantine.covid19.util


import java.util.Date
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.core.CovidSnapshots
import quarantine.covid19.core.CovidSnapshot

object HelperFunctions {
  
  // We are using this format for the Date string in the CovidSnapshots
	val format = new SimpleDateFormat("MM/dd/yyyy");
	
	// Millis in a day
	val DAY_IN_MS = (1000 * 60 * 60 * 24).toLong;
	
	/**
	 * In case of a daily snapshot missing, we can jumpstart with the default snapshot with 0 baseline
	 */
  def createDefaultCovidSnapshot(date: String, province_state: String, country: String, loc: GeoLocation): CovidSnapshot = 
     CovidSnapshot(date, province_state, country, loc.lat, loc.long, 0L, 0L, 0L, 0L, "HelperFunction",None)
     
  
     /**
      * Return the sum if both are present, else return the one with a value or None if both are missing
      */
  def combine(value1: Option[Long], value2:Option[Long]): Option[Long] = {
     value1 match {
       case None => {
         value2 match {
           case None => None
           case Some(x) => Some(x)
         }
       }
       case Some(y) => {
         value2 match {
           case None => Some(y)
           case Some(x) => Some(x+y)
         }
       }
     }
  }   
  
  /**
   * Assume the two belong to the same location and have the same source
   * 
   * @param dailySnapshot: Covid Snapshot for location "l" for some day "d"
   * @param cumulativeCovidSnapshot: Cumulative Covid Snapshot for the location "l" for day "d-1"
   */
  def combineCovidSnapshots(daily: CovidSnapshot, cumulative: CovidSnapshot): CovidSnapshot = {
    
      CovidSnapshot(daily.date, daily.province_state, daily.country, daily.lat, daily.long,
                    daily.confirmed+cumulative.confirmed,
                    daily.recovered+cumulative.recovered,
                    daily.active+cumulative.active,
                    daily.deaths+cumulative.deaths,
                    daily.source, combine(daily.tests, cumulative.tests))
  }
  
  /**
   * This is just an accumulator of all daily snapshots into a rolling cumulative at any day between the first date 
   * in the snapshots and the last date in the snapshots
   * We first get all locations, then we get the Date Range
   * We then process for each location by using the sorted date covidsnapshots
   * If there is a daily snapshot missing, we just copy the cumulative from the previoous day
   * The starting day daily is the cumulative
   * If starting date is missing for some location, we set the cumulative to 0 by default
   *  
   */
  def createCumulativeCovidSnapshots(dailySnapshots: CovidSnapshots): CovidSnapshots = {
  
    // Compute for each distinct location
    val locs = HelperFunctions.distinct(dailySnapshots.snapshots.map(d => GeoLocation(d.lat, d.long)))
    val locCovidSnapshotMap = locs.map(loc => (loc -> HelperFunctions.filter(loc, dailySnapshots)))
        
    // Compute for each distinct date
    val datesSorted = dailySnapshots.snapshots.map(dS => dS.date).distinct.map(dSS => (dSS, HelperFunctions.getDate(dSS))).sortWith((a,b) => a._2.before(b._2))    
    // Cumulative on day i is cumulative on day i-1 + daily on day i
    val dateMin = datesSorted(0)
    val dateMax = datesSorted.last
//    println(dateMin, dateMax)
//    println(dateMin._2.getDate,dateMax._2.getDate)
    val diffDays = HelperFunctions.daysBetween(dateMin._2, dateMax._2)
    val mapLocationDateToCovidSnapshot = scala.collection.mutable.Map[(GeoLocation, Date), CovidSnapshot]()
   
    locCovidSnapshotMap.foreach(locValue => {
      val loc = locValue._1
      val snapshot = locValue._2
      snapshot.snapshots.length > 0 match {
        case false => // do nothing
        case true => {
        	val province_state = snapshot.snapshots(0).province_state
        	val country = snapshot.snapshots(0).country
        			
        	Range(0,diffDays.toInt+1).foreach(i => {
        		val dt = datesSorted(i)._2
        		val dtString = datesSorted(i)._1
        		// check if there are any snapshots for this loc,dt combination
        		val matchesFound = snapshot.snapshots.filter(cs => cs.date.equals(dtString))
        		i==0 match {
        		  case true => {
        		    matchesFound.length >0 match { // implies exactly one match per loc.date
        			    case true => mapLocationDateToCovidSnapshot.+=((loc,dt) -> matchesFound(0))
        				  case false => mapLocationDateToCovidSnapshot.+=((loc,dt) -> HelperFunctions.createDefaultCovidSnapshot(dtString, province_state, country, loc))
        			  }
        		  }
        		  case false => { // now we are looking at a date past the min date, so we should have the cumulative value for minDate already or the previous day
        		    val dateBefore = HelperFunctions.dateDaysBefore(dt, 1)
        		    matchesFound.length >0 match { // implies exactly one match per loc.date
        			    case true => mapLocationDateToCovidSnapshot.+=((loc,dt) -> combineCovidSnapshots(matchesFound(0),mapLocationDateToCovidSnapshot.get((loc,dateBefore)).get))
        				  case false => mapLocationDateToCovidSnapshot.+=((loc,dt) -> HelperFunctions.deepCopyCovidSnapshot(mapLocationDateToCovidSnapshot.get((loc,dateBefore)).get))
        			  }        		    
          		}
        		}
        	})
        }
      }
    })
    CovidSnapshots(mapLocationDateToCovidSnapshot.toMap.values.toList)
  }
  
  /**
   * Just a clone
   */
  def deepCopyCovidSnapshot(covidSnapshot: CovidSnapshot): CovidSnapshot = {
    CovidSnapshot(covidSnapshot.date, covidSnapshot.province_state, covidSnapshot.country, covidSnapshot.lat, covidSnapshot.long,
                    covidSnapshot.confirmed,
                    covidSnapshot.recovered,
                    covidSnapshot.active,
                    covidSnapshot.deaths,
                    covidSnapshot.source, covidSnapshot.tests)
  }
    
    
  /**
   * Get Date from dateString in format "MM/dd/yyyy"
   */
  def getDate(dateString: String): Date = format.parse(dateString); 
  
  /**
   * Check if a date is before or the same as another 
   */
  def beforeOrEqual(dateStringToCheck: String, dateStringReference: String): Boolean = {
    val dateToCheck = getDate(dateStringToCheck)
    val dateReference = getDate(dateStringReference)
    dateToCheck.equals(dateReference) || dateToCheck.before(dateReference)
  }
  
  /**
   * This might need to be replaced with something that is not deprecated
   */
  def daysBetween(date1: Date, date2: Date): Long = {
   ChronoUnit.DAYS.between(LocalDate.of(date1.getYear, date1.getMonth, date1.getDate), LocalDate.of(date2.getYear, date2.getMonth, date2.getDate))
  }
  
  /**
   * Find the Date days before the given date
   */
  def dateDaysBefore(date: Date, days: Long): Date = {
    new Date(date.getTime() - (days * DAY_IN_MS))
  }
  /**
   * Check if latLong is the same as a geoLocation loc
   */
  def equals(latLong: (String, String), loc: GeoLocation) = loc.lat.equals(latLong._1) && loc.long.equals(latLong._2)
  
  /**
   * Check if loc1 and loc2 are equal (have same lat and long)
   */
  def equals(loc1: GeoLocation, loc2: GeoLocation) =  loc1.lat.equals(loc2.lat) && loc1.long.equals(loc2.long)
  
  /**
   * Check if the snapshots belong to the same Date
   */
  def sameDateSnapshot(snapshot1: CovidSnapshot, snapshot2: CovidSnapshot): Boolean  = snapshot1.date.equals((snapshot2.date))
  
  
  /**
   * Find the set from the a list of geo-locations - essentially the unique/distinct elements
   */
  def distinct(locs: List[GeoLocation]): List[GeoLocation] = locs.toSet.toList
  
  
  /**
   * Filter the CovidSnapshots for a given location loc only
   */
  def filter(loc: GeoLocation, covidSnapshots:CovidSnapshots): CovidSnapshots = 
    CovidSnapshots(covidSnapshots.snapshots.filter(x => equals((x.lat, x.long),loc)))
  
    
}
