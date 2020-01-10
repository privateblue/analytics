package com.tundra.cr.config

import com.twitter.app.{Flaggable, GlobalFlag}

object dynamoPrefix
    extends GlobalFlag[String](
      "dynamo db prefix to namespace tables per environment"
    )

object recreateTables
    extends GlobalFlag[Boolean](
      false,
      "WARNING: this drops and recreates all tables on startup"
    )

object dynamoDbThreadPoolCoreSize
    extends GlobalFlag[Int](
      5,
      "Minimum size of thread pool dedicated to DynamoDB"
    )

object dynamoDbThreadPoolMaxSize
    extends GlobalFlag[Int](
      50,
      "Maximum size of thread pool dedicated to DynamoDB"
    )

object dynamoDbThreadPoolKeepAlive
    extends GlobalFlag[Long](
      60,
      "Keep-alive time in seconds of threads in thread pool dedicated to DynamoDB"
    )
