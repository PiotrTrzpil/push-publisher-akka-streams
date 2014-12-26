import scalariform.formatter.preferences._

name := "push-publisher-akka-streams"

version := "1.0"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-M2",
  "org.reactivestreams" % "reactive-streams-tck" %  "1.0.0.M3" % "test",
   "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)
