ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "fs2-aes",
    libraryDependencies ++= Seq(
      "co.fs2"        %% "fs2-core"            % "3.6.1",
      "org.scalameta" %% "munit"               % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck"    % "0.7.29" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7"  % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "1.0.4" % Test

    )
  )
