package quarantine.covid19.util

/**
 * This object provides helpful functions to handle dates, filters, 
 * annotations/metrics, and transformations of CovidSnapshots.
 * 
 * @author rajdeep
 */

import java.util.Date
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.core.CovidSnapshots
import quarantine.covid19.core.CovidSnapshot
import quarantine.covid19.core.Annotation
import quarantine.covid19.core.Annotations

object HelperFunctions {
  
  // We are using this format for the Date string in the CovidSnapshots
	val format = new SimpleDateFormat("MM/dd/yyyy");
	
	// Millis in a day
	val DAY_IN_MS = (1000 * 60 * 60 * 24).toLong;
	
	def makeFileNameFriendlyDateString(dateString: String) = {
	  val elements = dateString.split("/")
	  val elem0 = elements(0).length == 1 match {
	    case true => "0" + elements(0)
	    case false => elements(0)
	  }
		val elem1 = elements(1).length == 1 match {
	    case true => "0" + elements(1)
	    case false => elements(1)
	  }
		elem0 + elem1 + elements(2)
	
	}
	
	def pivotOnCountry(cSnapshots: CovidSnapshots, countryName:String): CovidSnapshots = {
   CovidSnapshots(cSnapshots.snapshots.filter(cs => cs.country.equals(countryName)))
  }

  def pivotOnDate(cSnapshots: CovidSnapshots, dateString:String): CovidSnapshots = {
   CovidSnapshots(cSnapshots.snapshots.filter(cs => cs.date.equals(dateString)))
  }
  
  def pivotAnnotationsOnCountry(annotations: Annotations, countryName:String): Annotations = {
   Annotations(annotations.elements.filter(an => an.country.equals(countryName)))
  }

  def pivotAnnotationsOnDate(annotations: Annotations, dateString:String): Annotations = {
   Annotations(annotations.elements.filter(an => an.date.equals(dateString)))
  }
  
	/**
	 * In case of a daily snapshot missing, we can jumpstart with the default snapshot with 0 baseline
	 */
  def createDefaultCovidSnapshot(date: String, province_state: String, country: String, epidemicControlThreshold: Double, loc: GeoLocation): CovidSnapshot = 
     CovidSnapshot(date, province_state, country, loc.lat, loc.long, 0L, 0.0, 0L, 0.0, 0L, 0.0, 0L, 0.0, epidemicControlThreshold, "HelperFunction",None)
   
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
                    daily.confirmedNormalized+cumulative.confirmedNormalized,
                    daily.recovered+cumulative.recovered,
                    daily.recoveredNormalized+cumulative.recoveredNormalized,
                    // may be make more formal pattern check to assign None to active when None in both
                    daily.active+cumulative.active,
                    daily.activeNormalized+cumulative.activeNormalized,
                    daily.deaths+cumulative.deaths,
                    daily.deathsNormalized+cumulative.deathsNormalized,
                    cumulative.epidemiccontrolThreshold,
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
  def createCumulativeCovidSnapshots(dailySnapshots: CovidSnapshots, generateAnnotations: Boolean = false): (CovidSnapshots, Option[List[Annotation]]) = {
    
    // Compute for each distinct location
    val locs = HelperFunctions.distinct(dailySnapshots.snapshots.map(d => GeoLocation(d.lat, d.long)))
    val locCovidSnapshotMap = locs.map(loc => (loc -> filter(dailySnapshots, loc))).toMap
        
    // Compute for each distinct date
    val datesSorted = dailySnapshots.snapshots.map(dS => dS.date).distinct.map(dSS => (dSS, HelperFunctions.getDate(dSS))).sortWith((a,b) => a._2.before(b._2))    
    // Cumulative on day i is cumulative on day i-1 + daily on day i
    val dateMin = datesSorted(0)
    val dateMax = datesSorted.last
    val diffDays = HelperFunctions.daysBetween(dateMin._2, dateMax._2)
    val mapLocationDateToCovidSnapshot = scala.collection.mutable.Map[(GeoLocation, Date), CovidSnapshot]()
    val mapLocationToAnnotation = scala.collection.mutable.Map[GeoLocation, List[Annotation]]()
    
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
        				  case false => mapLocationDateToCovidSnapshot.+=((loc,dt) -> HelperFunctions.createDefaultCovidSnapshot(dtString, province_state, country, 0.5, loc))
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
      val cumulativeSnapshotsForLoc = CovidSnapshots(mapLocationDateToCovidSnapshot.filter(x => sameLoc(x._1._1,loc)).values.toList)
      // we have the cumulatives finished here so we can compute the annotations now
      generateAnnotations match {
        case false => // do nothing
        case true => mapLocationToAnnotation.+=(loc -> createAnnotation(locCovidSnapshotMap(loc),cumulativeSnapshotsForLoc, 4, 4, List(7,14,21)))
      }
    })
    
    val finalAnnotations = generateAnnotations match {
      case false => None
      case true => Some(mapLocationToAnnotation.toMap.values.reduceLeft(_++_))
    }
    (CovidSnapshots(mapLocationDateToCovidSnapshot.toMap.values.toList),finalAnnotations) 
  }
  
  
  /**
   * sort the snapshots per the earliest reported date to the latest reported date
   */
  def sortSnapshots(cs: List[CovidSnapshot]): List[CovidSnapshot] = {
    cs.sortWith((a,b) => HelperFunctions.beforeOrEqual(a.date,b.date))
  }
  
  
  def findNonZeroElement(sortedSnapshot: List[CovidSnapshot], maxLookUpDays: Int): Option[Int] = {
    sortedSnapshot.length > 1 match {
      case false => None // first time occurence set to 0
      case true => {
    	  val reverseSorted = sortedSnapshot.reverse
    			  sortedSnapshot.last.confirmed > 0L match {
    			    case false => None // if latest value is 0, set to 0
    			    case true => {
    				    Range(1, scala.math.min(sortedSnapshot.length,maxLookUpDays)).map(i => (i -> reverseSorted(i))).find(x => x._2.confirmed >0L) match {
    				      case None => None // this one can be debated to default to some other value
    				      case Some(y) => Some(y._1)
    				      }
    				    }
    			    }
    			  }
    }    
  }

  
  def dailyGrowthEstimate(dSorted: List[CovidSnapshot], maxLookUpDays: Int): Double = {
    val lengthSnapshots = dSorted.length
    val y = findNonZeroElement(dSorted, maxLookUpDays)
    y match {
      case None => 0.0 // this one can be debated to default to some other value
    	case Some(x: Int) => scala.math.pow((dSorted(lengthSnapshots-1).confirmed.toDouble/dSorted(lengthSnapshots-x-1).confirmed.toDouble),(1.0/x.toDouble))
    }
  }
  

  def alphaEstimate(csSorted: List[CovidSnapshot], maxLookUpDays: Int): Double = {
    val lengthSnapshots = csSorted.length
    val y = findNonZeroElement(csSorted, maxLookUpDays)
    y match {
      case None => 0.0 // this one can be debated to default to some other value
    	case Some(x: Int) => (scala.math.log(csSorted(lengthSnapshots-1).confirmed.toDouble)-scala.math.log(csSorted(lengthSnapshots-x-1).confirmed.toDouble))/x.toDouble
    }
  }
  
  def stringForDays(days: Int): String = {
    
    days == 1 match {
      case true => "daily"
      case false => {
        days/7 match {
          case 1 => "weekly"
          case 2 => "biweekly"
          case 3 => "triweekly"
          case _ => "not supported"
        }
      }
    }
  }
  /**
   * This takes in daily and cumulative CovidSnapshots for a specific location and upto a specific date
   * The caller has the responsibility to provide continuous data at this point so no date is missing
   * between the min and the max date for the snapshots
   */
  
  def createAnnotation(dailySnapshots: CovidSnapshots, cumulativeSnapshots: CovidSnapshots,  
                     alphaWindow: Int, growthWindow: Int, movingAverageWindows: List[Int]): List[Annotation] = {
    
     
     
    val daysToString = movingAverageWindows.map(x => (x, stringForDays(x))).toMap
    val province = dailySnapshots.snapshots(0).province_state
    val country = dailySnapshots.snapshots(0).country
    val lat = dailySnapshots.snapshots(0).lat
    val long = dailySnapshots.snapshots(0).long
    val datesSorted = dailySnapshots.snapshots.map(dS => dS.date).distinct.map(dSS => (dSS, HelperFunctions.getDate(dSS))).sortWith((a,b) => a._2.before(b._2))
    
    val dateMin = datesSorted(0)
    val dateMax = datesSorted.last
    
    val diffDays = HelperFunctions.daysBetween(dateMin._2, dateMax._2)
    val mapIndexToAnnotationMetrics = scala.collection.mutable.Map[Int, (Double, Double, Double, Double, Double, Double)]()
    val mapIndexToAnnotationAverageMetrics = scala.collection.mutable.Map[Int, (Array[(String,Double)], 
                                                                                Array[(String,Double)], 
                                                                                Array[(String,Double)],
                                                                                Array[(String,Double)], 
                                                                                Array[(String,Double)],
                                                                                Array[(String,Double)])]()
    val cumulativeSnapshotsSorted = sortSnapshots(cumulativeSnapshots.snapshots)
    val dailySnapshotsSorted = sortSnapshots(dailySnapshots.snapshots)
    
    Range(0,diffDays.toInt+1).foreach(i => {
      val dt = datesSorted(i)._2
      val dtString = datesSorted(i)._1
      // check if there are any snapshots for this loc,dt combination
      val currentCumulativeSnapshot = cumulativeSnapshotsSorted(i)
      val currentDailySnapshot = dailySnapshotsSorted(i)
      val cumulConfirmed = currentCumulativeSnapshot.confirmed.toDouble
      val cumulDeaths = currentCumulativeSnapshot.deaths.toDouble
      val cumulativeDeathRate = cumulConfirmed > 0.0 match {
        case true => (cumulDeaths/cumulConfirmed)
        case false => 0.0
      }
      val matchesFoundDaily = dailySnapshots.snapshots.filter(ds => HelperFunctions.getDate(ds.date).before(dt))
      
//      println("I " + cumulativeSnapshotsSorted.slice(0,i+1).length)
      
      val estimatedAlpha = alphaEstimate(cumulativeSnapshotsSorted.slice(0,i+1),alphaWindow) 
      
      val growthRateDaily =  dailyGrowthEstimate(matchesFoundDaily, growthWindow)
      
      val epidemicControlDaily = matchesFoundDaily.length>0 match {
        case true => {
          InputOutput.countryPopulationMap.contains(country) match {
            case true => matchesFoundDaily.last.confirmed.toDouble/InputOutput.countryPopulationMap(country)._2
            case false => 0.0
          }
        }
        case false => 0.0
      }
      mapIndexToAnnotationMetrics.+=(i -> (currentDailySnapshot.confirmedNormalized.toDouble, currentCumulativeSnapshot.confirmedNormalized.toDouble, estimatedAlpha, cumulativeDeathRate,growthRateDaily, epidemicControlDaily))
    })
    // now construct the moving averages from the values in the map

    
    // Let's create the mean over the right window sizes
    Range(0,diffDays.toInt+1).foreach(j => {
      
          val listCumulativeConfirmedMA = scala.collection.mutable.ListBuffer[(String, Double)](("daily",mapIndexToAnnotationMetrics(j)._1))
          val listCumulativeConfirmedPCMA = scala.collection.mutable.ListBuffer[(String, Double)](("daily",mapIndexToAnnotationMetrics(j)._2))
        	val listEstimatedAlphaMA = scala.collection.mutable.ListBuffer[(String, Double)](("daily",mapIndexToAnnotationMetrics(j)._3))
    			val listCumulativeDeathRateMA = scala.collection.mutable.ListBuffer[(String, Double)](("daily",mapIndexToAnnotationMetrics(j)._4))
    			val listGrowthRateDailyMA = scala.collection.mutable.ListBuffer[(String, Double)](("daily",mapIndexToAnnotationMetrics(j)._5))
    			val listEpidemicControlRatioMA = scala.collection.mutable.ListBuffer[(String, Double)](("daily",mapIndexToAnnotationMetrics(j)._6))
      movingAverageWindows.foreach(m => {
     
      val windowLeftIndex = scala.math.max(0,j-m)
      val movingAverageRange = Range(windowLeftIndex,j).toList
      val movingAverages = movingAverageRange.map(k => mapIndexToAnnotationMetrics.get(k).get).foldLeft((0.0, 0.0, 0.0,0.0,0.0,0.0))((a,b) => (a._1+b._1,a._2+b._2,a._3+b._3, a._4+b._4,a._5+b._5,a._6+b._6))
      
      listCumulativeConfirmedMA.+=((daysToString(m),(movingAverages._1/m.toDouble)))
      listCumulativeConfirmedPCMA.+=((daysToString(m),(movingAverages._2/m.toDouble)))
      listEstimatedAlphaMA.+=((daysToString(m),(movingAverages._3/m.toDouble)))
      listCumulativeDeathRateMA.+=((daysToString(m),(movingAverages._4/m.toDouble)))
      listGrowthRateDailyMA.+=((daysToString(m), (movingAverages._5/m.toDouble)))
      listEpidemicControlRatioMA.+=((daysToString(m), (movingAverages._6/m.toDouble)))
      })
      mapIndexToAnnotationAverageMetrics.+=(j-> (listCumulativeConfirmedMA.toArray,
                                                 listCumulativeConfirmedPCMA.toArray,
                                                 listEstimatedAlphaMA.toList.toArray,
                                                 listCumulativeDeathRateMA.toList.toArray,
                                                 listGrowthRateDailyMA.toList.toArray,
                                                 listEpidemicControlRatioMA.toList.toArray))
    })
    
    // now combine them into the annotation object
    Range(0,diffDays.toInt+1).toList.map(l => {
      val map2Value = mapIndexToAnnotationAverageMetrics.get(l).get
      Annotation(datesSorted(l)._1, province, country, lat, long, map2Value._1, map2Value._2, map2Value._3,map2Value._4,map2Value._5,map2Value._6)
    })      
    
  }    

  /**
   * Just a clone
   */
  def deepCopyCovidSnapshot(covidSnapshot: CovidSnapshot): CovidSnapshot = {
    CovidSnapshot(covidSnapshot.date, covidSnapshot.province_state, covidSnapshot.country, covidSnapshot.lat, covidSnapshot.long,
                    covidSnapshot.confirmed,covidSnapshot.confirmedNormalized,
                    covidSnapshot.recovered,covidSnapshot.recoveredNormalized,
                    covidSnapshot.active,covidSnapshot.activeNormalized,
                    covidSnapshot.deaths,covidSnapshot.deathsNormalized,covidSnapshot.epidemiccontrolThreshold, 
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
   ChronoUnit.DAYS.between(LocalDate.of(date1.getYear, date1.getMonth+1, date1.getDate), LocalDate.of(date2.getYear, date2.getMonth+1, date2.getDate))
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
  
  def sameLoc(loc1: GeoLocation, loc2: GeoLocation) = equals(loc1, loc2)
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
  def filter(covidSnapshots:CovidSnapshots,loc: GeoLocation): CovidSnapshots = 
    CovidSnapshots(covidSnapshots.snapshots.filter(x => equals((x.lat, x.long),loc)))
  
    
}
