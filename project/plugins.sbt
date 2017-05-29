resolvers ++= Seq(
  Resolver.url("zalando-sbt-plugins", url("https://dl.bintray.com/zalando/sbt-plugins/"))(Resolver.ivyStylePatterns)
)

credentials += Credentials(Path.userHome / ".sbt" / ".bintrayCredentials")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")

addSbtPlugin("de.zalando.buffalo" % "swagger-bootstrapper" % "0.1.5")

addSbtPlugin("ie.zalando.buffalo" % "sbt-scm-source" % "0.0.5")

addSbtPlugin("ie.zalando.buffalo" % "play-json-schema" % "0.0.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")