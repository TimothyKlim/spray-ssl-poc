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
import akka.util.Timeout
import spray.http._
import HttpMethods._
import spray.can.Http._

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

  implicit val clientSSLEngineProvider =
    ClientSSLEngineProvider {
      _ =>
        val engine = trustfulSslContext.createSSLEngine()
        engine.setUseClientMode(true)
        engine
    }

  log.info("Requesting...")

  private implicit val timeout: Timeout = 5.seconds

  val connector = HostConnectorSetup(host = "api-sandbox.direct.yandex.ru", port = 443, sslEncryption = true)

  val hQuery = Post("/json-api/v4/", "{}")

  for {
    Http.HostConnectorInfo(hostConnector, _) <- IO(Http) ? connector
    response <- hostConnector.ask(hQuery).mapTo[HttpResponse]
    _ <- hostConnector ? Http.CloseAll
  } yield {

    system.log.info("Host-Level API: received {} response with {} bytes",
      response.status, response.entity.data.length)

    response.header[HttpHeaders.Server].get.products.head
    println(s"body: ${response.entity.data.asString}")

    shutdown()
  }


  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(10.seconds).await
    system.shutdown()
  }

}
