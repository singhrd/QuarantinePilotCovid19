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
import quarantine.covid19.constants.Constants
import quarantine.covid19.constants.MetricType
import quarantine.covid19.core.Alert



object HelperFunctions {
  
  // We are using this format for the Date string in the CovidSnapshots
	val format = new SimpleDateFormat("MM/dd/yyyy");
	
	val defaultAnnotationMapArray: Array[(String, Double)] = Array[(String, Double)](("daily", 0.0),("weekly", 0.0),("triweekly",0.0))
	// Millis in a day
	
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
	
  def pivotLineMultipleColumnsCSVJoined(lineRecords: List[List[String]], col1: Int, col2: Int): Map[String, List[List[String]]] = {
    val mapLocaleToLines = scala.collection.mutable.Map[String, List[List[String]]]()
    lineRecords.foreach(lineElements => {
      val localeCurrent = lineElements(col1) + ","+ lineElements(col2)
      val currentLinesThisLocale = mapLocaleToLines.getOrElse(localeCurrent, List[List[String]]())
      mapLocaleToLines.+=((localeCurrent,currentLinesThisLocale ++ List(lineElements)))
    })
   mapLocaleToLines.toMap
  }  

  
  def pivotCovidSnapshotByLocale(snapshots: List[CovidSnapshot]): Map[String, CovidSnapshots] = {
    val mapLocaleToSnapshots = scala.collection.mutable.Map[String, List[CovidSnapshot]]()
    snapshots.foreach(element => {
      val localeCurrent = element.locale
      val currentSnapshotThisLocale = mapLocaleToSnapshots.getOrElse(localeCurrent, List[CovidSnapshot]())
      mapLocaleToSnapshots.+=((localeCurrent,currentSnapshotThisLocale++List(element)))
    })
   mapLocaleToSnapshots.toMap.map(x => (x._1, CovidSnapshots(x._2)))
  }  
  
  def pivotAnnotationsByLocale(annotations: List[Annotation]): Map[String, List[Annotation]] = {
    val mapLocaleToAnnotations = scala.collection.mutable.Map[String, List[Annotation]]()
    annotations.foreach(element => {
      val localeCurrent = element.locale
      val currentAnnotationsThisLocale = mapLocaleToAnnotations.getOrElse(localeCurrent, List[Annotation]())
      mapLocaleToAnnotations.+=((localeCurrent,currentAnnotationsThisLocale++List(element)))
    })
   mapLocaleToAnnotations.toMap
  }

  
	/**
	 * In case of a daily snapshot missing, we can jumpstart with the default snapshot with 0 baseline
	 */
  def createDefaultCovidSnapshot(date: String, locale: String, epidemicControlThreshold: Double, loc: GeoLocation): CovidSnapshot = 
     CovidSnapshot(date, locale, loc.lat, loc.long, 0L, 0.0, 0.0, 0L, 0.0, 0L, 0.0, 0L, 0.0, epidemicControlThreshold, "HelperFunction",None)

     
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
      
      CovidSnapshot(daily.date, daily.locale, daily.lat, daily.long,
                    daily.confirmed+cumulative.confirmed,
                    daily.confirmedPer100k+cumulative.confirmedPer100k,
                    daily.confirmedPer100kN+cumulative.confirmedPer100kN,
                    daily.recovered+cumulative.recovered,
                    daily.recoveredPer100k+cumulative.recoveredPer100k,
                    // may be make more formal pattern check to assign None to active when None in both
                    daily.active+cumulative.active,
                    daily.activePer100k+cumulative.activePer100k,
                    daily.deaths+cumulative.deaths,
                    daily.deathsPer100k+cumulative.deathsPer100k,
                    cumulative.epidemiccontrolThreshold,
                    daily.source, combine(daily.tests, cumulative.tests))
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
  def createAnnotation(dailySnapshots: CovidSnapshots, 
                       cumulativeSnapshots: CovidSnapshots,  
                       metrics: List[MetricType.Value]=Constants.defaultMetrics,
                       alphaWindow: Int = Constants.DefaultDeltaInDays, 
                       growthWindow: Int = Constants.DefaultDeltaInDays,
                       movingAverageWindows: List[Int] = Constants.DefaultMovingAverageWindowInDaysSet): Option[List[Annotation]] = {
    
    val daysToString = movingAverageWindows.map(x => (x, stringForDays(x))).toMap
    
    val (locale, lat, long) = (dailySnapshots.snapshots(0).locale, dailySnapshots.snapshots(0).lat, dailySnapshots.snapshots(0).long)

    // we can use the sortSnapshots on this and then transform to the string, date form
    val datesSorted = dailySnapshots.snapshots.map(dS => dS.date).distinct.map(dSS => (dSS, HelperFunctions.getDate(dSS))).sortWith((a,b) => a._2.before(b._2))
    
    val (dateMin, dateMax) = (datesSorted(0), datesSorted.last)
    val diffDays = HelperFunctions.daysBetween(dateMin._2, dateMax._2)

    val mapIndexToAnnotationMetrics = scala.collection.mutable.Map[Int, Map[MetricType.Value, Double]]()
  
    // is this not guaranteed already // if so this is a huge savings
    diffDays > cumulativeSnapshots.snapshots.length match {
      case true => {
      	println("There is a gap in data for this location " + locale + "-" + diffDays + "-"+ cumulativeSnapshots.snapshots  .length)
      	return None
      }
    
      case false => {
//        println("Working on " + locale)
    	  val (cumulativeSnapshotsSorted, dailySnapshotsSorted) = //(cumulativeSnapshots.snapshots, dailySnapshots.snapshots)
     			  (sortSnapshots(cumulativeSnapshots.snapshots), sortSnapshots(dailySnapshots.snapshots))
    
        val result = Range(0,cumulativeSnapshotsSorted.length.toInt).toList.map(i => {
    //      val currentMetricValues = getMetricValues(dailySnapshotsSorted, cumulativeSnapshotsSorted, i)
          
          val currentMetricValues = metrics.map(metric => {
          val metricValue = metric match {
          case MetricType.SpreadRate => alphaEstimate(cumulativeSnapshotsSorted.slice(0,i+1),alphaWindow)
          case MetricType.DailyGrowth => dailyGrowthEstimate(dailySnapshotsSorted.slice(0,i+1), growthWindow)
          case MetricType.CFR => {
            val cumulConfirmed = cumulativeSnapshotsSorted(i).confirmed.toDouble
            val cumulDeaths = cumulativeSnapshotsSorted(i).deaths.toDouble
            cumulConfirmed > 0.0 match {
              case true => (cumulDeaths/cumulConfirmed)
              case false => 0.0
            }
          }
          case MetricType.CFRTA => {
            val cumulDeaths = cumulativeSnapshotsSorted(i).deaths.toDouble
            val CFRTimeAdjustedIndex = scala.math.max(i-Constants.DefaultOutcomeTimeDays,0)
            val cumulConfirmedTimeDelayed = cumulativeSnapshotsSorted(CFRTimeAdjustedIndex).confirmed.toDouble
            cumulConfirmedTimeDelayed > 0.0 match {
              case true => (cumulDeaths/cumulConfirmedTimeDelayed)
              case false => 0.0
            }
          }
          case MetricType.CumulativePer100k => cumulativeSnapshotsSorted(i).confirmedPer100k
          case MetricType.DailyPer100k => dailySnapshotsSorted(i).confirmedPer100k
        }
          (metric -> metricValue)
        }).toMap
        
          mapIndexToAnnotationMetrics.+=(i -> currentMetricValues)
          val mapAnnotation = scala.collection.mutable.Map[String, Array[(String, Double)]]()
          metrics.foreach(metric => mapAnnotation.+=(MetricType.name(metric) -> Array[(String, Double)](("daily",currentMetricValues(metric)))))
        	movingAverageWindows.foreach(m => {
            val windowLeftIndex = scala.math.max(0,i-m)
            val movingAverageRange = Range(windowLeftIndex,i).toList
            val windowSize = m > i match {
              case true => (i+1).toDouble
              case false => m.toDouble
            }
            val dailyMetricValues = movingAverageRange.map(k => mapIndexToAnnotationMetrics.get(k).get) 
            val sumMetricValues = metrics.map(metric => (MetricType.name(metric) -> dailyMetricValues.foldLeft(0.0)((a,b) => a + b.get(metric).get))).toMap
          
            sumMetricValues.foreach(x => {
                  val currentArray = mapAnnotation(x._1)
                  mapAnnotation.+=(x._1 -> (currentArray ++ Array((daysToString(m), x._2/windowSize))))
            })
        	})
          Annotation(datesSorted(i)._1, locale, lat, long, mapAnnotation.toMap)
        })    
        Some(result)     
      }
    }

  }    

  /**
   * Just a clone
   */
  def deepCopyCovidSnapshot(covidSnapshot: CovidSnapshot): CovidSnapshot = {
    CovidSnapshot(covidSnapshot.date, covidSnapshot.locale, covidSnapshot.lat, covidSnapshot.long,
                    covidSnapshot.confirmed,covidSnapshot.confirmedPer100k,covidSnapshot.confirmedPer100kN,
                    covidSnapshot.recovered,covidSnapshot.recoveredPer100k,
                    covidSnapshot.active,covidSnapshot.activePer100k,
                    covidSnapshot.deaths,covidSnapshot.deathsPer100k,covidSnapshot.epidemiccontrolThreshold, 
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
  
  def before(dateStringToCheck: String, dateStringReference: String): Boolean = {
    val dateToCheck = getDate(dateStringToCheck)
    val dateReference = getDate(dateStringReference)
    dateToCheck.before(dateReference)
  }
  /**
   * This might need to be replaced with something that is not deprecated
   */
  def daysBetween(date1: Date, date2: Date): Long = {
   ChronoUnit.DAYS.between(LocalDate.of(date1.getYear, date1.getMonth+1, date1.getDate), LocalDate.of(date2.getYear, date2.getMonth+1, date2.getDate))
  }
  
  /**
   * "MM/dd/yyyy"
   */
  def getDateStringFromDate(date: Date): String = format.format(date)
  
  /**
   * Find the Date days before the given date
   */
  def dateDaysAfter(date: Date, days: Long): Date = {
    new Date(date.getTime() + (days * Constants.DaysInMs))
  }
  
  /**
   * Find the Date days before the given date
   */
  def dateDaysBefore(date: Date, days: Long): Date = dateDaysAfter(date, -days)
  
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
   * Ideally, would like to only use the county name if it is unique to a state
   * If the same name appears in multiple states, we would like to include the State name to 
   * fully qualify it
   * 
   * The method looks through the entire list of county,state to determine whether to 
   * keep the state in the qualified name
   */
  def createCountyNameLookUpMap(countyStateNamesCommaSeparated: List[String]): Map[String, String] = {
    val mapLookUp = scala.collection.mutable.Map[String, (String, Boolean)]()
    countyStateNamesCommaSeparated.foreach(countyStateNamePair => {
      val nameElements = countyStateNamePair.split(",")
      val (countyName, stateName) = (nameElements(0), nameElements(1))
      mapLookUp.contains(countyStateNamePair) match {
        case false =>  mapLookUp.+=((countyStateNamePair, (nameElements(0), true)))
        case true => {
          // check if we have already found it to be non-unique - key exists means it has already been seen once with some other state
          mapLookUp(countyStateNamePair)._2 match {
            case false => // do nothing already found a match
            case true => // found a second match reset the key to false
              {
                mapLookUp.+=((countyStateNamePair, (countyStateNamePair, false)))
              }
          }
        }
      }
    })
    return mapLookUp.toMap.map(y => (y._1,y._2._1))
  }
  
  /**
   * Find the set from the a list of geo-locations - essentially the unique/distinct elements
   */
  def distinct(locs: List[GeoLocation]): List[GeoLocation] = locs.toSet.toList
  
  
  /**
   * Filter the CovidSnapshots for a given location loc only
   */
  def filter(covidSnapshots:CovidSnapshots,loc: GeoLocation): CovidSnapshots = 
    CovidSnapshots(covidSnapshots.snapshots.filter(x => equals((x.lat, x.long),loc)))
  
 
/**
   * Assume all the files have same header format
   */
  def createSnapshots(tokensPerRecordConfirmed:List[List[String]],
                      tokensPerRecordRecovered:List[List[String]],
                      tokensPerRecordDeaths:List[List[String]],
                      localeMap: Map[String, (String, GeoLocation)]): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    
    val covidSnapshotListBufferDaily = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    val covidSnapshotListBufferCumul = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    
    val headers = tokensPerRecordConfirmed(0)
    val localeIndex = 1 // headers.indexOf("Country"+"\\/\\"+"Region")
    val latIndex = headers.indexOf("Lat")
    val longIndex = headers.indexOf("Long")
    val firstDateIndex = 4//headers.indexOf("1"+"\\/\\"+"20"+"\\/\\"+"2020")
    val lastDateIndex = headers.length
    
    // Check if need to escape the "/"
    val localeList = tokensPerRecordConfirmed.slice(1,tokensPerRecordConfirmed.length).map(l => {
      l(localeIndex)}).distinct
    
    // The following calls line up everything per the index extracted from the headers by default
    val mapConfirmed = getNormalizedLocaleRecords(localeList, localeIndex, tokensPerRecordConfirmed.slice(1, tokensPerRecordConfirmed.length))
    val mapRecovered = getNormalizedLocaleRecords(localeList, localeIndex, tokensPerRecordRecovered.slice(1, tokensPerRecordRecovered.length))
    val mapDeaths = getNormalizedLocaleRecords(localeList, localeIndex, tokensPerRecordDeaths.slice(1, tokensPerRecordDeaths.length))
    
    // now combine these into cumulative CovidSnapshots
    localeList.foreach(c => {
      InputOutput.countryLocaleMap.contains(c) match {
        case true => {
          val countryMapValue = InputOutput.countryLocaleMap(c)
          val (normalizerCountry, ecCountry) = InputOutput.countryPopulationECMap.contains(c) match {
            case true => (InputOutput.countryPopulationECMap(c)._1, InputOutput.countryPopulationECMap(c)._2)
            case false => {
              println("** missing key " + c)
              (1.0,0.5)
            }
          }
          val normalizerCountryPD = InputOutput.countryPopulationDensityMap.contains(c) match {
            case true => InputOutput.countryPopulationDensityMap(c)
            case false => {
              println("** missing key " + c)
              1.0
            }
          }          
          
          val (confirmedCumul, confirmedDaily) = combineCountryRecords(mapConfirmed(c), firstDateIndex, c)
          val (recoveredCumul, recoveredDaily) = combineCountryRecords(mapRecovered(c), firstDateIndex, c)
          val (deathsCumul, deathsDaily) = combineCountryRecords(mapDeaths(c), firstDateIndex, c)
        // Now let's create the daily and the cumulative snapshots 
        Range(0, lastDateIndex-firstDateIndex).foreach(i => {
          
           val activeDaily = confirmedDaily(i)-deathsDaily(i)-recoveredDaily(i)
           covidSnapshotListBufferDaily.+=(CovidSnapshot(headers(i+firstDateIndex), 
                                                         c, 
                                                         countryMapValue._2.lat.toString(), 
                                                         countryMapValue._2.long.toString(),
                                                         confirmedDaily(i), confirmedDaily(i).toDouble/normalizerCountry,
                                                         confirmedDaily(i).toDouble/(normalizerCountry*normalizerCountryPD),
                                                         recoveredDaily(i),recoveredDaily(i).toDouble/normalizerCountry,
                                                         activeDaily, activeDaily.toDouble/normalizerCountry,// daily active does not make much sense
                                                         deathsDaily(i),deathsDaily(i).toDouble/normalizerCountry,
                                                         ecCountry,
                                                         "JHU",
                                                         None))
          val activeCumulative = confirmedCumul(i)-deathsCumul(i)-recoveredCumul(i)                                                    
          covidSnapshotListBufferCumul.+=(CovidSnapshot(headers(i+firstDateIndex), 
                                                         c, 
                                                         countryMapValue._2.lat.toString(), 
                                                         countryMapValue._2.long.toString(),
                                                         confirmedCumul(i),confirmedCumul(i).toDouble/normalizerCountry,
                                                         confirmedCumul(i).toDouble/(normalizerCountry*normalizerCountryPD),
                                                         recoveredCumul(i),recoveredCumul(i).toDouble/normalizerCountry,
                                                         activeCumulative, activeCumulative.toDouble/normalizerCountry, 
                                                         deathsCumul(i),deathsCumul(i).toDouble/normalizerCountry,
                                                         ecCountry,
                                                         "JHU",
                                                         None))
                                                         
                                                         
          })
      }
      case false => println("Skipping *** " + c)// skip it 
      }
        
    })
   
    val dailyCS = CovidSnapshots(covidSnapshotListBufferDaily.toList)
    
    val cumulCS = CovidSnapshots(covidSnapshotListBufferCumul.toList)
    
    val countryCovidSnapshotMapDaily = HelperFunctions.pivotCovidSnapshotByLocale(dailyCS.snapshots)
    
    val countryCovidSnapshotMapCumul = HelperFunctions.pivotCovidSnapshotByLocale(cumulCS.snapshots)
    
    (dailyCS,cumulCS,countryCovidSnapshotMapDaily,countryCovidSnapshotMapCumul)
  }
  
    
  def transformDateToSlashFormat(dateInHyphenFormat: String): String = {
    val elementsDate = dateInHyphenFormat.split("-").toList
    elementsDate(1)+"/"+elementsDate(2)+"/"+elementsDate(0)
  }
  
  /**
   * Assume all the files have same header format
   */
  def createSnapshotsCounty(tokensPerRecordRaw:List[List[String]]):
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    
    val covidSnapshotListBufferDaily = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    val covidSnapshotListBufferCumul = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    
    val localeCountyDateIndex = 0
    val localeCountyIndex = 1
    val localeStateIndex = 2
    val confirmedCountIndex = 4
    val deathsCountIndex = 5  
  
    val tokensPerRecord = tokensPerRecordRaw.slice(1,tokensPerRecordRaw.length).map(l => List(transformDateToSlashFormat(l(0))) ++ l.slice(1,3) ++ l.slice(4,6))
  
    
    val localeListCounty = tokensPerRecord.slice(1,tokensPerRecord.length).map(l => (l(localeCountyIndex)+","+l(localeStateIndex))).distinct

    val countyRecordsMap = pivotLineMultipleColumnsCSVJoined(tokensPerRecord, localeCountyIndex, localeStateIndex) 
      
      //localeListCounty.map(l => (l -> tokensPerRecord.filter(ll => (ll(localeCountyIndex)+","+ll(localeStateIndex)).equals(l)))).toMap
    countyRecordsMap.foreach(c => {
          val cKey = c._1.toLowerCase()
          val (normalizerCounty, ecCounty) = InputOutput.countyPopulationECMap.contains(cKey) match {
            case true => (InputOutput.countyPopulationECMap(cKey)._1, InputOutput.countyPopulationECMap(cKey)._2)
            case false => {
              println("** missing key " + c._1)
              (1.0,0.5)
            }
          }
          val normalizerCountyPD = 1.0 // replace when you have the population densities
          
          val (confirmedCumul, confirmedDaily) = combineCountyRecords(countyRecordsMap(c._1), confirmedCountIndex-1)
          val (deathsCumul, deathsDaily) = combineCountyRecords(countyRecordsMap(c._1), deathsCountIndex-1)
        // Now let's create the daily and the cumulative snapshots 
        Range(0, confirmedCumul.length).foreach(i => {
          
           val activeDaily = confirmedDaily(i)._2-deathsDaily(i)._2-0L
           covidSnapshotListBufferDaily.+=(CovidSnapshot(confirmedDaily(i)._1, 
                                                         c._1, 
                                                         Constants.DefaultLat+c._1, 
                                                         Constants.DefaultLong+c._1,
                                                         confirmedDaily(i)._2, confirmedDaily(i)._2.toDouble/normalizerCounty,
                                                         confirmedDaily(i)._2.toDouble/(normalizerCounty*normalizerCountyPD),
                                                         0L,0.0,
                                                         activeDaily, activeDaily.toDouble/normalizerCounty,// daily active does not make much sense
                                                         deathsDaily(i)._2,deathsDaily(i)._2.toDouble/normalizerCounty,
                                                         ecCounty,
                                                         "NYGithub",
                                                         None))
          val activeCumulative = confirmedCumul(i)._2-deathsCumul(i)._2-0L  // using recovery is 0 without any additional info but we won't be using these snapshots                                                  
          covidSnapshotListBufferCumul.+=(CovidSnapshot(confirmedCumul(i)._1, 
                                                         c._1, 
                                                         Constants.DefaultLat+c._1, 
                                                         Constants.DefaultLong+c._1,
                                                         confirmedCumul(i)._2,confirmedCumul(i)._2.toDouble/normalizerCounty,
                                                         confirmedCumul(i)._2.toDouble/(normalizerCounty*normalizerCountyPD),
                                                         0L,0.0,
                                                         activeCumulative, activeCumulative.toDouble/normalizerCounty, 
                                                         deathsCumul(i)._2,deathsCumul(i)._2.toDouble/normalizerCounty,
                                                         ecCounty,
                                                         "NYGithub",
                                                         None))
                                                         
                                                         
          })
    })
    
    val dailyCS = CovidSnapshots(covidSnapshotListBufferDaily.toList)
    
    val cumulCS = CovidSnapshots(covidSnapshotListBufferCumul.toList)
    
    
    val countyCovidSnapshotMapDaily = HelperFunctions.pivotCovidSnapshotByLocale(dailyCS.snapshots)
    
    val countyCovidSnapshotMapCumul = HelperFunctions.pivotCovidSnapshotByLocale(cumulCS.snapshots)
    
    (dailyCS,cumulCS,countyCovidSnapshotMapDaily,countyCovidSnapshotMapCumul)
  }

  /**
   * Assume all the files have same header format
   */
  def createSnapshotsState(tokensPerRecordRaw:List[List[String]]): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    
    val covidSnapshotListBufferDaily = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    val covidSnapshotListBufferCumul = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    
    val localeCountyDateIndex = 0
    val localeStateIndex = 2
    val confirmedCountIndex = 4
    val deathsCountIndex = 5  

  
    
    val tokensPerRecordMap = scala.collection.mutable.Map[(String, String), List[(String, String)]]()
    
    tokensPerRecordRaw.slice(1,tokensPerRecordRaw.length).foreach(l => {
        val dateKey = transformDateToSlashFormat(l(0))
        tokensPerRecordMap.contains((dateKey,l(2))) match {
          case false => tokensPerRecordMap.+=((dateKey, l(2)) -> List((l(confirmedCountIndex), l(deathsCountIndex))))
          case true => {
            val currentValue = tokensPerRecordMap((dateKey, l(2)))
            tokensPerRecordMap.+=((dateKey, l(2)) -> (currentValue ++ List((l(confirmedCountIndex), l(deathsCountIndex)))))
          }
        }
      })
      
      
      val tokensPerRecord = tokensPerRecordMap.toMap.map(x => {
        val dateString = x._1._1
        val stateKey = x._1._2
        val sumConfirmedDeaths = x._2.foldLeft((0L,0L))((a,b) => (a._1+b._1.toLong, a._2+b._2.toLong))
        List(dateString, stateKey, sumConfirmedDeaths._1.toString(), sumConfirmedDeaths._2.toString())
      }).toList
  
    
    val localeListState = tokensPerRecord.map(l => l(localeStateIndex-1)).distinct

    val stateRecordsMap = localeListState.map(l => (l -> tokensPerRecord.filter(ll => ll(localeStateIndex-1).equals(l)))).toMap
    
    stateRecordsMap.foreach(c => {
          val (normalizerState, ecState) = InputOutput.statePopulationECMap.contains(c._1) match {
            case true => (InputOutput.statePopulationECMap(c._1)._1, InputOutput.statePopulationECMap(c._1)._2)
            case false => {
              println("** missing key " + c._1)
              (1.0,0.5)
            }
          }
          val normalizerStatePD = 1.0 // replace when you have the population densities
          
          val (confirmedCumul, confirmedDaily) = combineCountyRecords(stateRecordsMap(c._1), confirmedCountIndex-2)
          val (deathsCumul, deathsDaily) = combineCountyRecords(stateRecordsMap(c._1), deathsCountIndex-2)
        // Now let's create the daily and the cumulative snapshots 
        Range(0, confirmedCumul.length).foreach(i => {
          
           val activeDaily = confirmedDaily(i)._2-deathsDaily(i)._2-0L
           covidSnapshotListBufferDaily.+=(CovidSnapshot(confirmedDaily(i)._1, 
                                                         c._1, 
                                                         Constants.DefaultLat+c._1, 
                                                         Constants.DefaultLong+c._1,
                                                         confirmedDaily(i)._2, confirmedDaily(i)._2.toDouble/normalizerState,
                                                         confirmedDaily(i)._2.toDouble/(normalizerState*normalizerStatePD),
                                                         0L,0.0,
                                                         activeDaily, activeDaily.toDouble/normalizerState,// daily active does not make much sense
                                                         deathsDaily(i)._2,deathsDaily(i)._2.toDouble/normalizerState,
                                                         ecState,
                                                         "NYGithub",
                                                         None))
          val activeCumulative = confirmedCumul(i)._2-deathsCumul(i)._2-0L  // using recovery is 0 without any additional info but we won't be using these snapshots                                                  
          covidSnapshotListBufferCumul.+=(CovidSnapshot(confirmedCumul(i)._1, 
                                                         c._1, 
                                                         Constants.DefaultLat+c._1, 
                                                         Constants.DefaultLong+c._1,
                                                         confirmedCumul(i)._2,confirmedCumul(i)._2.toDouble/normalizerState,
                                                         confirmedCumul(i)._2.toDouble/(normalizerState*normalizerStatePD),
                                                         0L,0.0,
                                                         activeCumulative, activeCumulative.toDouble/normalizerState, 
                                                         deathsCumul(i)._2,deathsCumul(i)._2.toDouble/normalizerState,
                                                         ecState,
                                                         "NYGithub",
                                                         None))
                                                         
                                                         
          })
    })
    
    val dailyCS = CovidSnapshots(covidSnapshotListBufferDaily.toList)
    
    val cumulCS = CovidSnapshots(covidSnapshotListBufferCumul.toList)
    
    
    val countyCovidSnapshotMapDaily = HelperFunctions.pivotCovidSnapshotByLocale(dailyCS.snapshots)
    
    val countyCovidSnapshotMapCumul = HelperFunctions.pivotCovidSnapshotByLocale(cumulCS.snapshots)
    
    (dailyCS,cumulCS,countyCovidSnapshotMapDaily,countyCovidSnapshotMapCumul)
  }
  
  def extractDailyFromCumulativeCounty(cumul:List[(String, Long)]): List[(String, Long)] = {
    val cumulOneStepMoved = cumul.slice(1,cumul.length)
    List(cumul(0)) ++ cumulOneStepMoved.zip(cumul.slice(0,cumul.length-1)).map(x => (x._1._1, x._1._2-x._2._2))
  }

  
  def extractDailyFromCumulative(cumul:List[Long]): List[Long] = {
    val cumulOneStepMoved = cumul.slice(1,cumul.length)
    List(cumul(0)) ++ cumulOneStepMoved.zip(cumul.slice(0,cumul.length-1)).map(x => x._1-x._2)
  }

    // replace the province with the capital name or default and use the country lat lon from the map
  def combineCountyRecords(records: List[List[String]], dataIndex: Int): (List[(String, Long)], List[(String, Long)]) = {
    val recordsCountValues = records.sortWith((a,b) => a(0)<b(0)).map(x => (x(0),x(dataIndex).toLong))
    val referenceDate = HelperFunctions.getDate(Constants.DefaultCountyRefDate)
    val startDate = HelperFunctions.getDate(recordsCountValues(0)._1)
    referenceDate.before(startDate) match {
      case true => {
        // add dummy annotations to the front
        val defaultFillerSnapshotValue = Range(0,HelperFunctions.daysBetween(referenceDate, startDate).toInt).toList.map(i => {
          (HelperFunctions.getDateStringFromDate(HelperFunctions.dateDaysAfter(referenceDate, i)),0L)
        }).toList
        
        val recordsCountValuesPadded = defaultFillerSnapshotValue ++ recordsCountValues
        (recordsCountValuesPadded,extractDailyFromCumulativeCounty(recordsCountValuesPadded))
      }
      case false => (recordsCountValues,extractDailyFromCumulativeCounty(recordsCountValues))
    }
    // now fill in the gap on the front since data only got recorded after the first case was reported
    
    
  }
  
    /**
   * This takes in daily and cumulative CovidSnapshots for a specific location and upto a specific date
   * The caller has the responsibility to provide continuous data at this point so no date is missing
   * between the min and the max date for the snapshots
   */
  
//  def getMetricValues(dailySnapshotsSorted: List[CovidSnapshot], 
//                     cumulativeSnapshotsSorted: List[CovidSnapshot],
//                     dateIndex: Int, 
//                     alphaWindow: Int = Constants.DefaultDeltaInDays,
//                     growthWindow: Int = Constants.DefaultDeltaInDays,
//                     metrics: List[MetricType.Value]=Constants.defaultMetrics): Map[MetricType.Value,Double] = {
//    
//    metrics.map(metric => {
//      val metricValue = metric match {
//      case MetricType.SpreadRate => alphaEstimate(cumulativeSnapshotsSorted.slice(0,dateIndex+1),alphaWindow)
//      case MetricType.DailyGrowth => dailyGrowthEstimate(dailySnapshotsSorted.slice(0,dateIndex+1), growthWindow)
//      case MetricType.CFR => {
//        val cumulConfirmed = cumulativeSnapshotsSorted(dateIndex).confirmed.toDouble
//        val cumulDeaths = cumulativeSnapshotsSorted(dateIndex).deaths.toDouble
//        cumulConfirmed > 0.0 match {
//          case true => (cumulDeaths/cumulConfirmed)
//          case false => 0.0
//        }
//      }
//      case MetricType.CFRTA => {
//        val cumulDeaths = cumulativeSnapshotsSorted(dateIndex).deaths.toDouble
//        val CFRTimeAdjustedIndex = scala.math.max(dateIndex-Constants.DefaultOutcomeTimeDays,0)
//        val cumulConfirmedTimeDelayed = cumulativeSnapshotsSorted(CFRTimeAdjustedIndex).confirmed.toDouble
//        cumulConfirmedTimeDelayed > 0.0 match {
//          case true => (cumulDeaths/cumulConfirmedTimeDelayed)
//          case false => 0.0
//        }
//      }
//      case MetricType.CumulativePer100k => cumulativeSnapshotsSorted(dateIndex).confirmedPer100k
//      case MetricType.DailyPer100k => dailySnapshotsSorted(dateIndex).confirmedPer100k
//    }
//      (metric -> metricValue)
//    }).toMap
//  }
  
  // replace the province with the capital name or default and use the country lat lon from the map
  def combineCountryRecords(records: List[List[String]], startDateIndex: Int, countryName: String): (List[Long], List[Long]) = {

    val recordsCountValues = records.map(x => x.slice(startDateIndex, x.length).map(y => y.toLong))
    records.length >1 match {
    case true =>
      {
        val initialList = Range(0,recordsCountValues(0).length).map(x => 0L)
        val cumulValues = recordsCountValues.foldLeft(initialList)((a,b) => a.zip(b).map(x => x._1 + x._2)).toList
        (cumulValues, extractDailyFromCumulative(cumulValues))
      }
    case false => (recordsCountValues(0),extractDailyFromCumulative(recordsCountValues(0)))
    }
    
  }
  /**
   * Pick the right size based on whether the country index was moved by 1 due to a comma in the province/state column
   */
  def getNormalizedLocaleRecords(localeList: List[String], localeIndex: Int, dataRecords: List[List[String]]): Map[String, List[List[String]]] = {
          // now filter per country and aggregate the data
          localeList.map(c => {
            val filteredCountryDefaultIndex = dataRecords.filter(rec => rec(localeIndex).equals(c))
            val filteredCountryMovedIndex = dataRecords.filter(rec => rec(localeIndex+1).equals(c))
            filteredCountryMovedIndex.length >0 match {
              case false => (c -> filteredCountryDefaultIndex)
              case true => {
                println("**** Found multiple records for this country " + c)
                (c -> (filteredCountryDefaultIndex ++ filteredCountryMovedIndex.map(x => List(x(0)+"-"+x(1)) ++ x.slice(2,x.length))))
              }
            }
          }).toMap
  }
  
  
  def transformCumulativeDataCountyNYFormat(fileNameCountyState: String, countyOrState: String="county"): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    countyOrState match {
      case "county" => createSnapshotsCounty(InputOutput.readCSV(fileNameCountyState).map(line => InputOutput.tokenize(line)))
      case "state" => createSnapshotsState(InputOutput.readCSV(fileNameCountyState).map(line => InputOutput.tokenize(line)))
    }
    
  }
  
  def transformCumulativeDataJHUFormat(fileNameConfirmed: String, fileNameRecovered: String, fileNameDeaths: String): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    createSnapshots(InputOutput.readCSV(fileNameConfirmed).map(line => InputOutput.tokenize(line)),
                    InputOutput.readCSV(fileNameRecovered).map(line => InputOutput.tokenize(line)),
                    InputOutput.readCSV(fileNameDeaths).map(line => InputOutput.tokenize(line)),InputOutput.countryLocaleMap)
  }  
}
