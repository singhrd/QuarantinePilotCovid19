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
  
    def main(args: Array[String]) {
    
    
    val directoryName = "/home/rajdeep/workspace/QuarantinePilotCovid19/data/csv/current/"
    val outputDirectoryNameSnapshots="/home/rajdeep/workspace/QuarantinePilotCovid19/ui/src/assets/snapshots/"

    val outputDirectoryNameAnnotations="/home/rajdeep/workspace/QuarantinePilotCovid19/ui/src/assets/annotations/"
    
    val (dcS, ccS,dcSDate,ccSDate,dcSCountry,ccSCountry) = InputOutput.transformCumulativeDataJHUFormat(directoryName+"time_series_covid19_confirmed_global.csv",
                                     directoryName+"time_series_covid19_recovered_global.csv",
                                     directoryName+"time_series_covid19_deaths_global.csv")
//                                     
    InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcS).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/dailySnapshots.json")
    InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccS).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/cumulativeSnapshots.json")
    dcSDate.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSDate(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+HelperFunctions.makeFileNameFriendlyDateString(x._1)+"_DailySnapshots.json"))
    ccSDate.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSDate(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+HelperFunctions.makeFileNameFriendlyDateString(x._1)+"_CumulativeSnapshots.json"))
    dcSCountry.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSCountry(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+x._1+"_DailySnapshots.json"))
    ccSCountry.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSCountry(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+x._1+"_CumulativeSnapshots.json"))
//    
//    
    val locs = HelperFunctions.distinct(dcS.snapshots.map(d => GeoLocation(d.lat, d.long)))
    val locCovidSnapshotMap = locs.map(loc => (loc -> (HelperFunctions.filter(dcS, loc),HelperFunctions.filter(ccS, loc)))).toMap

    //    // Compute for each distinct date

    val annotations = Annotations(locs.map(loc => HelperFunctions.createAnnotation(locCovidSnapshotMap(loc)._1,locCovidSnapshotMap(loc)._2, 4, 4, List(7,14,21))).flatten)
    val annotationsCountryMap = dcS.snapshots.map(x => (x.country -> HelperFunctions.pivotAnnotationsOnCountry(annotations, x.country)))
    val annotationsDateMap = dcS.snapshots.map(x => (x.date -> HelperFunctions.pivotAnnotationsOnCountry(annotations, x.date)))
    val annotationsJSValue = annotationsJsonImplicit.write(annotations)
    InputOutput.writeToFile(annotationsJSValue.prettyPrint.getBytes,outputDirectoryNameAnnotations+"annotations.json")
    annotationsDateMap.foreach(x => InputOutput.writeToFile(annotationsJsonImplicit.write(x._2).prettyPrint.getBytes, outputDirectoryNameAnnotations+HelperFunctions.makeFileNameFriendlyDateString(x._1)+"_Annotations.json"))
    annotationsCountryMap.foreach(x => InputOutput.writeToFile(annotationsJsonImplicit.write(x._2).prettyPrint.getBytes, outputDirectoryNameAnnotations+x._1+"_Annotations.json"))
    
  }
}