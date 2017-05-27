package org.bruchez.olivier.kindleclippings

import java.io._
import scala.collection.mutable
import scala.io.Source
import scala.util._

case class Book(title: String) {
  def markdown: String = title
}

case class Clipping(contents: String, pageOption: Option[Int], locationOption: Option[String]) {
  def markdown: String =
    contents +
      (pageOption.map(page => s"p. $page") orElse locationOption.map(location => s"loc. $location")).
        map(pageOrLocation => s" ($pageOrLocation)").
        getOrElse("")
}

case class KindleClippings(clippingsByBook: Map[Book, Seq[Clipping]]) {
  def createMarkdownFiles() {
    for {
      (book, clippings) <- clippingsByBook
      markdown = KindleClippings.markdown(book, clippings)
    } {
      val out = new PrintWriter(new File(s"${book.title}.md"), "UTF-8")
      try {
        out.print(markdown)
      } finally {
        out.close()
      }
    }
  }
}

object KindleClippings {
  def main(args: Array[String]) {
    lines(args.headOption.getOrElse("My Clippings.txt")) match {
      case Success(lines) => KindleClippings(lines).createMarkdownFiles()
      case Failure(throwable) => println(s"Error: ${throwable.getMessage}")
    }
  }

  def apply(lines: List[String]): KindleClippings = {
    val clippingsByBook = mutable.HashMap[Book, Vector[Clipping]]()

    for {
      title :: pageOrlocation :: empty :: clippingContents :: separator :: Nil <- lines.grouped(5)
      trimmedTitle = title.trim.replaceAll("\uFEFF", "")
      trimmedClippingContents = clippingContents.trim
      if trimmedClippingContents.nonEmpty
      book = Book(trimmedTitle)
      clippingsForBook = clippingsByBook.getOrElse(book, Vector[Clipping]())
      pageOption = pageOrlocation match {
        case Page(page) => Try(page.toInt).toOption
        case _ => None
      }
      locationOption = pageOrlocation match {
        case Location(location) => Some(location)
        case _ => None
      }
      clipping = Clipping(trimmedClippingContents, pageOption, locationOption)
    } {
      clippingsByBook.update(book, clippingsForBook :+ clipping)
    }

    KindleClippings(Map(clippingsByBook.toSeq.map(kv => kv._1 -> kv._2.toSeq): _*))
  }

  private val Page = """.*[Pp]age (\d+) .*""".r

  private val Location = """.*Loc(?:\.|ation) ([^ ]+) .*""".r

  private def lines(filename: String): Try[List[String]] =
    Try(Source.fromFile(filename)(scala.io.Codec.UTF8).getLines().toList)

  private def markdown(book: Book, clippings: Seq[Clipping]): String =
    (s"# ${book.markdown}" +: clippings.map(clipping => s"* ${clipping.markdown}")).mkString("\n")
}
