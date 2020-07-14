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
import quarantine.covid19.core.RiskLocale
import quarantine.covid19.core.RiskLocales
import quarantine.covid19.core.RiskLocalesTemporal
import quarantine.covid19.constants.Constants
import quarantine.covid19.constants.MetricType
import quarantine.covid19.constants.MeasureType
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
  val outputDirectoryNameMaps = "../ui/src/assets/maps/"

  val tempDataDirectory = "../"
    
  val confirmedFileName = "time_series_covid19_confirmed_global.csv"
  val recoveredFileName = "time_series_covid19_recovered_global.csv"
  val deathsFileName = "time_series_covid19_deaths_global.csv"
  val countyFileName = "us-counties.csv"
  val sandiegoZipCodeConfirmedFile = "sandiegozipcodesCOVID19.csv"

  def update(localeType: String = "county", createMapData: Boolean = true, generateAlerts: Boolean = true, writeNewData: Boolean = true) = {
    val (dcS, ccS, dcSLocale,ccSLocale) = localeType match {
      case "county" => HelperFunctions.transformCumulativeDataCountyNYFormat(csvDirectoryName+countyFileName)
      case "state" => HelperFunctions.transformCumulativeDataCountyNYFormat(csvDirectoryName+countyFileName, "state")
      case "country" => HelperFunctions.transformCumulativeDataJHUFormat(csvDirectoryName+confirmedFileName,
                                     csvDirectoryName+recoveredFileName,
                                     csvDirectoryName+deathsFileName)
      case "sandiegozipcode" => HelperFunctions.transformCumulativeDataSanDiegoCountyOpenGISFormat(csvDirectoryName+sandiegoZipCodeConfirmedFile)                               
    }
    val locales =   dcS.snapshots.map(d => d.locale).distinct.sortWith((a,b) => a<b)
    println("Found unique locales " + locales.length)
    
    val localeDailyCovidSnapshotMap = HelperFunctions.pivotCovidSnapshotByLocale(dcS.snapshots)
    val localeCumulativeCovidSnapshotMap = HelperFunctions.pivotCovidSnapshotByLocale(ccS.snapshots)

    val alertPercentMap = scala.collection.mutable.Map[String, Double]()
    val alertTrendMap = scala.collection.mutable.Map[String, Double]()
    val alertDailyMap = scala.collection.mutable.Map[String, Double]()


    
    val riskLocalesTemporal = scala.collection.mutable.Map[String, List[RiskLocale]]()
//    val riskLocalesTemporal = scala.collection.mutable.ListBuffer[(String, RiskLocale)]()
    
    locales.foreach(locale => {
      // we can use the sortSnapshots on this and then transform to the string, date form
      val currentSnapshotDaily = localeDailyCovidSnapshotMap(locale)
      val (lat, long) = (currentSnapshotDaily.snapshots(0).lat, currentSnapshotDaily.snapshots(0).long)
      val dates = currentSnapshotDaily.snapshots.map(x => x.date)
      val (dateMin, dateMax) = (dates(0), dates.last)
      val diffDays = HelperFunctions.daysBetween(HelperFunctions.getDate(dateMin), HelperFunctions.getDate(dateMax))
      diffDays > currentSnapshotDaily.snapshots.length match {
        case true => {
          println("There is a gap in data for this location " + locale + "-" + diffDays + "-"+ currentSnapshotDaily.snapshots  .length)
        }
        case false => {          
          val metricsForLocale = HelperFunctions.createAnnotationMetrics(localeDailyCovidSnapshotMap(locale),localeCumulativeCovidSnapshotMap(locale))
          generateAlerts match {
            case false => // do nothing
            case true => {
              val alertPercentMeasure = HelperFunctions.findPercentMeasure(metricsForLocale.last, 
                                                                                     metricsForLocale(metricsForLocale.length-14),
                                                                                     MetricType.DailyPer100k) //"weekly",
              alertPercentMeasure match {
                case Some(y: Double) => alertPercentMap.+=(locale -> y)
                case None => // do nothing
              }
              val alertTrendMeasure = HelperFunctions.findMovingAverageUpTrendMeasure(metricsForLocale.last, metricsForLocale(metricsForLocale.length-14), MetricType.DailyPer100k,  14) //"weekly",
              alertTrendMeasure match {
                case Some(y: Double) => alertTrendMap.+=(locale -> y)
                case None => // do nothing
              }        
              alertDailyMap.+=(locale -> metricsForLocale.last(MetricType.name(MetricType.DailyPer100k))(1)._2) 
            }
          }
          
 
          createMapData match {
                case true => { // do something 
                  
                  val continueCreatingMap = localeType.equals("state") match {
                    case true => !locale.equals("Hawaii") && !locale.equals("Alaska")
                    case false => {
                      localeType.equals("county") match {
                        case true => locale.endsWith(",California")
                        case false => {
                          localeType.equals("sandiegozipcode") match {
                            case true => true
                            case false => localeType.equals("country")
                          }
                        }
                      }
                    }
                  }
                   continueCreatingMap match {
                     case false => // do nothing
                     case true => {
                       val listRiskLocales = 
                       Range(1,Constants.DefaultMapWindowInDays).foreach(dateKey => {
                         val currentDateKey = dates(dates.length-dateKey)
                         val newValueToAdd = List(RiskLocale(InputOutput.mapLocaleToGeoMapName(localeType, locale), locale, metricsForLocale(dates.length-dateKey)(MetricType.name(MetricType.DailyPer100k))(1)._2))
                         riskLocalesTemporal.contains(currentDateKey) match {
                           case false => {
                             riskLocalesTemporal.+=(currentDateKey -> newValueToAdd)
                           }
                           case true => {
                             val currentValue = riskLocalesTemporal(currentDateKey)
                             riskLocalesTemporal.+=(currentDateKey -> (newValueToAdd++currentValue)) 
                           }
                         }
                       })
//                       val metricsLastWeek = metricsForLocale(metricsForLocale.length-8)
//                       val metricsNow = metricsForLocale.last
//                       riskLocalesLastWeek.+=((dates.last, RiskLocale(InputOutput.mapLocaleToGeoMapName(localeType, locale), locale, metricsLastWeek(MetricType.name(MetricType.DailyPer100k))(1)._2)))
//                       riskLocalesCurrent.+=((dates(dates.length-8), RiskLocale(InputOutput.mapLocaleToGeoMapName(localeType, locale), locale, metricsNow(MetricType.name(MetricType.DailyPer100k))(1)._2)))
                     }
                   }
                }
                case false => // do nothing
          }
          
          writeNewData match {
            case true => {
              val annotationsLocale = Annotations(Range(0,metricsForLocale.length).map(i => Annotation(dates(i), locale, lat, long,metricsForLocale(i))).toList)
              InputOutput.writeToFile(annotationsJsonImplicit.write(annotationsLocale).prettyPrint.getBytes, outputDirectoryNameAnnotations+locale+"_Annotations.json")
            }
            case false => // do not write
          }

        }
      }
    })
    
              
    createMapData match {
      case false => // don't write it
      case true => {
                val resultTOWrite = RiskLocalesTemporal(riskLocalesTemporal.toMap.toList.sortWith((y,z) => y._1<z._1).map(x => RiskLocales(x._1, x._2)))
                InputOutput.writeToFile(riskLocalesTemporalJsonImplicit.write(resultTOWrite).prettyPrint.getBytes, outputDirectoryNameMaps+"/"+localeType+"/RiskMapMultiDays.json")

//        InputOutput.writeToFile(riskLocalesJsonImplicit.write(RiskLocales(riskLocalesLastWeek(0)._1, riskLocalesLastWeek.toList.map(x => x._2))).prettyPrint.getBytes, outputDirectoryNameMaps+"/"+localeType+"/RiskMapLastWeek.json")
//        InputOutput.writeToFile(riskLocalesJsonImplicit.write(RiskLocales(riskLocalesCurrent(0)._1, riskLocalesCurrent.toList.map(x => x._2))).prettyPrint.getBytes, outputDirectoryNameMaps+"/"+localeType+"/RiskMap.json")
      }
    }    
  
    generateAlerts match {
      case false => // do nothing
      case true => {
        alertPercentMap.size > 0 match {
          case true => {
            writeAlerts(localeType, MeasureType.BiweeklyPercentChangeUptrend, alertPercentMap.toMap)
            writeAlerts(localeType, MeasureType.BiweeklyPercentChangeDowntrend, alertPercentMap.toMap, false)
          }
          case false => // do nothing
        }
        alertDailyMap.size > 0 match {
          case true => {
            writeAlerts(localeType, MeasureType.DailyHigh, alertDailyMap.toMap)
            writeAlerts(localeType, MeasureType.DailyLow, alertDailyMap.toMap, false)
          }
          case false => // do nothing
        }         
        alertTrendMap.size > 0 match {
          case true => writeAlerts(localeType, MeasureType.MACrossoverUptrend, alertTrendMap.toMap)
          case false => // do nothing
        }        
      }
    }
      
    writeNewData match {
      case true => {
        dcSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(dcSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"daily-snapshots/"+x._1+"_DailySnapshots.json"))
        ccSLocale.foreach(x => InputOutput.writeToFile(covidSnapshotsJsonImplicit.write(ccSLocale(x._1)).prettyPrint.getBytes, outputDirectoryNameSnapshots+"cumulative-snapshots/"+x._1+"_CumulativeSnapshots.json"))
      }
      case false => // do nothing
    }
  }
    
  def writeAlerts(localeType: String, 
      measure: MeasureType.Value, 
      alertMap: Map[String, Double], 
      upTrend: Boolean = true, 
      numLocalesToSelect: Int = 5) {
      
    val alertUIInfo = HelperFunctions.generateAlertUInfo(localeType, measure, alertMap.toMap, upTrend)
    val fileNameSuffix = "/" + MeasureType.name(measure) + "Alert.json"
    InputOutput.writeToFile(alertUIJsonImplicit.write(alertUIInfo).prettyPrint.getBytes,
        outputDirectoryNameAlerts+localeType+fileNameSuffix)
    }
    
  def getAlerts(localeType: String) = update(localeType, true, false)
      
  def alertsDaily() = {
    getAlerts("country")
    getAlerts("state")
    getAlerts("county")
    getAlerts("sandiegozipcode")
    }
    
  def updateDaily() = {
//    update("country", true)
//    update("state", true)
//    update("county", true)
    update("sandiegozipcode", true)
    }
    

  def main(args: Array[String]) {
	  updateDaily()
//    update("state", true)
//    update("country", true)
//    update("county", true, false, false)
//      InputOutput.sanDiegoZipCodePopulationMap.toList.sortWith((a,b) => a._1<b._1).foreach(x => println("    '"+x._1+"'"+","))
//      InputOutput.sanDiegoZipCodePopulationMap.toList.foreach(x => println(x._1+","+x._2))
  }
}
