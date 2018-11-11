package demo.streams

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString

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
    runWith(sink)

  val fileCsv = Paths.get("example.csv")
  println("Reading file: " + fileCsv.toAbsolutePath)

  val objectListFlow = Flow[List[String]].map(l => LineElement(l.head, l(1), l(2)))

  //Example of csv read using alpakka
  FileIO.fromPath(fileCsv)
    .via(CsvParsing.lineScanner())
    .map(_.map(_.utf8String))
    .via(objectListFlow)
    .runWith(sink)

  actorSystem.terminate
}
