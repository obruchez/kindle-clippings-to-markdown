name := "Kindle clippings to Markdown"
version := "1.2"
scalaVersion := "2.12.8"

mainClass in assembly := Some("org.bruchez.olivier.kindleclippings.KindleClippings")

assemblyJarName in assembly := "kindle-clippings.jar"

scalafmtOnCompile in ThisBuild := true
