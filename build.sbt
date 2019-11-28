import sbt.Keys.{publishArtifact, publishLocal}

enablePlugins(
  GitVersioning
)

scalaVersion := "2.12.4"

val commonSettings =
  Seq(
    scalaVersion := "2.12.4",
    organization := "com.tundra",
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-Xlog-reflective-calls"
    ),
    publishArtifact in packageDoc := false,
    publishArtifact in packageSrc := false,
    publishArtifact in Test := false,
    publishMavenStyle := true,
    publishTo := {
      if (isSnapshot.value)
        Some("snapshots" at "https://nexus.tundra-shared.com/repository/maven-snapshots/")
      else
        Some("releases" at "https://nexus.tundra-shared.com/repository/maven-releases/")
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishArtifact in packageDoc := false,
    publishArtifact in packageSrc := false,
    publishArtifact in Test := false,
    resolvers := Seq(
      "Tundra Nexus" at "https://nexus.tundra-shared.com/repository/tundra/",
      Resolver.url("vox-code", url("http://vox-releases.s3-website-eu-west-1.amazonaws.com/"))(
        Resolver.ivyStylePatterns
      ),
      Resolver.url("vox-public-ivy", url("https://nexus.tundra-shared.com/repository/vox-plugins/"))(
        Resolver.ivyStylePatterns
      ),
      "Redshift" at "http://redshift-maven-repository.s3-website-us-east-1.amazonaws.com/release",
      Resolver.bintrayRepo("ohnosequences", "maven"),
      "Tundra releases" at "https://nexus.tundra-shared.com/repository/maven-releases/"
    )
  )

skip in publish := true

lazy val disableDockerSettings = Seq(
  publish in Docker := {},
  publishLocal in Docker := {}
)

lazy val toolSettings = commonSettings

lazy val dockerTools = toolSettings

lazy val releaseSettings = Seq(
  releaseUseGlobalVersion := false,
  releaseVersionFile := file(name.value + "/version.sbt")
)

lazy val `analytics-client` = project
  .settings(commonSettings ++ disableDockerSettings ++ releaseSettings)

lazy val `analytics-service` = project
  .dependsOn(`analytics-client`)
  .settings(commonSettings)

// aggregates the analytics
lazy val root = project
  .in(file("."))
  .withId("analytics")
  .aggregate(`analytics-client`, `analytics-service`)
