package quarantine.covid19.util


import quarantine.covid19.core.CovidSnapshots
import scala.collection.mutable.ListBuffer
import quarantine.covid19.core.CovidSnapshot
import quarantine.covid19.core.Annotation
import quarantine.covid19.core.Annotations
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.core.JsonSupport
import quarantine.covid19.core.Alert
import quarantine.covid19.core.AlertUIInfo
import quarantine.covid19.constants.Constants

import quarantine.covid19.constants.MetricType
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
    val outputDirectoryNameAlerts = "../ui/src/assets/alerts/"

    val tempDataDirectory = "../"
    
    val confirmedFileName = "time_series_covid19_confirmed_global.csv"
    val recoveredFileName = "time_series_covid19_recovered_global.csv"
    val deathsFileName = "time_series_covid19_deaths_global.csv"
    val countyFileName = "us-counties.csv"
    
    
    def update(localeType: String = "county", updateType: String = "Both", generateAlerts: Boolean = true, writeNewData: Boolean = true, 
               startLocaleIndex: Int = 0, maxLocales:Int = 5000) = {
      
      val (dcS, ccS, dcSLocale,ccSLocale) = localeType match {
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

    val locales =   dcS.snapshots.map(d => d.locale).distinct.sortWith((a,b) => a<b)
    println("Found unique locales " + locales.length)
    val localeDailyCovidSnapshotMap = HelperFunctions.pivotCovidSnapshotByLocale(dcS.snapshots)
    val localeCumulativeCovidSnapshotMap = HelperFunctions.pivotCovidSnapshotByLocale(ccS.snapshots)
    
//    val annotations = Annotations(locs.map(loc => {
//    	HelperFunctions.createAnnotation(locCovidSnapshotMap(loc)._1,
//    	                                 locCovidSnapshotMap(loc)._2,
    val annotationsLocaleMap = scala.collection.mutable.Map[String, List[Annotation]]()
    val alertMap = scala.collection.mutable.Map[String, Double]()
    updateType match {
      case "SnapshotsOnly" => // just go to writing then
      case _ => {
    
        locales.slice(startLocaleIndex,scala.math.min(maxLocales, locales.length)).foreach(locale => {
        	val annotationsLocale = HelperFunctions.createAnnotation(localeDailyCovidSnapshotMap(locale),
        	                                                                         localeCumulativeCovidSnapshotMap(locale))
        	annotationsLocale match {
        	  case None => // do nothing
        	  case Some(x: List[Annotation]) => {
        	    generateAlerts match {
        	      case false => // do nothing
        	      case true => {
        	              val alertMeasure = HelperFunctions.findAlertMeasureExample(x, MetricType.DailyPer100k,  14) //"weekly",
        	              alertMeasure match {
        	                case Some(y: Double) => alertMap.+=(locale -> y)
        	                case None => // do nothing
        	              }
        	      }
        	    }
        	    annotationsLocaleMap.+=(locale -> x)
        	  }
        	}
        }) 
      }
    }
    
  
    generateAlerts match {
      case false => // do nothing
      case true => {
        alertMap.size > 0 match {
          case true => {
            alertMap.foreach(println(_))
            val sortedAlertMap = alertMap.toArray.sortWith((a,b) => a._2 > b._2)
            val top5Map = sortedAlertMap.slice(0, scala.math.min(5, alertMap.size))  
            val bottom5Map = sortedAlertMap.reverse.slice(0, scala.math.min(5, alertMap.size))
                val minChange = top5Map.map(x => x._2).foldLeft(100000000.0)((a,b) => scala.math.min(a,b))
                val maxChange = bottom5Map.map(x => x._2).foldLeft(100000000.0)((a,b) => scala.math.max(a,b))
//                val localesTopMap = top5Map.map(x => x._1)
//                val localesBottomMap = bottom5Map.map(x => x._1)
                
                val alertTopTemplate = Constants.alertTemplate("Confirmed Daily Cases per 100k", "weekly", 14, minChange)
                val alertBottomTemplate = Constants.alertTemplate("Confirmed Daily Cases per 100k", "weekly", 14, maxChange)
                
                val alertTop = Alert(localeType, "Confirmed Daily Cases per 100k", "weekly",14,alertTopTemplate,top5Map)
                val alertBottom = Alert(localeType, "Confirmed Daily Cases per 100k", "weekly",14,alertBottomTemplate,bottom5Map)
    
                InputOutput.writeToFile(alertJsonImplicit.write(alertTop).prettyPrint.getBytes,
                                          outputDirectoryNameAlerts+localeType+"/"+"topAlerts.json")
                                          
                InputOutput.writeToFile(alertJsonImplicit.write(alertBottom).prettyPrint.getBytes,
                                          outputDirectoryNameAlerts+localeType+"/"+"bottomAlerts.json")
              }
          case false => // do nothing
        }
      }
//        create an alert and write it
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
//      update("county","Both" , false, true, 0, 1000)
//      update("county","Both" , false, true, 1000, 2000)
      update("county","Both" , false, true, 2000, 3100)
//      update("county","Both" , true, false, 0, 3500)
    
  }
}
