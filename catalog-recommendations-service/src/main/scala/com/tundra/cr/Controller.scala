package com.tundra.cr

import vox.util.server.web.WebController

import com.twitter.finagle.http._

class Controller extends WebController {
  val contextPath = "/api/catalog-recommendations"

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
