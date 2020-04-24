package quarantine.covid19.util

import java.io._

object InputOutput {
  
  
  val COMMA_DELIMITER = ","
  val DEFAULT_DELIMITER = COMMA_DELIMITER
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
  
//  def writeToFile(lines: List[String], filePath: String) = {
//    
//  }
  
  def main(args: Array[String]) {
    val fileName = "/tmp/download.csv"
    val lines = readCSV(fileName)
    println(lines.length)
    
    val firstToken = lines.map(line => tokenize(line)(2)).distinct
    firstToken.foreach(println(_))
    println(lines(0) + "*****" + tokenize(lines(0)).length)
    println(lines(99) + "*****" + tokenize(lines(99)).length)
    println(lines(100) + "*****" + tokenize(lines(100)).length)
                
  }
}