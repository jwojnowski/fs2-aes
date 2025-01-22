val Scala213 = "2.13.16"
val Scala3   = "3.6.3"

ThisBuild / scalaVersion := Scala213

ThisBuild / crossScalaVersions := Seq(Scala213, Scala3)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / crossPaths        := true

inThisBuild(
  List(
    organization           := "me.wojnowski",
    homepage               := Some(url("https://github.com/jwojnowski/fs2-aes")),
    licenses               := List("MIT License" -> url("https://opensource.org/licenses/MIT")),
    developers             := List(
      Developer(
        "jwojnowski",
        "Jakub Wojnowski",
        "29680262+jwojnowski@users.noreply.github.com",
        url("https://github.com/jwojnowski")
      )
    ),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository     := "https://s01.oss.sonatype.org/service/local",
    versionScheme          := Some("early-semver")
  )
)

val commonSettings = Seq(
  makePom / publishArtifact := true
)

lazy val root = (project in file("."))
  .settings(
    name := "fs2-aes",
    libraryDependencies ++= Seq(
      "co.fs2"        %% "fs2-core"                % "3.11.0",
      "org.scalameta" %% "munit"                   % "1.0.4"    % Test,
      "org.scalameta" %% "munit-scalacheck"        % "1.0.0"    % Test,
      "org.typelevel" %% "munit-cats-effect"       % "2.0.0"    % Test,
      "org.typelevel" %% "scalacheck-effect-munit" % "2.0.0-M2" % Test
    )
  )

ThisBuild / mimaPreviousArtifacts := previousStableVersion.value.map(organization.value %% moduleName.value % _).toSet
