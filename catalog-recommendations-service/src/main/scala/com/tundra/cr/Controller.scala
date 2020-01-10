package com.tundra.cr

import com.tundra.cr.Codecs._
import com.tundra.cr.http.definitions._

import vox.util.server.web.WebController

import com.twitter.finagle.http._

import javax.inject.Inject

class Controller @Inject()(
    frequentItemsRepository: FrequentItemsRepository,
    frequentStoresRepository: FrequentStoresRepository
) extends WebController
    with ControllerValidation {

  import com.tundra.cr.http.definitions.Codecs._

  val contextPath = "/api/catalog-recommendations"

  post(s"$contextPath/product") { implicit r: Request =>
    val req = requestBody[ProductRequest].valueOrError
    for {
      item <- frequentItemsRepository.getFrequentItems(req.productId, req.storeId, req.size)
      result = item.map(i => ProductResponse(msg = None, products = i.products, frequency = i.frequency))
    } yield result.map(response.ok.withJson(_)).getOrElse(response.notFound)
  }

  get(s"$contextPath/product/:productId/:storeId") { implicit r: Request =>
    val productId = requiredParam("productId").valueOrError
    val storeId   = requiredParam("storeId").valueOrError
    for {
      item <- frequentItemsRepository.getFrequentItemsAll(productId, storeId)
      result = item.map(i => ProductResponse(msg = None, products = i.products, frequency = i.frequency))
    } yield result.map(response.ok.withJson(_)).getOrElse(response.notFound)
  }

  post(s"$contextPath/store") { implicit r: Request =>
    val req = requestBody[StoreRequest].valueOrError
    for {
      item <- frequentStoresRepository.getFrequentStores(req.storeId, req.size)
      result = item.map(i => StoreResponse(msg = None, stores = i.stores, frequency = i.frequency))
    } yield result.map(response.ok.withJson(_)).getOrElse(response.notFound)
  }

  get(s"$contextPath/store/:storeId") { implicit r: Request =>
    val storeId = requiredParam("storeId").valueOrError
    for {
      item <- frequentStoresRepository.getFrequentStoresAll(storeId)
      result = item.map(i => StoreResponse(msg = None, stores = i.stores, frequency = i.frequency))
    } yield result.map(response.ok.withJson(_)).getOrElse(response.notFound)
  }

}
