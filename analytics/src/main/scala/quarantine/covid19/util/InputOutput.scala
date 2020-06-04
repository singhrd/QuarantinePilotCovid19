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
  val countryMapFileName = "../data/csv/general/concap.csv"
  val countryPopulationMapFileName = "../data/csv/general/populationcountry2020.csv"
  val countryPopulationDensityMapFileName = "../data/csv/general/populationDensityCountry.csv"
 
  val countryMap = readCSV(countryMapFileName).map(x => {
    val elements = tokenize(x,Some(COMMA_DELIMITER))
    (elements(0) -> (elements(1),GeoLocation(elements(2),elements(3))))
  }).toMap
  
  
  val countryPopulationMap= {
    val lines = readCSV(countryPopulationMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(COMMA_DELIMITER))
    (elements(0) -> (elements(1).toDouble/ONE_HUNDRED_THOUSAND, elements(1).toDouble*EPIDEMIC_CONTROL_THRESHOLD_PER_100K/ONE_HUNDRED_THOUSAND))
  }).toMap
  }
  
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
                      countryMap: Map[String, (String, GeoLocation)]): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots],
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    
    val covidSnapshotListBufferDaily = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    val covidSnapshotListBufferCumul = scala.collection.mutable.ListBuffer[CovidSnapshot]()
    
    val headers = tokensPerRecordConfirmed(0)
    println(headers)
    val countryIndex = 1 // headers.indexOf("Country"+"\\/\\"+"Region")
    val latIndex = headers.indexOf("Lat")
    val longIndex = headers.indexOf("Long")
    val provinceStateIndex = 0 // headers.indexOf("Province"+"\\/\\"+"State")
    val firstDateIndex = 4//headers.indexOf("1"+"\\/\\"+"20"+"\\/\\"+"2020")
    val lastDateIndex = headers.length
    
    // Check if need to escape the "/"
    val countryList = tokensPerRecordConfirmed.slice(1,tokensPerRecordConfirmed.length).map(l => {
      l(0).startsWith(DOUBLE_QUOTES) match {
        case false => l(countryIndex)
        case true => l(countryIndex+1) // might need revision
      }
    }).distinct
    
    // The following calls line up everything per the index extracted from the headers by default
    val mapConfirmed = getNormalizedCountryRecords(countryList, countryIndex, tokensPerRecordConfirmed.slice(1, tokensPerRecordConfirmed.length))
    val mapRecovered = getNormalizedCountryRecords(countryList, countryIndex, tokensPerRecordRecovered.slice(1, tokensPerRecordRecovered.length))
    val mapDeaths = getNormalizedCountryRecords(countryList, countryIndex, tokensPerRecordDeaths.slice(1, tokensPerRecordDeaths.length))
    
    // now combine these into cumulative CovidSnapshots
    countryList.foreach(c => {
      countryMap.contains(c) match {
        case true => {
          val countryMapValue = countryMap(c)
          val (normalizerCountry, ecCountry) = countryPopulationMap.contains(c) match {
            case true => (countryPopulationMap(c)._1, countryPopulationMap(c)._2)
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
                                                         countryMapValue._1, 
                                                         c, 
                                                         countryMapValue._2.lat.toString(), 
                                                         countryMapValue._2.long.toString(),
                                                         confirmedDaily(i), confirmedDaily(i).toDouble/normalizerCountry,
                                                         confirmedDaily(i).toDouble/(normalizerCountry*normalizerCountryPD),
                                                         recoveredDaily(i),recoveredDaily(i).toDouble/normalizerCountry,
                                                         0L, 0.0,// daily active does not make much sense
                                                         deathsDaily(i),deathsDaily(i).toDouble/normalizerCountry,
                                                         ecCountry,
                                                         "JHU",
                                                         None))
          val activeCumulative = confirmedCumul(i)-deathsDaily(i)-recoveredCumul(i)                                                    
          covidSnapshotListBufferCumul.+=(CovidSnapshot(headers(i+firstDateIndex), 
                                                         countryMapValue._1, 
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
    
    val countryCovidSnapshotMapDaily = countryList.map(c => (c -> HelperFunctions.pivotOnCountry(dailyCS, c))).toMap
    
    val countryCovidSnapshotMapCumul = countryList.map(c => (c -> HelperFunctions.pivotOnCountry(cumulCS, c))).toMap
    
    (dailyCS,cumulCS,dateCovidSnapshotMapDaily,dateCovidSnapshotMapCumul,countryCovidSnapshotMapDaily,countryCovidSnapshotMapCumul)
  }
  
  def extractDailyFromCumulative(cumul:List[Long]): List[Long] = {
    val cumulOneStepMoved = cumul.slice(1,cumul.length)
    List(cumul(0)) ++ cumulOneStepMoved.zip(cumul.slice(0,cumul.length-1)).map(x => x._1-x._2)
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
  def getNormalizedCountryRecords(countryList: List[String], countryIndex: Int, dataRecords: List[List[String]]): Map[String, List[List[String]]] = {
    
    // now filter per country and aggregate the data
    countryList.map(c => {
      val filteredCountryDefaultIndex = dataRecords.filter(rec => rec(countryIndex).equals(c))
      val filteredCountryMovedIndex = dataRecords.filter(rec => rec(countryIndex+1).equals(c))
      filteredCountryMovedIndex.length >0 match {
        case false => (c -> filteredCountryDefaultIndex)
        case true => {
          println("**** Found multiple records for this country " + c)
          (c -> (filteredCountryDefaultIndex ++ filteredCountryMovedIndex.map(x => List(x(0)+"-"+x(1)) ++ x.slice(2,x.length))))
        }
      }
    }).toMap

  }
  
  def transformCumulativeDataJHUFormat(fileNameConfirmed: String, fileNameRecovered: String, fileNameDeaths: String): 
                      (CovidSnapshots, CovidSnapshots, 
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots],
                          Map[String,CovidSnapshots], Map[String, CovidSnapshots]) = {
    createSnapshots(readCSV(fileNameConfirmed).map(line => tokenize(line)),
                    readCSV(fileNameRecovered).map(line => tokenize(line)),
                    readCSV(fileNameDeaths).map(line => tokenize(line)),countryMap)
  }
 
}