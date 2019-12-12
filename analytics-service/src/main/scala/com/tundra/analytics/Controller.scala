package com.tundra.analytics

import vox.util.server.web.WebController

import com.twitter.finagle.http._

object Controller extends WebController {
  val contextPath = "/api/analytics"

  post(s"$contextPath/product") { implicit request: Request =>
    response.noContent
  }

  post(s"$contextPath/store") { implicit request: Request =>
    response.noContent
  }

  get(s"$contextPath/product/:productId") { implicit request: Request =>
    response.noContent
  }

  get(s"$contextPath/store/:storeId") { implicit request: Request =>
    response.noContent
  }
}
