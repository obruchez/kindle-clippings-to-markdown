logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.beautiful-scala" % "sbt-scalastyle" % "1.5.1")