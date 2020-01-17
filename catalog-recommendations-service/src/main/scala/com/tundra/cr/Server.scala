package com.tundra.cr

import com.tundra.catalog.recommendations.service.BuildInfo

import vox.util.server.VoxServer
import vox.util.server.common.HasDevelopment
import vox.util.server.web.BuildInfoController
import vox.aws.dynamo.AmazonDynamoDB
import vox.api.repo.{AWSWrapperConfig, DynamoDBConfig}

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.inject.TwitterModule

import com.amazonaws.regions.Regions

import com.google.inject.{Provides, Singleton}

object Module extends TwitterModule {
  override def configure(): Unit = {
    super.configure()
    bind[Regions].toInstance(Regions.EU_WEST_1)
    bind[FrequentItemsRepository].to[FrequentItemsRepositoryDynamo]
    bind[FrequentStoresRepository].to[FrequentStoresRepositoryDynamo]
  }

  @Singleton
  @Provides
  def dynamoDbConfig(dbRegion: Regions): DynamoDBConfig = new DynamoDBConfig {
    override lazy val client = new AmazonDynamoDB(
      if (prefix == "local")
        AWSWrapperConfig.createClientLocal(8000, executors.dynamoDBExecutor)
      else if (prefix == "unit")
        AWSWrapperConfig.createClientUnit(8001, executors.dynamoDBExecutor)
      else
        AWSWrapperConfig.createClientRegional(dbRegion)
    )
    override def prefix         = config.dynamoPrefix()
    override val recreateTables = config.recreateTables()
  }
}

object Server extends VoxServer with HasDevelopment {
  override def modules: Seq[com.google.inject.Module] =
    super.modules ++ Seq(Module)

  override def configureHttp(router: HttpRouter): Unit = {
    super.configureHttp(router)
    router.add(buildController)
    router.add[Controller]
  }

  def buildController = BuildInfoController(
    "",
    Map(
      "name"          -> BuildInfo.name,
      "version"       -> BuildInfo.version,
      "isDevelopment" -> isDevelopment
    )
  )
}
