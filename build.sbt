import swaggerboot.playversion.SupportedPlayVersion
import java.io.File

name := "play-basic"

organization := "markland"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",     // Emit warning and location for usages of deprecated APIs.
  "-feature",         // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",       // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint",           // Enable recommended additional warnings.
  "-Xcheckinit",
  "-Ywarn-dead-code"  // Warn when dead code is identified.
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SwaggerGenerate, ScmSourcePlugin, GitVersioning, DockerPlugin, DockerComposePlugin)

git.useGitDescribe := true

credentials += Credentials(Path.userHome / ".sbt" / ".bintrayCredentials")
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
//  Resolver.bintrayRepo("marklandcompany", "releases"),
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Xcheckinit",
  "-Ywarn-dead-code" // Warn when dead code is identified.
)

val MacwireVersion = "2.2.3"

libraryDependencies ++= Seq(
  ws,
  "nl.grons"                  %% "metrics-scala"                % "3.5.5_a2.3",
  "io.dropwizard.metrics"     %  "metrics-json"                 % "3.1.2",
  "io.dropwizard.metrics"     %  "metrics-jvm"                  % "3.1.2",
  "io.dropwizard.metrics"     %  "metrics-logback"              % "3.1.2",

  "com.typesafe.slick" %% "slick" % "3.2.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "com.github.tminglei" % "slick-pg_2.11" % "0.15.0-RC",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.15.0-RC" excludeAll ExclusionRule(organization = "com.typesafe.play"),
  "com.github.tminglei" % "slick-pg_date2_2.11" % "0.15.0-M2",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.webjars" % "swagger-ui" % "2.2.5",
  "org.scalaz" %% "scalaz-core" % "7.2.2",

  "com.softwaremill.macwire" %% "macros" % MacwireVersion % "provided",
  "com.softwaremill.macwire" %% "util" % MacwireVersion,
  "com.softwaremill.macwire" %% "proxy" % MacwireVersion,

  "com.typesafe.play" %% "play-datacommons" % "2.5.10",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.mockito" % "mockito-core" % "2.3.7" % "test",
  "com.h2database" % "h2" % "1.4.187" % "test",
  "org.flywaydb" %% "flyway-play" % "3.0.1" % "test"
)

fork in run := true

// Swagger boostrap settings
swaggerSourceDirectory := new File("swagger")
swaggerAutoUpdateSwaggerJson := Some("public/swagger.json")
swaggerServiceSpecName := Some("api.yaml")
swaggerGenerateControllerStubs := false
swaggerGenerateClient := true
swaggerUpdatePlayRoutes := false
swaggerEnumVendorExtensionName := Some("x-extensible-enum")
swaggerTaggedAttributes := Seq("label", "connectives", "md5")
swaggerPlayVersion := SupportedPlayVersion.Play25

//Docker stuff:
maintainer in Docker := "mkelly28@tcd.ie"
dockerBaseImage := "registry.opensource.zalan.do/stups/openjdk:8u91-b14-1-22"
//dockerRepository in Docker := Some("")
dockerExposedPorts in Docker := Seq(9000)
dockerExposedVolumes in Docker := Seq("/opt/docker/logs")
daemonUser in Docker := "root"

routesGenerator := InjectedRoutesGenerator
routesImport := Seq(
  "api.service.binders._",
  "api.service.models._",
  "api.service.tags.ids",
  "api.common.IdBindables._"
)