import java.io.File

import com.typesafe.sbt.GitPlugin.autoImport._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import sbt.Keys._

val customItSettings = Defaults.itSettings ++ inConfig(IntegrationTest)(Seq(
  scalaSource := baseDirectory.value / "it",
  resources := Seq(baseDirectory.value / "it" / "resources"),
  resourceDirectory := baseDirectory.value / "it" / "resources",
  fork in test := true,
  parallelExecution := false,
  javaOptions += s"-Dconfig.file=${baseDirectory.value}/it/resources/application.it.conf"
))

lazy val commonSettings = Seq(
  organization := "markland",
  scalaVersion := "2.11.11",

  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint", // Enable recommended additional warnings.
    "-Xcheckinit",
    "-Ywarn-dead-code" // Warn when dead code is identified.
  ),
  credentials += Credentials(Path.userHome / ".sbt" / ".bintrayCredentials"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    //  Resolver.bintrayRepo("marklandcompany", "releases"),
    "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
  ),
  javaOptions in Test += s"-Dconfig.file=${baseDirectory.value}/test/resources/application.test.conf"
)

lazy val dockerSettings = Seq(
  maintainer := "mkelly28@tcd.ie",
  dockerBaseImage := "registry.opensource.zalan.do/stups/openjdk:1.8.0-131-8",
  dockerRepository := Some("markymark1"),
  dockerExposedPorts := Seq(9000),
  dockerExposedVolumes := Seq("/opt/docker/logs"),
  daemonUser := "root",
  dockerImageCreationTask := (publishLocal in Docker).value
)

val MacwireVersion = "2.2.3"
lazy val macwireDeps = Seq(
  "com.softwaremill.macwire" %% "macros" % MacwireVersion % "provided",
  "com.softwaremill.macwire" %% "util" % MacwireVersion,
  "com.softwaremill.macwire" %% "proxy" % MacwireVersion
).map {
  _.excludeAll(
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
    ExclusionRule(organization = "javax.jms"),
    ExclusionRule(organization = "com.sun.jmx"),
    ExclusionRule(organization = "com.sun.jdmk"),
    ExclusionRule(organization = "org.jboss.logging"),
    ExclusionRule(organization = "com.typesafe.scala-logging")
  )
}

lazy val playDep = Seq("com.typesafe.play" %% "play-datacommons" % "2.5.10")
lazy val playWsDep = Seq("com.typesafe.play" %% "play-ws" % "2.4.11")

lazy val models = (project in file("play-basic-models"))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= playDep.map(_ % "provided")
  )

lazy val playBasicClient = (project in file("play-basic-client"))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .dependsOn(models)
  .settings(
    libraryDependencies ++= playDep.map(_ % "provided") ++ playWsDep.map(_ % "provided")
  )

lazy val protoSettings = Seq(
  PB.targets in Compile := Seq(
    scalapb.gen(flatPackage        = true,
      javaConversions    = true,
      singleLineToString = true) -> (sourceManaged in Compile).value / "proto_gen",
    PB.gens.java                 -> (sourceManaged in Compile).value / "proto_gen"
  ),
  PB.protoSources in Compile := Seq(file("protobuf"))
)

val AkkaVersion             = "2.4.18"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, ScmSourcePlugin, GitVersioning, DockerPlugin, DockerComposePlugin)
  .configs(IntegrationTest)
  .settings(customItSettings)
  .settings(commonSettings)
  .settings(protoSettings)
  .settings(dockerSettings)
  .settings(
    name := "play-basic",
    git.useGitDescribe := true,
    libraryDependencies ++= macwireDeps ++ playDep ++ playWsDep ++ Seq(
      cache,
      "nl.grons" %% "metrics-scala" % "3.5.5_a2.3",
      "io.dropwizard.metrics" % "metrics-json" % "3.1.2",
      "io.dropwizard.metrics" % "metrics-jvm" % "3.1.2",
      "io.dropwizard.metrics" % "metrics-logback" % "3.1.2",
      "io.prometheus" % "simpleclient_dropwizard" % "0.0.23",
      "io.prometheus" % "simpleclient_common" % "0.0.23",
      "com.blacklocus" % "metrics-cloudwatch" % "0.4.0",

      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-kafka" % "0.17",

      "com.typesafe.play.modules" %% "play-modules-redis" % "2.4.0",

      "org.apache.kafka" %% "kafka" % "1.0.0" excludeAll (
//        ExclusionRule(organization = "org.slf4j"),
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx"),
        ExclusionRule(organization = "javax.jms")),

      "com.typesafe.slick" %% "slick" % "3.2.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
      "com.github.tminglei" % "slick-pg_2.11" % "0.15.0-RC",
      "com.github.tminglei" %% "slick-pg_play-json" % "0.15.0-RC" excludeAll ExclusionRule(organization = "com.typesafe.play"),
      "com.github.tminglei" % "slick-pg_date2_2.11" % "0.15.0-M2",

      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "org.webjars" % "swagger-ui" % "2.2.5",
      "org.scalaz" %% "scalaz-core" % "7.2.2",

      "com.google.code.findbugs" % "jsr305" % "2.0.3",

      "info.batey.kafka" % "kafka-unit" % "0.6" % "test",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test,it",
      "org.mockito" % "mockito-core" % "2.3.7" % "test,it",
      "com.h2database" % "h2" % "1.4.187" % "test",
      "org.flywaydb" %% "flyway-play" % "3.0.1" % "test,it",
      "io.rest-assured" % "rest-assured" % "3.0.3" % "it",
      "org.scalaj" %% "scalaj-http" % "2.2.1" % "it"
    ),

    fork in run := true,

    routesGenerator := InjectedRoutesGenerator,
    routesImport := Seq(
      "com.markland.service.binders._",
      "com.markland.service.models._",
      "com.markland.service.tags.ids",
      "com.markland.service.tags.cursors",
      "com.markland.service.IdBindables._",
      "com.markland.service.CursorBindables._"
    )
  )
  .dependsOn(models, playBasicClient)
  .aggregate(models, playBasicClient)
  .settings(aggregate in Docker := false)

//To use 'dockerComposeTest' to run tests in the 'IntegrationTest' scope instead of the default 'Test' scope:
// 1) Package the tests that exist in the IntegrationTest scope
testCasesPackageTask := (sbt.Keys.packageBin in IntegrationTest).value
// 2) Specify the path to the IntegrationTest jar produced in Step 1
testCasesJar := artifactPath.in(IntegrationTest, packageBin).value.getAbsolutePath
// 3) Include any IntegrationTest scoped resources on the classpath if they are used in the tests
testDependenciesClasspath := {
  val fullClasspathCompile = (fullClasspath in Compile).value
  val classpathTestManaged = (managedClasspath in IntegrationTest).value
  val classpathTestUnmanaged = (unmanagedClasspath in IntegrationTest).value
  val testResources = (resources in IntegrationTest).value
  (fullClasspathCompile.files ++ classpathTestManaged.files ++ classpathTestUnmanaged.files ++ testResources).map(_.getAbsoluteFile).mkString(File.pathSeparator)
}