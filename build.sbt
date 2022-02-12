name := "Kindle clippings to Markdown"
version := "1.3"
scalaVersion := "2.13.8"

assembly / mainClass := Some("org.bruchez.olivier.kindleclippings.KindleClippings")
assembly / assemblyJarName := "kindle-clippings.jar"

ThisBuild / scalafmtOnCompile := true
