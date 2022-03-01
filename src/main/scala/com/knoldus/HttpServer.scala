package com.knoldus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.pinpoint.PinpointClient

import scala.util.{Failure, Success}

object HttpServer extends App {

  val host = "localhost"
  val port = 7000
  val pinpointApi = new PinpointApi

  implicit val system = ActorSystem("HTTP_SERVER")
  implicit val materializer: ActorMaterializer.type = ActorMaterializer

  import system.dispatcher

  val route =
    pathPrefix("api") {
      get {
        path("hello") {
          complete(StatusCodes.OK, "Hello from Server")
        } ~
          path("send") {
            val pinpoint = PinpointClient.builder()
              .region(Region.US_EAST_1)
              .build()
            val originationNumber: String = "+19027016862"
            val destinationNumber: String = "+916395165593"
            pinpointApi.send(pinpoint, "hi, how are you doing? ", "00b747a947994dd39f6caf23fd541478", originationNumber, destinationNumber)
            complete(StatusCodes.OK, "Send")
          }
      }
    }

  val binding = Http().newServerAt(host, port).bindFlow(route)

  binding.onComplete {
    case Success(value) =>
      println(s"Server is listening on http://$host:${port}")
    case Failure(exception) =>
      println(s"Failure : $exception")
      system.terminate()
  }
}
