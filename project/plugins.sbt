resolvers := Seq(
  "Tundra Nexus" at "https://nexus.tundra-shared.com/repository/tundra/",
  "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com",
  "Tundra Plugins" at "https://nexus.tundra-shared.com/repository/vox-plugins/",
  "releases" at "https://nexus.tundra-shared.com/repository/maven-releases/",
  "Tundra releases" at "https://nexus.tundra-shared.com/repository/maven-releases/",
  Resolver.bintrayRepo("ohnosequences", "maven"),
  Resolver.bintrayRepo("sbt", "sbt-plugin-releases")
)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

addSbtPlugin("vox" %% "sbt-vox" % "9.6.0")

addSbtPlugin("com.voxsupplychain" %% "json-schema-codegen-sbt" % "0.6.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-license-report" % "1.2.0")
