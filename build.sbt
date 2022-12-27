name := """play-game"""
organization := "com.yadavan88"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

scalaVersion := "2.13.10"

libraryDependencies += guice
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.postgresql" % "postgresql" % "42.5.1"
libraryDependencies += "org.webjars" % "swagger-ui" % "4.11.1"

swaggerDomainNameSpaces := Seq("models")

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
