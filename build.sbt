import de.johoop.jacoco4sbt.JacocoPlugin._
import org.scalastyle.sbt.ScalastylePlugin
import templemore.sbt.cucumber.CucumberPlugin
import Sandbox.runMicroServicesTask
import Sandbox.sandboxTask
import Sandbox.runAsyncTask
import Sandbox.testGatlingTask
import Sandbox.sandboxAsyncTask
import Sandbox.gatlingTask
import Sandbox.gatlingTests
import Sandbox.vehiclesLookup
import net.litola.SassPlugin
import Common._

name := "vehicles-acquire-online"

version := versionString

organization := organisationString

organizationName := organisationNameString

scalaVersion := scalaVersionString

scalacOptions := scalaOptionsSeq

publishTo.<<=(publishResolver)

credentials += sbtCredentials

resolvers ++= projectResolvers

lazy val root = (project in file(".")).enablePlugins(PlayScala, SassPlugin, SbtWeb)

lazy val acceptanceTestsProject = Project("acceptance-tests", file("acceptance-tests"))
  .dependsOn(root % "test->test")
  .disablePlugins(PlayScala, SassPlugin, SbtWeb)

libraryDependencies ++= Seq(
  cache,
  filters,
  "dvla" %% "vehicles-presentation-common" % "2.2" withSources() withJavadoc() exclude("junit", "junit-dep"),
  "com.google.guava" % "guava" % "15.0" withSources() withJavadoc(), // See: http://stackoverflow.com/questions/16614794/illegalstateexception-impossible-to-get-artifacts-when-data-has-not-been-loaded
  "org.seleniumhq.selenium" % "selenium-java" % "2.42.2" % "test" withSources() withJavadoc(),
  "com.github.detro" % "phantomjsdriver" % "1.2.0" % "test" withSources() withJavadoc(),
  "org.mockito" % "mockito-all" % "1.9.5" % "test" withSources() withJavadoc(),
  "org.slf4j" % "log4j-over-slf4j" % "1.7.7" % "test" withSources() withJavadoc(),
  "org.scalatest" %% "scalatest" % "2.2.1" % "test" withSources() withJavadoc(),
  "com.google.inject" % "guice" % "4.0-beta4" withSources() withJavadoc(),
  "com.tzavellas" % "sse-guice" % "0.7.1" withSources() withJavadoc(), // Scala DSL for Guice
  "commons-codec" % "commons-codec" % "1.9" withSources() withJavadoc(),
  "org.apache.httpcomponents" % "httpclient" % "4.3.4" withSources() withJavadoc(),
  "org.webjars" % "requirejs" % "2.1.14-1")

pipelineStages := Seq(rjs, digest, gzip)

CucumberPlugin.cucumberSettings ++
  Seq (
    CucumberPlugin.cucumberFeaturesLocation := "./test/acceptance/acquire/",
    CucumberPlugin.cucumberStepsBasePackage := "helpers.steps",
    CucumberPlugin.cucumberJunitReport := false,
    CucumberPlugin.cucumberHtmlReport := false,
    CucumberPlugin.cucumberPrettyReport := false,
    CucumberPlugin.cucumberJsonReport := false,
    CucumberPlugin.cucumberStrict := true,
    CucumberPlugin.cucumberMonochrome := false
  )

val myTestOptions =
  if (System.getProperty("include") != null ) {
    Seq(testOptions in Test += Tests.Argument("include", System.getProperty("include")))
  } else if (System.getProperty("exclude") != null ) {
    Seq(testOptions in Test += Tests.Argument("exclude", System.getProperty("exclude")))
  } else Seq.empty[Def.Setting[_]]

myTestOptions

// If tests are annotated with @LiveTest then they are excluded when running sbt test
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "helpers.tags.LiveTest")

javaOptions in Test += System.getProperty("waitSeconds")

concurrentRestrictions in Global := Seq(Tags.limit(Tags.CPU, 4), Tags.limit(Tags.Network, 10), Tags.limit(Tags.Test, 4))

sbt.Keys.fork in Test := false

jacoco.settings

parallelExecution in jacoco.Config := false

// Using node to do the javascript optimisation cuts the time down dramatically
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

// Disable documentation generation to save time for the CI build process
sources in doc in Compile := List()

ScalastylePlugin.Settings

net.virtualvoid.sbt.graph.Plugin.graphSettings

runMicroServicesTask

sandboxTask

runAsyncTask

testGatlingTask

sandboxAsyncTask

gatlingTask

lazy val p1 = osAddressLookup.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p2 = vehiclesLookup.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p4 = legacyStubs.disablePlugins(PlayScala, SassPlugin, SbtWeb)
lazy val p7 = gatlingTests.disablePlugins(PlayScala, SassPlugin, SbtWeb)
