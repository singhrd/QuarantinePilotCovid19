package quarantine.covid19.util

/**
 * This is the driver object that reads in the data, creates Annotations and
 * daily and cumulative CovidSnapshots and writes them into json format
 * .
 * 
 * @author rajdeep
 * 
 */
import java.io._
import java.io.File
import java.io.FileOutputStream
import quarantine.covid19.core.CovidSnapshots
import scala.collection.mutable.ListBuffer
import quarantine.covid19.core.CovidSnapshot
import quarantine.covid19.core.Annotation
import quarantine.covid19.core.Annotations
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.core.JsonSupport

object InputOutput extends JsonSupport {
  
  
  val ONE_HUNDRED_THOUSAND = 100000.0
  val ONE_HUNDRED = 100.0
  
  val EPIDEMIC_CONTROL_THRESHOLD_PER_100K = 0.5
  
  val COMMA_DELIMITER = ","
  val DOUBLE_QUOTES = "\""

  val DEFAULT_DELIMITER = COMMA_DELIMITER
  val DEFAULT_LAT_COUNTY = "REPLACE_ME"
  val DEFAULT_LONG_COUNTY = "REPLACE_ME"
  
  val countryMapFileName = "../data/csv/general/concap.csv"
  val countryPopulationMapFileName = "../data/csv/general/populationcountry2020.csv"
  val countryPopulationDensityMapFileName = "../data/csv/general/populationDensityCountry.csv"
 
  val countyPopulationMapFileName = "../data/csv/general/populationCountyUS.csv"

  val countryLocaleMap = readCSV(countryMapFileName).map(x => {
    val elements = tokenize(x,Some(COMMA_DELIMITER))
    (elements(0) -> (elements(1),GeoLocation(elements(2),elements(3))))
  }).toMap
  
  
  val countryPopulationECMap= {
    val lines = readCSV(countryPopulationMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(COMMA_DELIMITER))
    (elements(0) -> (elements(1).toDouble/ONE_HUNDRED_THOUSAND, elements(1).toDouble*EPIDEMIC_CONTROL_THRESHOLD_PER_100K/ONE_HUNDRED_THOUSAND))
  }).toMap
  }
  
  val countyPopulationECMap= {
    val lines = readCSV(countyPopulationMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(COMMA_DELIMITER))
    (elements(1) -> (elements(2).toDouble/ONE_HUNDRED_THOUSAND, elements(2).toDouble*EPIDEMIC_CONTROL_THRESHOLD_PER_100K/ONE_HUNDRED_THOUSAND))
  }).toMap
  }
    
//    val countyPopulationMap= {
//    val lines = readCSV(countyPopulationMapFileName)
//    lines.slice(1,lines.length).map(x => {
//    val elements = tokenize(x,Some(COMMA_DELIMITER))
//    (elements(1) -> elements(2).toDouble)
//  }).toMap
//  }
    
  
  val countryPopulationDensityMap= {
    val lines = readCSV(countryPopulationDensityMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(COMMA_DELIMITER))
    (elements(0) -> (elements(2).toDouble/ONE_HUNDRED))
  }).toMap
  }
  
  def writeToFile(bytes: Array[Byte], filePath: String) = {
    
    val file = new File(filePath)
		val fos = new FileOutputStream(file);
    
      if (!file.exists()) {
	     file.createNewFile();
	  }
    
    fos.write(bytes)
    fos.flush()
    fos.close()
  }
  
  /**
   * Line delimited reading of a csv
   * The first element is the header line
   */
  def readCSV(filePath: String): List[String] = io.Source.fromFile(filePath).getLines().toList
  
  
  /**
   * Splitting a string/line into the elements using the delimiter 
   * If the delimiter is not provided, we use the CSV delimiter
   */
  def tokenize(line: String, delimiter: Option[String] = None): List[String] = {
    line.split(delimiter.getOrElse(DEFAULT_DELIMITER)).toList
  }
  

  /**
   * Assume all the files have same header format
   */
  def createSnapshots(tokensPerRecordConfirmed:List[List[String]],
                      tokensPerRecordRecovered:List[List[String]],
                      tokensPerRecordDeaths:List[List[String]],
                      localeMap: Map[String, (String, GeoLocation)]): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots],
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    
    val covidSnapshotListBufferDaily = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    val covidSnapshotListBufferCumul = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    
    val headers = tokensPerRecordConfirmed(0)
    println(headers)
    val localeIndex = 1 // headers.indexOf("Country"+"\\/\\"+"Region")
    val latIndex = headers.indexOf("Lat")
    val longIndex = headers.indexOf("Long")
//    val provinceStateIndex = 0 // headers.indexOf("Province"+"\\/\\"+"State")
    val firstDateIndex = 4//headers.indexOf("1"+"\\/\\"+"20"+"\\/\\"+"2020")
    val lastDateIndex = headers.length
    
    // Check if need to escape the "/"
    val localeList = tokensPerRecordConfirmed.slice(1,tokensPerRecordConfirmed.length).map(l => {
      l(0).startsWith(DOUBLE_QUOTES) match {
        case false => l(localeIndex)
        case true => l(localeIndex+1) // might need revision
      }
    }).distinct
    
    // The following calls line up everything per the index extracted from the headers by default
    val mapConfirmed = getNormalizedLocaleRecords(localeList, localeIndex, tokensPerRecordConfirmed.slice(1, tokensPerRecordConfirmed.length))
    val mapRecovered = getNormalizedLocaleRecords(localeList, localeIndex, tokensPerRecordRecovered.slice(1, tokensPerRecordRecovered.length))
    val mapDeaths = getNormalizedLocaleRecords(localeList, localeIndex, tokensPerRecordDeaths.slice(1, tokensPerRecordDeaths.length))
    
    // now combine these into cumulative CovidSnapshots
    localeList.foreach(c => {
      countryLocaleMap.contains(c) match {
        case true => {
          val countryMapValue = countryLocaleMap(c)
          val (normalizerCountry, ecCountry) = countryPopulationECMap.contains(c) match {
            case true => (countryPopulationECMap(c)._1, countryPopulationECMap(c)._2)
            case false => {
              println("** missing key " + c)
              (1.0,0.5)
            }
          }
          val normalizerCountryPD = countryPopulationDensityMap.contains(c) match {
            case true => countryPopulationDensityMap(c)
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
    
    val dateCovidSnapshotMapDaily = headers.slice(firstDateIndex, headers.length).map(dS => (dS -> HelperFunctions.pivotOnDate(dailyCS, dS))).toMap
    
    val dateCovidSnapshotMapCumul = headers.slice(firstDateIndex, headers.length).map(dS => (dS -> HelperFunctions.pivotOnDate(cumulCS, dS))).toMap
    
    val countryCovidSnapshotMapDaily = localeList.map(c => (c -> HelperFunctions.pivotOnCountry(dailyCS, c))).toMap
    
    val countryCovidSnapshotMapCumul = localeList.map(c => (c -> HelperFunctions.pivotOnCountry(cumulCS, c))).toMap
    
    (dailyCS,cumulCS,dateCovidSnapshotMapDaily,dateCovidSnapshotMapCumul,countryCovidSnapshotMapDaily,countryCovidSnapshotMapCumul)
  }
  
  def transformDateToSlashFormat(dateInHyphenFormat: String): String = dateInHyphenFormat.split("-").toList.reduceLeft(_+"/"+_)
  
  /**
   * Assume all the files have same header format
   */
  def createSnapshotsCounty(tokensPerRecordRaw:List[List[String]],
                      localeMap: Map[String, (String, GeoLocation)]): 
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
  
    
    val localeListCounty = tokensPerRecord.slice(1,tokensPerRecord.length).map(l => l(localeCountyIndex)).distinct
//    val localeListState = tokensPerRecord.slice(1,tokensPerRecord.length).map(l => l(localeStateIndex)).distinct
//      val localeList = localeListCounty ++ localeListState
    
      val countyRecordsMap = localeListCounty.map(l => (l -> tokensPerRecord.filter(ll => ll(localeCountyIndex).equals(l)))).toMap
//      localeListState.map(l => (l -> tokensPerRecord.filter(ll => ll(localeStateIndex).equals(l))))

// The following calls line up everything per the index extracted from the headers by default
//    val mapConfirmedCounty = getNormalizedLocaleRecords(localeList, localeCountyIndex, tokensPerRecordConfirmed.slice(1, tokensPerRecordConfirmed.length), false)
//    val mapConfirmedState = getNormalizedLocaleRecords(localeList, localeStateIndex, tokensPerRecordConfirmed.slice(1, tokensPerRecordConfirmed.length), false)
    
//    val mapDeathsCounty = getNormalizedLocaleRecords(localeList, localeCountyIndex, tokensPerRecordDeaths.slice(1, tokensPerRecordDeaths.length), false)
//    val mapDeathsState = getNormalizedLocaleRecords(localeList, localeStateIndex, tokensPerRecordDeaths.slice(1, tokensPerRecordDeaths.length), false)
    
    // now combine these into cumulative CovidSnapshots - using default Loc and default Lat
    countyRecordsMap.foreach(c => {
          val (normalizerCounty, ecCounty) = countyPopulationECMap.contains(c._1) match {
            case true => (countyPopulationECMap(c._1)._1, countyPopulationECMap(c._1)._2)
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
                                                         DEFAULT_LAT_COUNTY, 
                                                         DEFAULT_LONG_COUNTY,
                                                         confirmedDaily(i)._2, confirmedDaily(i)._2.toDouble/normalizerCounty,
                                                         confirmedDaily(i)._2.toDouble/(normalizerCounty*normalizerCountyPD),
                                                         0L,0.0,
                                                         0L, 0.0,// daily active does not make much sense
                                                         deathsDaily(i)._2,deathsDaily(i)._2.toDouble/normalizerCounty,
                                                         ecCounty,
                                                         "NYGithub",
                                                         None))
          val activeCumulative = confirmedCumul(i)._2-deathsCumul(i)._2-0L  // using recovery is 0 without any additional info but we won't be using these snapshots                                                  
          covidSnapshotListBufferCumul.+=(CovidSnapshot(confirmedCumul(i)._1, 
                                                         c._1, 
                                                         DEFAULT_LAT_COUNTY, 
                                                         DEFAULT_LONG_COUNTY,
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
    
    
    val countyCovidSnapshotMapDaily = localeListCounty.map(c => (c -> HelperFunctions.pivotOnCountry(dailyCS, c))).toMap
    
    val countyCovidSnapshotMapCumul = localeListCounty.map(c => (c -> HelperFunctions.pivotOnCountry(cumulCS, c))).toMap
    
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
    (recordsCountValues,extractDailyFromCumulativeCounty(recordsCountValues))
  }
  
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
  def getNormalizedLocaleRecords(localeList: List[String], localeIndex: Int, dataRecords: List[List[String]], checkForMovedIndex: Boolean = true): Map[String, List[List[String]]] = {
    
    checkForMovedIndex match {
      case false => localeList.map(c => (c -> dataRecords.filter(rec => rec(localeIndex).equals(c)))).toMap
      case true => {
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
    }
  }
  
  def transformCumulativeDataCountyNYFormat(fileNameCounty: String): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    createSnapshotsCounty(readCSV(fileNameCounty).map(line => tokenize(line)),countryLocaleMap)
  }
  
  def transformCumulativeDataJHUFormat(fileNameConfirmed: String, fileNameRecovered: String, fileNameDeaths: String): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots],
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    createSnapshots(readCSV(fileNameConfirmed).map(line => tokenize(line)),
                    readCSV(fileNameRecovered).map(line => tokenize(line)),
                    readCSV(fileNameDeaths).map(line => tokenize(line)),countryLocaleMap)
  }
 
}