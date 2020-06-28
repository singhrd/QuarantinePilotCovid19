package quarantine.covid19.core

import spray.json.DefaultJsonProtocol
import scala.reflect.ClassManifestFactory.classType

/** This trait holds implicit functions that provide formats for unmarshalling and marshalling JSON 
  * arguments.
  * 
  * We use Scala Case Classes to hold/define the schema for conversion to and from json.
  * 
  * As an example:
  * 
  * Case Class Example(type1: nameVar1, type2: nameVar2, ........, typeN: nameVarN)
  * 
  * Since the sample case class has N fields, we use jsonFormat[N] below 
  * implicit val exampleJsonImplicit = jsonFormatN(SampleJson)
  * 
  * Look at the SampleJson for an example when N = 2
  * 
  * @author rajdeep
  */
  
trait JsonSupport extends DefaultJsonProtocol {
  implicit val covidSnapshotJsonImplicit = jsonFormat16(CovidSnapshot)
  implicit val annotationJsonImplicit = jsonFormat5(Annotation)
  implicit val annotationsJsonImplicit = jsonFormat1(Annotations)
  implicit val covidSnapshotsJsonImplicit = jsonFormat1(CovidSnapshots)
//  implicit val alertUIInfoJsonImplicit = jsonFormat2(AlertUIInfo)
  implicit val alertJsonImplicit = jsonFormat6(Alert)
  
  
}

