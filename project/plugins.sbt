resolvers ++= Seq(
  Resolver.url("zalando-sbt-plugins", url("https://dl.bintray.com/zalando/sbt-plugins/"))(Resolver.ivyStylePatterns)
)

credentials += Credentials(Path.userHome / ".sbt" / ".bintrayCredentials")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")

addSbtPlugin("ie.zalando.buffalo" % "sbt-scm-source" % "0.0.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.34")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

//addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.3")
