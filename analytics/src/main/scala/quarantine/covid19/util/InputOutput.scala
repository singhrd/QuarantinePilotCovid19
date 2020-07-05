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
import java.nio.file.Files
import java.nio.file.Paths
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

  val sanDiegoPopulationByZipCodeFileName = "../data/csv/general/populationSanDiegoByZipSandag.csv"
  
  
  
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
    val countyNameOnly = elements(1).replace(" County", "").replace(" Parish", "").replace(" Borough","").replace(" Municipality","").toLowerCase()
    ((countyNameOnly+","+elements(0).toLowerCase())-> (elements(2).toDouble/Constants.OneHundredThousand, elements(2).toDouble*Constants.EpidemicControlThresholdPer100k/Constants.OneHundredThousand))
  }).toMap
  }

  // state data is in M so instead of dividing by OneHundredThousand we multiply by 10
  val statePopulationECMap= {
    val lines = readCSV(statePopulationMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    // multiplication by 10 is equivalent to multiply by ONE_MILLION and divide by ONE_HUNDRED_THOUSAND - the population in the file is in Millions
    (elements(0)-> (elements(1).toDouble*10.0, elements(1).toDouble*10.0*Constants.EpidemicControlThresholdPer100k))
  }).toMap
  }
    
  // We are normalizing every country to 100 sq mile
  val countryPopulationDensityMap= {
    val lines = readCSV(countryPopulationDensityMapFileName)
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    (elements(0) -> (elements(2).toDouble/Constants.OneHundred))
  }).toMap
  }
  
  // Since the zip code data is segmented by age/gender - we simply add 
  // values belonging to the same zip code to get the total population and then normalize
  // The county does not normalize for zip codes with cases <5 or population less than 10k
  val sanDiegoZipCodePopulationMap = {
    val lines = readCSV(sanDiegoPopulationByZipCodeFileName)
    val mapZipToCount = scala.collection.mutable.Map[String, Long]()
    lines.slice(1,lines.length).map(x => {
    val elements = tokenize(x,Some(Constants.CommaDelimiter))
    val zipCurrent = elements(0)
    val popSegmentCount = elements(1).toLong
    val currentCount = mapZipToCount.getOrElse(zipCurrent, 0L)
    mapZipToCount.+=((zipCurrent, currentCount+popSegmentCount))
  })
  mapZipToCount.filter(y => y._2 >0L).map(x => (x._1 -> (x._2/Constants.OneHundredThousand, x._2*Constants.EpidemicControlThresholdPer100k/Constants.OneHundredThousand)))
  }
  /**
   * Took a while to fix this
   * https://dzone.com/articles/fileinputstream-fileoutputstream-considered-harmful
   */
  def writeToFile(content: Array[Byte], fileName: String) = {
    
//    try {
//      val os = Files.newOutputStream(Paths.get(fileName))
//        os.write(content);
//    }
    
        val bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName))
//    try {
//      val os = Files.newOutputStream(Paths.get(fileName))
        bufferedOutputStream.write(content);
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
  }
    
  
  /**
   * Line delimited reading of a csv
   * The first element is the header line
   */
  def readCSV(filePath: String): List[String] = io.Source.fromFile(filePath).getLines().toList
  
  def readAnnotations(dirPath: String):List[Annotations] = {
    
    null
  }
  /**
   * Splitting a string/line into the elements using the delimiter 
   * If the delimiter is not provided, we use the CSV delimiter
   */
  def tokenize(line: String, delimiter: Option[String] = None): List[String] = {
    handleCountryNames(line).split(delimiter.getOrElse(Constants.DefaultDelimiter)).toList
  }
 
  def fixCountryNamesMisMatch(name: String): String = Constants.mappingNamesMismatch.getOrElse(name, name)
  
  
  def handleCountryNames(line: String): String = {
    line.contains(Constants.DoubleQuotes) match {
      case false => line.replace("*","")
      case true => {
        val elements = line.split(Constants.DoubleQuotes)
        val elementCountryName = elements(1).split(",")
        
        elements(0) + fixCountryNamesMisMatch(elementCountryName(0).replace(" ","") + "_" + elementCountryName(1).replace(" ",""))  + elements(2)
      }
    }
  }
  
  
  
  
}