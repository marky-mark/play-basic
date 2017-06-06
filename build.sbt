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

val customItSettings = Defaults.itSettings ++ inConfig(IntegrationTest)(Seq(
  scalaSource := baseDirectory.value / "it",
  resources := Seq(baseDirectory.value / "it" / "resources"),
  resourceDirectory := baseDirectory.value / "it" / "resources",
  fork in test := true,
  parallelExecution := false,
  javaOptions += s"-Dconfig.file=${baseDirectory.value}/it/resources/application.it.conf"
))


lazy val root = (project in file("."))
  .enablePlugins(PlayScala, ScmSourcePlugin, GitVersioning, DockerPlugin, DockerComposePlugin)
  .configs(IntegrationTest)
  .settings(customItSettings)

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

libraryDependencies ++= macwireDeps ++ Seq(
  "com.typesafe.play" %% "play-ws"              % "2.4.11",
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

  "com.typesafe.play" %% "play-datacommons" % "2.5.10",
  "com.google.code.findbugs" % "jsr305" % "2.0.3",

  "org.scalatest" %% "scalatest" % "2.2.4" % "test,it",
  "org.mockito" % "mockito-core" % "2.3.7" % "test,it",
  "com.h2database" % "h2" % "1.4.187" % "test",
  "org.flywaydb" %% "flyway-play" % "3.0.1" % "test,it",
  "io.rest-assured" % "rest-assured" % "3.0.3" % "it",
  "org.scalaj" %% "scalaj-http" % "2.2.1" % "it"
)

fork in run := true

//Docker stuff:
maintainer in Docker := "mkelly28@tcd.ie"
dockerBaseImage := "registry.opensource.zalan.do/stups/openjdk:8u91-b14-1-22"
//dockerRepository in Docker := Some("")
dockerExposedPorts in Docker := Seq(9000)
dockerExposedVolumes in Docker := Seq("/opt/docker/logs")
daemonUser in Docker := "root"

dockerImageCreationTask := (publishLocal in Docker).value

routesGenerator := InjectedRoutesGenerator
routesImport := Seq(
  "api.service.binders._",
  "api.service.models._",
  "api.service.tags.ids",
  "api.common.IdBindables._"
)