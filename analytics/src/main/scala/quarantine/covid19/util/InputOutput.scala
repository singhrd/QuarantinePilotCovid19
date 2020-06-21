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
import scala.collection.mutable.ListBuffer
import quarantine.covid19.core.CovidSnapshots
import quarantine.covid19.core.CovidSnapshot
import quarantine.covid19.core.Annotation
import quarantine.covid19.core.Annotations
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.core.JsonSupport
import quarantine.covid19.core.JsonSupport
import quarantine.covid19.constants.Constants

object InputOutput extends JsonSupport {
  
  val countryMapFileName = "../data/csv/general/concap.csv"
  val countryPopulationMapFileName = "../data/csv/general/populationcountry2020.csv"
  val countryPopulationDensityMapFileName = "../data/csv/general/populationDensityCountry.csv"
 
  val countyPopulationMapFileName = "../data/csv/general/populationCountyUS.csv"
  val statePopulationMapFileName = "../data/csv/general/populationStates.csv"

  val countryTestMapFileName = "../data/csv/general/populationStates.csv"
  
  val stateTestMapFileName = "../data/csv/general/populationStates.csv"
  
  
  val countryLocaleMap = readCSV(countryMapFileName).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    (elements(0) -> (elements(1),GeoLocation(elements(2),elements(3))))
  }).toMap
  
  
  val countryPopulationECMap= {
    val lines = readCSV(countryPopulationMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    (elements(0) -> (elements(1).toDouble/Constants.OneHundredThousand, elements(1).toDouble*Constants.EpidemicControlThresholdPer100k/Constants.OneHundredThousand))
  }).toMap
  }
  
  val countyPopulationECMap= {
    val lines = readCSV(countyPopulationMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    val countyNameOnly = elements(1).replace(" County", "").toLowerCase()
    ((countyNameOnly+","+elements(0).toLowerCase())-> (elements(2).toDouble/Constants.OneHundredThousand, elements(2).toDouble*Constants.EpidemicControlThresholdPer100k/Constants.OneHundredThousand))
  }).toMap
  }

  val statePopulationECMap= {
    val lines = readCSV(statePopulationMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    // multiplication by 10 is equivalent to multiply by ONE_MILLION and divide by ONE_HUNDRED_THOUSAND - the population in the file is in Millions
    (elements(0)-> (elements(1).toDouble*10.0, elements(1).toDouble*10.0*Constants.EpidemicControlThresholdPer100k))
  }).toMap
  }
    
  
  val countryPopulationDensityMap= {
    val lines = readCSV(countryPopulationDensityMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    (elements(0) -> (elements(2).toDouble/Constants.OneHundred))
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
    line.split(delimiter.getOrElse(Constants.DefaultDelimiter)).toList
  }
 
}