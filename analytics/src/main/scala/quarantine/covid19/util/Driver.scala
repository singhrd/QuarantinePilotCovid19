package quarantine.covid19.util


import quarantine.covid19.core.CovidSnapshots
import scala.collection.mutable.ListBuffer
import quarantine.covid19.core.CovidSnapshot
import quarantine.covid19.core.Annotation
import quarantine.covid19.core.Annotations
import quarantine.covid19.core.GeoLocation
import quarantine.covid19.core.JsonSupport
/**
 * This is the driver code that ingests raw data from JHU github, 
 * transforms, and creates meta-data from the data source, and writes them 
 * in json format to 
 * 
 */
object Driver extends JsonSupport {

    
  
    val directoryName = "../data/csv/current/"
    val outputDirectoryNameSnapshots="../ui/src/assets/snapshots/"

    val outputDirectoryNameAnnotations="../ui/src/assets/annotations/"

    
    val confirmedFileName = "time_series_covid19_confirmed_global.csv"
    val recoveredFileName = "time_series_covid19_recovered_global.csv"
    val deathsFileName = "time_series_covid19_deaths_global.csv"
    val countyFileName = "us-counties.csv"
    
    def main(args: Array[String]) {
    

//    val (dcSCounties, ccSCounties, dcSCounty, ccSCounty) = InputOutput.transformCumulativeDataCountyNYFormat(directoryName+countyFileName)  
    
    val (dcS, ccS,dcSDate,ccSDate,dcSLocale,ccSLocale) = InputOutput.transformCumulativeDataJHUFormat(directoryName+confirmedFileName,
                                     directoryName+recoveredFileName,
                                     directoryName+deathsFileName)
//                                     
    InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcS).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/dailySnapshots.json")
    InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccS).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/cumulativeSnapshots.json")
//    dcSDate.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSDate(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+HelperFunctions.makeFileNameFriendlyDateString(x._1)+"_DailySnapshots.json"))
//    ccSDate.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSDate(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+HelperFunctions.makeFileNameFriendlyDateString(x._1)+"_CumulativeSnapshots.json"))
    dcSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+x._1+"_DailySnapshots.json"))
    ccSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+x._1+"_CumulativeSnapshots.json"))
//    
//    
    val locs = HelperFunctions.distinct(dcS.snapshots.map(d => GeoLocation(d.lat, d.long)))
    val locCovidSnapshotMap = locs.map(loc => (loc -> (HelperFunctions.filter(dcS, loc),HelperFunctions.filter(ccS, loc)))).toMap

    //    // Compute for each distinct date

    val annotations = Annotations(locs.map(loc => HelperFunctions.createAnnotation(locCovidSnapshotMap(loc)._1,locCovidSnapshotMap(loc)._2, 4, 4, List(7,14,21))).flatten)
    val annotationsLocaleMap = dcS.snapshots.map(x => (x.locale -> HelperFunctions.pivotAnnotationsOnCountry(annotations, x.locale)))
//    val annotationsDateMap = dcS.snapshots.map(x => (x.date -> HelperFunctions.pivotAnnotationsOnDate(annotations, x.date)))
    val annotationsJSValue = annotationsJsonImplicit.write(annotations)
    InputOutput.writeToFile(annotationsJSValue.prettyPrint.getBytes,outputDirectoryNameAnnotations+"annotations.json")
//    annotationsDateMap.foreach(x => InputOutput.writeToFile(annotationsJsonImplicit.write(x._2).prettyPrint.getBytes, outputDirectoryNameAnnotations+HelperFunctions.makeFileNameFriendlyDateString(x._1)+"_Annotations.json"))
    annotationsLocaleMap.foreach(x => InputOutput.writeToFile(annotationsJsonImplicit.write(x._2).prettyPrint.getBytes, outputDirectoryNameAnnotations+x._1+"_Annotations.json"))
    
  }
}