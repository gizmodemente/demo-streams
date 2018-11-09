package demo.streams

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString

import scala.concurrent.Future

case class LineElement(val1: String, val2: String, val3: String) {
  override def toString: String = s"Val 1: $val1, Val 2: $val2, Val 3: $val3"
}

object FileStreamDemoApp extends App {
  implicit val actorSystem = ActorSystem("DemoSystem")
  implicit val materializer = ActorMaterializer()

  val file = Paths.get("example.txt")
  println("Reading file: " + file.toAbsolutePath)

  val fileSource = FileIO.fromPath(file)

  val lineFlow = Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String)
  val elementFlow = Flow[String].map(s => s.split("#").map(_.trim))
  val objectFlow = Flow[Array[String]].map(l => LineElement(l(0), l(1), l(2)))

  val sink = Sink.foreach(println)

  fileSource.
    via(lineFlow).via(elementFlow).via(objectFlow).
    to(sink).run()

  fileSource.
    via(lineFlow).
    to(sink).run()

  actorSystem.terminate
}
