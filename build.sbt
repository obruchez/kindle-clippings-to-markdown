name := "Kindle clippings to Markdown"
version := "1.3"
scalaVersion := "2.13.3"

mainClass in assembly := Some("org.bruchez.olivier.kindleclippings.KindleClippings")

assemblyJarName in assembly := "kindle-clippings.jar"

scalafmtOnCompile in ThisBuild := true
