name := "MyApp"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
   "com.google.code.gson" % "gson" % "2.2.4"
)     

play.Project.playScalaSettings
