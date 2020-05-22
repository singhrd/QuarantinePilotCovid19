//=============================================================================
// Standard values needed for the project.
name := "covid19"

version := "1.0"

scalaVersion := "2.10.6"
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


//=============================================================================
// Versions of the stuff we use for the project.

val scalaTestVersion = "3.0.0"

//=============================================================================
// Stuff dealing with ScalaTest dependencies

libraryDependencies += "org.scalactic" %% "scalactic" % scalaTestVersion


libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


//=============================================================================
// Stuff dealing with Json Parsing 
//libraryDependencies += "com.typesafe.play" %% "play-json" % "2.4.10"
libraryDependencies += "io.spray" %% "spray-json" % "1.3.3"
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

