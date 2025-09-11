val Scala213 = "2.13.16"
val Scala3   = "3.7.3"

ThisBuild / tlBaseVersion := "0.3"
ThisBuild / scalaVersion  := Scala213

ThisBuild / crossScalaVersions := Seq(Scala213, Scala3)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / crossPaths        := true

ThisBuild / tlCiReleaseBranches        := Seq()
ThisBuild / tlCiHeaderCheck            := false
ThisBuild / tlCiScalafixCheck          := true
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"), JavaSpec.temurin("21"))
ThisBuild / tlJdkRelease               := Some(11)

ThisBuild / organization  := "me.wojnowski"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / licenses      := Seq(License.MIT)
ThisBuild / developers    := List(
  tlGitHubDev("jwojnowski", "Jakub Wojnowski")
)

val commonSettings = Seq(
  makePom / publishArtifact := true
)

lazy val root = (project in file("."))
  .settings(
    name := "fs2-aes",
    libraryDependencies ++= Seq(
      "co.fs2"        %% "fs2-core"                % "3.12.2",
      "org.scalameta" %% "munit"                   % "1.1.1"    % Test,
      "org.scalameta" %% "munit-scalacheck"        % "1.2.0"    % Test,
      "org.typelevel" %% "munit-cats-effect"       % "2.1.0"    % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test
    )
  )
