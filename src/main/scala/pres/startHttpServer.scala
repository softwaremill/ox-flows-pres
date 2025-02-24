package pres

import ox.sleep
import sttp.tapir.*
import sttp.tapir.server.netty.sync.NettySyncServer
import scala.util.Random
import scala.concurrent.duration.DurationInt

@main def startHttpServer(): Unit =
  val work1Endpoint = endpoint.post
    .in("work1")
    .in(stringBody)
    .out(stringBody)
    .handleSuccess { in =>
      sleep(Random.nextInt(1000).milliseconds)
      in.reverse
    }

  val work2Endpoint = endpoint.post
    .in("work2")
    .in(stringBody)
    .out(stringBody)
    .handleSuccess { in =>
      sleep(Random.nextInt(1000).milliseconds)
      in.hashCode().toString
    }

  NettySyncServer().addEndpoints(List(work1Endpoint, work2Endpoint)).startAndWait()
