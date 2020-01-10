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

import com.google.inject.{Module, Provides, Singleton}

object Server extends VoxServer with HasDevelopment {

  object Module extends TwitterModule {

    override def configure(): Unit = {
      bind[Regions].toInstance(Regions.EU_WEST_1)
      super.configure()
      bind[FrequentItemsRepository].to[FrequentItemsRepositoryDynamo]
      bind[FrequentStoresRepository].to[FrequentStoresRepositoryDynamo]
    }

    @Singleton
    @Provides
    def dynamoDbConfig(dbRegion: Regions): DynamoDBConfig = new DynamoDBConfig {
      override lazy val client = new AmazonDynamoDB(dynamodbClient)

      def dynamodbClient =
        // local is for running server in dev local env where data needs to be persisted between runs
        if (prefix == "local") AWSWrapperConfig.createClientLocal(8000, executors.dynamoDBExecutor)
        // for integration testing where data is not persisted
        else if (prefix == "unit") AWSWrapperConfig.createClientUnit(8001, executors.dynamoDBExecutor)
        // real deployed environment
        else AWSWrapperConfig.createClientRegional(dbRegion)

      override def prefix: String = {
        val p = config.dynamoPrefix()
        assert(p.nonEmpty, "dynamoPrefix is required")
        p
      }

      override val recreateTables: Boolean = config.recreateTables()
    }

  }

  override def modules: Seq[Module] =
    super.modules ++ Seq(Module)

  override def configureHttp(router: HttpRouter): Unit = {
    super.configureHttp(router)
    router.add(
      BuildInfoController(
        "",
        Map(
          "name"          -> BuildInfo.name,
          "version"       -> BuildInfo.version,
          "isDevelopment" -> isDevelopment
        )
      )
    )
    router.add[Controller]
  }

}
