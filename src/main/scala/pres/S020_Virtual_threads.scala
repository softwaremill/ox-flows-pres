package pres

import ox.discard

import java.util.concurrent.ConcurrentHashMap

@main def s020_Virtual_threads(): Unit =
  timedWithLabel("loom") {
    val threads = new Array[Thread](10_000_000)
    val results = ConcurrentHashMap.newKeySet[Int]()

    for (i <- threads.indices)
      threads(i) = Thread
        .ofVirtual()
        .start(() => results.add(0).discard);

    threads.foreach(_.join())
  }
