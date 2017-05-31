resolvers ++= Seq(
  Resolver.url("zalando-sbt-plugins", url("https://dl.bintray.com/zalando/sbt-plugins/"))(Resolver.ivyStylePatterns)
)

credentials += Credentials(Path.userHome / ".sbt" / ".bintrayCredentials")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")

addSbtPlugin("de.zalando.buffalo" % "swagger-bootstrapper" % "0.4.0")

addSbtPlugin("ie.zalando.buffalo" % "sbt-scm-source" % "0.0.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.22")