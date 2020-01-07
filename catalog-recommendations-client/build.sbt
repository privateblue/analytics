import CatalogRecommendationsDependencies.Versions

enablePlugins(
  json.schema.codegen.Plugin
)

libraryDependencies ++= Seq(
  "io.argonaut" %% "argonaut" % Versions.argonaut
)