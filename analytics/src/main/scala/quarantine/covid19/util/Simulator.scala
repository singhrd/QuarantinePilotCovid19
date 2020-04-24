package quarantine.covid19.util


/**
 * 
 * 
 * @author rajdeep
 */


import java.util.Date
import quarantine.covid19.core.CovidSnapshot
import quarantine.covid19.core.JsonSupport
import quarantine.covid19.core.CovidSnapshots
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.util.HelperFunctions
import quarantine.covid19.core.Annotation
import quarantine.covid19.core.Annotations
import quarantine.covid19.core.SampleJson

/**
 * We will use this for creating some dummy data that we will use to test the UI and verify some of the
 * analytics.
 * 
 * @author Rajdeep Singh
 */
object Simulator extends JsonSupport {
 
  
  val nyAlpha = 0.2;
  val sdAlpha = 0.1;
  val sdLat = "2";
  val sdLong = "3";
  val nyLat = "4";
  val nyLong = "5";
  val nyTests = 500000;
  val sdTests = 10000;
  val detectionRateSD = 0.08;
  val detectionRateNY = 0.4;
  val deathRateSD = 0.02;
  val recoveryRateNY = 0.15;
  val recoveryRateSD = 0.25;
  val deathRateNY = 0.04;
  val country = "US"
  val defaultSource = "simulator"
  
//  def convertToJson
  


 
  

  
  def createSampleCovidSnapshots(datesTestMap: List[(String, Int)]): List[CovidSnapshot] = {
   
    datesTestMap.map(d =>  createSampleCovidSnapshots(d._2, d._1)).flatten
  }
  
  
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
  
  

  
  def createSampleCovidSnapshots(nTests: Long, date: String): List[CovidSnapshot] = {
    
    // first create for SD
    
    val confirmedSD = (scala.math.ceil(nTests*detectionRateSD)) 
    val deathsSD = (scala.math.ceil(confirmedSD*deathRateSD))
    val recoveredSD = (scala.math.ceil(confirmedSD*recoveryRateSD))
    val activeSD = confirmedSD - deathsSD - recoveredSD;
    
    val lStateSD = CovidSnapshot(date, "San Diego", 
                                 country, 
                                 sdLat, 
                                 sdLong, 
                                 confirmedSD.toLong,
                                 deathsSD.toLong,
                                 recoveredSD.toLong,
                                 activeSD.toLong, 
                                 defaultSource)
  // then create for NY
    val confirmedNY = (scala.math.ceil(nTests*detectionRateNY)) 
    val deathsNY = (scala.math.ceil(confirmedNY*deathRateNY))
    val recoveredNY = (scala.math.ceil(confirmedNY*recoveryRateNY))
    val activeNY = confirmedNY - deathsNY - recoveredNY;
    
    val lStateNY = CovidSnapshot(date, 
                                   "New York", 
                                   country, 
                                   nyLat, 
                                   nyLong, 
                                   confirmedNY.toLong,
                                   deathsNY.toLong,
                                   recoveredNY.toLong, 
                                   activeNY.toLong, 
                                   defaultSource)
      
      
      
    return List[CovidSnapshot](lStateSD, lStateNY)
  }
  
  

  def main(args: Array[String]) {
    
    val sJ = SampleJson("1", List(1,2))
    println("Test " + sampleJsonImplicit.write(sJ))
//    println(HelperFunctions.beforeOrEqual("03/05/2020", "03/01/2020"))
////    println(getDate("03/04/020"))
//    val listSimulator = createSampleCovidSnapshots(200, "03/01/2020")
//    val listSimulatorA = createSampleCovidSnapshots(200, "03/05/2020")
//    
//    println("Before Sorting")
//    println(listSimulatorA++listSimulator)
//    println("After Sorting")
//    println(HelperFunctions.sortSnapshots(listSimulatorA++listSimulator))
    
//    listSimulator.foreach(println(_))
    
    val listSimulator2 = createSampleCovidSnapshots(List[(String,Int)](("03/01/2020",100), ("03/02/2020",200),("03/03/2020",50),
                                                     ("03/04/2020",300),("03/05/2020",250),
                                                     ("03/06/2020",500),
                                                     ("03/07/2020",350),
                                                     ("03/08/2020",400)))
    
    listSimulator2.foreach(l => println(covidSnapshotJsonImplicit.write(l)))
    
    val (cumulativeState, annotations) = HelperFunctions.createCumulativeCovidSnapshots(CovidSnapshots(listSimulator2), true)
    
    println("****** Cumulative ********")
    cumulativeState.snapshots.foreach(l => println(covidSnapshotJsonImplicit.write(l)))
    println(covidSnapshotsJsonImplicit.write(cumulativeState))
    
    val annotationsJSValue = annotationsJsonImplicit.write(Annotations(annotations.get))
    IOUtil.writeToFile(annotationsJSValue.prettyPrint.getBytes, "/tmp/anno.json")
  }
}
