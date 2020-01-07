package com.tundra.cr

import com.tundra.catalog.recommendations.service.BuildInfo

import vox.util.server.VoxServer
import vox.util.server.common.HasDevelopment
import vox.util.server.web.BuildInfoController

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.routing.HttpRouter

object Server extends VoxServer with HasDevelopment {

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
