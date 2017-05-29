import swaggerboot.playversion.SupportedPlayVersion

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
  .enablePlugins(PlayScala, SwaggerGenerate, ScmSourcePlugin, GitVersioning, DockerPlugin)

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
  "org.webjars" % "swagger-ui" % "2.2.5",
  "org.scalaz" %% "scalaz-core" % "7.2.2",
  "com.softwaremill.macwire" %% "macros" % MacwireVersion % "provided",
  "com.softwaremill.macwire" %% "util" % MacwireVersion,
  "com.softwaremill.macwire" %% "proxy" % MacwireVersion,
  "com.typesafe.play" %% "play-datacommons" % "2.5.10"
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
dockerRepository in Docker := Some("pierone.stups.zalan.do/setanta")
dockerExposedPorts in Docker := Seq(9000)
dockerExposedVolumes in Docker := Seq("/opt/docker/logs")
daemonUser in Docker := "root"

routesGenerator := InjectedRoutesGenerator
routesImport := Seq(
  "api.service.binders._",
  "api.service.models._",
  "api.service.tags.{ids => svcids}",
  "api.common.IdBindables._"
)