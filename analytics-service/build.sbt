import vox.common.Dependencies.versions
import AnalyticsDependencies.Versions

enablePlugins(
  VoxPlugin,
  DockerPlugin,
  JavaServerAppPackaging
)

mainClass in Compile := Some("com.tundra.analytics.Server")

bashScriptExtraDefines +=
  """
    |# decrypt JAVA_OPTS content just before running the app, but ignore it for local environments where gruntkms is not used
    |export JAVA_OPTS=$(gruntkms decrypt --aws-region "$AWS_REGION" --ciphertext "$JAVA_OPTS" || echo "$JAVA_OPTS")
    |export AWS_AZ=$(curl http://169.254.169.254/latest/meta-data/placement/availability-zone || echo "nozone")
  """.stripMargin

skip in publish := true

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig"                 % Versions.pureConfig,
  "com.github.pureconfig" %% "pureconfig-cats-effect"     % Versions.pureConfig,
  "org.scalatest"         %% "scalatest"                  % Versions.scalaTest % "test",
  "com.twitter"           %% "util-core"                  % versions.finatra,
  "com.twitter"           %% "bijection-util"             % "0.9.6",
  "vox"                   %% "util-server"                % Versions.common,
  "ch.qos.logback"        % "logback-classic"             % Versions.logback,
  "ch.qos.logback"        % "logback-core"                % Versions.logback,
  "com.amazonaws"         % "aws-java-sdk-secretsmanager" % Versions.aws
)

dependencyOverrides ++= Seq(
  "com.twitter" %% "util-core" % versions.finatra
)
