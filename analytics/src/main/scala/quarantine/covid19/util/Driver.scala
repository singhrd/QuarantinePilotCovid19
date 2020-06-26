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
    
    
    def update(locale: String = "county", updateType: String = "Both", writeNewData: Boolean = true) = {
      
      val (dcS, ccS, dcSLocale,ccSLocale) = locale match {
        case "county" =>  
          HelperFunctions.transformCumulativeDataCountyNYFormat(csvDirectoryName+countyFileName)
        
        case "state" => HelperFunctions.transformCumulativeDataCountyNYFormat(csvDirectoryName+countyFileName, "state")
        
        case "country" => 
          HelperFunctions.transformCumulativeDataJHUFormat(csvDirectoryName+confirmedFileName,
                                     csvDirectoryName+recoveredFileName,
                                     csvDirectoryName+deathsFileName)
      }
          
    // may be we can do this with locale (if it is unique) instead of location  
//    val locs = HelperFunctions.distinct(dcS.snapshots.map(d => GeoLocation(d.lat, d.long)))
//    val locCovidSnapshotMap = locs.map(loc => (loc -> (HelperFunctions.filter(dcS, loc),HelperFunctions.filter(ccS, loc)))).toMap

    val locales =   dcS.snapshots.map(d => d.locale).distinct
    println("Found unique locales " + locales.length)
    val localeDailyCovidSnapshotMap = HelperFunctions.pivotCovidSnapshotByLocale(dcS.snapshots)
    val localeCumulativeCovidSnapshotMap = HelperFunctions.pivotCovidSnapshotByLocale(ccS.snapshots)
    
//    val annotations = Annotations(locs.map(loc => {
//    	HelperFunctions.createAnnotation(locCovidSnapshotMap(loc)._1,
//    	                                 locCovidSnapshotMap(loc)._2,
    val annotationsLocaleMap = scala.collection.mutable.Map[String, List[Annotation]]()
    updateType match {
      case "SnapshotsOnly" => // just go to writing then
      case _ => {
    
        locales.foreach(locale => {
        	val annotationsLocale = HelperFunctions.createAnnotation(localeDailyCovidSnapshotMap(locale),
        	                                                                         localeCumulativeCovidSnapshotMap(locale))
        	annotationsLocale match {
        	  case None => // do nothing
        	  case Some(x: List[Annotation]) => annotationsLocaleMap.+=(locale -> x)
        	}
        }) 
      }
    }
    
  
//    val mapNames = locale match {
//      case "county" => HelperFunctions.createCountyNameLookUpMap(locales)
//      case  _ => annotationsLocaleMap.values.toList.flatten.map(x => (x.locale -> x.locale)).toMap
//    }
    
    writeNewData match {
      case true => {
        updateType match {
          case "SnapshotsOnly" => { 
            dcSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+x._1+"_DailySnapshots.json"))
            ccSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+x._1+"_CumulativeSnapshots.json"))
          }
          case "AnnotationsOnly" => {
            println("Starting to write ")
        	  annotationsLocaleMap.foreach(x => InputOutput.writeToFile(annotationsJsonImplicit.write(Annotations(x._2)).prettyPrint.getBytes, outputDirectoryNameAnnotations+x._1+"_Annotations.json"))
          }
          case "Both" => {
            dcSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+x._1+"_DailySnapshots.json"))
            ccSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+x._1+"_CumulativeSnapshots.json"))
        	  annotationsLocaleMap.foreach(x => InputOutput.writeToFile(annotationsJsonImplicit.write(Annotations(x._2)).prettyPrint.getBytes, outputDirectoryNameAnnotations+x._1+"_Annotations.json"))
          }

        }
      }
      case false => // do not write
    }


    }
    
    def main(args: Array[String]) {
//    	update("country")
//    	update("state")
//      update("county","SnapshotsOnly")
      update("county","AnnotationsOnly")
    
  }
}