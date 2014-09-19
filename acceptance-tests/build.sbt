import Common._

name := "vehicles-acquire-acceptance-tests"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

libraryDependencies ++= Seq(
  "info.cukes" %% "cucumber-scala" % "1.1.7" % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-java" % "1.1.7" % "test" withSources() withJavadoc(),
//  "info.cukes" % "cucumber-picocontainer" % "1.1.8" % "test" withSources() withJavadoc(),
  "info.cukes" % "cucumber-junit" % "1.1.7" % "test" withSources() withJavadoc()
//  "junit" % "junit" % "4.11" % "test" withSources() withJavadoc(),
//  "junit" % "junit-dep" % "4.10" % "test" withSources() withJavadoc(),
//  "com.novocode" % "junit-interface" % "0.10" % "test" withSources() withJavadoc()
)
