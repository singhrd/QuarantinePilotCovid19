package quarantine.covid19.util


import quarantine.covid19.core.CovidSnapshots
import scala.collection.mutable.ListBuffer
import quarantine.covid19.core.CovidSnapshot
import quarantine.covid19.core.Annotation
import quarantine.covid19.core.Annotations
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.core.JsonSupport
import quarantine.covid19.constants.Constants

/**
 * This is the driver code that ingests raw data from JHU github, 
 * transforms, and creates meta-data from the data source, and writes them 
 * in json format to 
 * 
 */
object Driver extends JsonSupport {

    
  
    // make this a part of some config file but ok for now
  
    val csvDirectoryName = "../data/csv/current/"
    val outputDirectoryNameSnapshots = "../ui/src/assets/snapshots/"

    val outputDirectoryNameAnnotations = "../ui/src/assets/annotations/"

    val tempDataDirectory = "../"
    
    val confirmedFileName = "time_series_covid19_confirmed_global.csv"
    val recoveredFileName = "time_series_covid19_recovered_global.csv"
    val deathsFileName = "time_series_covid19_deaths_global.csv"
    val countyFileName = "us-counties.csv"
    
    
    def update(locale: String = "county", writeNewData: Boolean = true) = {
      
      val (dcS, ccS, dcSLocale,ccSLocale) = locale match {
        case "county" =>  
          HelperFunctions.transformCumulativeDataCountyNYFormat(csvDirectoryName+countyFileName)
        
        case "state" => HelperFunctions.transformCumulativeDataCountyNYFormat(csvDirectoryName+countyFileName, "state")
        
        case "country" => 
          HelperFunctions.transformCumulativeDataJHUFormat(csvDirectoryName+confirmedFileName,
                                     csvDirectoryName+recoveredFileName,
                                     csvDirectoryName+deathsFileName)
      }
          
    val locs = HelperFunctions.distinct(dcS.snapshots.map(d => GeoLocation(d.lat, d.long)))
    val locCovidSnapshotMap = locs.map(loc => (loc -> (HelperFunctions.filter(dcS, loc),HelperFunctions.filter(ccS, loc)))).toMap

    val annotations = Annotations(locs.map(loc => {
    	HelperFunctions.createAnnotation(locCovidSnapshotMap(loc)._1,
    	                                 locCovidSnapshotMap(loc)._2, 
    	                                 Constants.DefaultDeltaInDays,
    	                                 Constants.DefaultDeltaInDays,
    	                                 Constants.DefaultMovingAverageWindowInDaysSet)
    }).flatten)
    
    val annotationsLocaleMap = HelperFunctions.pivotAnnotationsByLocale(annotations.elements)
  
    val mapNames = locale match {
      case "county" => HelperFunctions.createCountyNameLookUpMap(annotations.elements.map(_.locale))
      case  _ => annotations.elements.map(x => (x.locale -> x.locale)).toMap
    }
    
    writeNewData match {
      case true => {
    	  dcSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+mapNames(x._1)+"_DailySnapshots.json"))
    	  ccSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+mapNames(x._1)+"_CumulativeSnapshots.json"))
    	  annotationsLocaleMap.foreach(x => InputOutput.writeToFile(annotationsJsonImplicit.write(Annotations(x._2)).prettyPrint.getBytes, outputDirectoryNameAnnotations+mapNames(x._1)+"_Annotations.json"))
      }
      case false => // do not write
    }


    }
    
    def main(args: Array[String]) {
    	update("country")
    	update("state")
      update("county")
    
  }
}