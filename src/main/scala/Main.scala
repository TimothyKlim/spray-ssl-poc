import spray.json.DefaultJsonProtocol
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.http._
import HttpCharsets._
import MediaTypes._
import spray.json._
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._
import org.json4s.jackson.Serialization
import scala.util.{Success, Failure}
import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.io._
import javax.net.ssl._
import java.security.cert._
import java.security.{SecureRandom, KeyStore}

object Main extends App {

  implicit val system = ActorSystem("simple-spray-client")

  import system.dispatcher

  val log = Logging(system, getClass)
  val applicationId = "11111111111111111111111111111111"
  val login = "wowlogin"
  val token = "12222222222222222222"

  implicit val trustfulSslContext: SSLContext = {

    class IgnoreX509TrustManager extends X509TrustManager {
      def checkClientTrusted(chain: Array[X509Certificate], authType: String) {}
      def checkServerTrusted(chain: Array[X509Certificate], authType: String) {}
      def getAcceptedIssuers = null
    }

    val context = SSLContext.getInstance("TLS")
    context.init(null, Array(new IgnoreX509TrustManager), null)
    log.info("trustfulSslContex")
    context

  }

  implicit val trustEngineProvider = ClientSSLEngineProvider { engine =>
    engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
    engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
    log.info("trustEngineProvider")
    engine
  }

  log.info("Requesting...")

  val pipeline = sendReceive ~> unmarshal[String]
  val responseFuture = pipeline {
    Post("https://api-sandbox.direct.yandex.ru/json-api/v4/", "{}")
  }
  responseFuture onComplete {
    case Success(q) =>
      log.info(q)
      shutdown()

    case Failure(error) =>
      log.error(error, "Couldn't get elevation")
      shutdown()
  }


  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(10.seconds).await
    system.shutdown()
  }

}
