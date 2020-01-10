package com.tundra.cr

import java.util.concurrent._
import scala.concurrent.ExecutionContext

object executors {

  // thread pool for AWS DynamoDB
  val dynamoDBExecutor =
    new ThreadPoolExecutor(
      config.dynamoDbThreadPoolCoreSize(),
      config.dynamoDbThreadPoolMaxSize(),
      config.dynamoDbThreadPoolKeepAlive(),
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable]
    )
  val dynamoDBExecutionContext =
    ExecutionContext.fromExecutor(dynamoDBExecutor)

}
