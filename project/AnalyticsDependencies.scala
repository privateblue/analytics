import sbt._

/**
  * Put common dependencies for the buy project modules here
  */
object AnalyticsDependencies {
  object Versions {
    val common     = "10.5.22"
    val logback    = "1.2.3"
    val pureConfig = "0.11.0"
    val scalaTest  = "3.0.5"
    val aws        = "1.11.628"
  }

  val voxAwsAuth    = "vox" %% "aws-auth"    % Versions.common
  val voxAwsDynamo  = "vox" %% "aws-dynamo"  % Versions.common
  val voxAwsScala   = "vox" %% "aws-scala"   % Versions.common
  val voxUtilServer = "vox" %% "util-server" % Versions.common
}
