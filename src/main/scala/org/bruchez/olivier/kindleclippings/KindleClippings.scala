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
      (pageOption.map(page => s"p. $page") orElse locationOption.map(location => s"loc. $location"))
        .map(pageOrLocation => s" ($pageOrLocation)")
        .getOrElse("")
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
      case Success(lines)     => KindleClippings(lines).createMarkdownFiles()
      case Failure(throwable) => println(s"Error: ${throwable.getMessage}")
    }
  }

  def apply(lines: List[String]): KindleClippings = {
    val MinLinesPerBook = 5

    @annotation.tailrec
    def clippingsByBook(remainingLines: List[String],
                        acc: List[(Book, Clipping)] = List()): Seq[(Book, Clipping)] =
      if (remainingLines.size < MinLinesPerBook) {
        acc.reverse
      } else {
        val title :: pageOrLocation :: empty :: clippingContentsAndRemainingLines = remainingLines

        val newRemainingLines =
          clippingContentsAndRemainingLines.dropWhile(line => !separator(line)).tail

        val clippingContents = clippingContentsAndRemainingLines
          .take(clippingContentsAndRemainingLines.size - newRemainingLines.size - 1)
          .map(_.trim)
          .dropWhile(_.isEmpty)
          .reverse
          .dropWhile(_.isEmpty)
          .reverse

        val newAcc =
          if (clippingContents.isEmpty) {
            acc
          } else {
            val clippingContentsAsString = clippingContents.mkString("\n")

            val trimmedTitle = title.trim.replaceAll("\uFEFF", "")
            val book = Book(trimmedTitle)
            val pageOption = pageOrLocation match {
              case Page(page) => Try(page.toInt).toOption
              case _          => None
            }
            val locationOption = pageOrLocation match {
              case Location(location) => Some(location)
              case _                  => None
            }
            val clipping = Clipping(clippingContentsAsString, pageOption, locationOption)

            (book -> clipping) :: acc
          }

        clippingsByBook(newRemainingLines, acc = newAcc)
      }

    KindleClippings(
      clippingsByBook(lines).groupBy(_._1).map(kv => kv._1 -> kv._2.map(_._2).distinct))
  }

  private def separator(string: String): Boolean = string.trim.toSet == Set('=')

  private val Page = """.*[Pp]age (\d+) .*""".r

  private val Location = """.*Loc(?:\.|ation) ([^ ]+) .*""".r

  private def lines(filename: String): Try[List[String]] =
    Try(Source.fromFile(filename)(scala.io.Codec.UTF8).getLines().toList)

  private def markdown(book: Book, clippings: Seq[Clipping]): String =
    (s"# ${book.markdown}" +: clippings.map(clipping => s"* ${clipping.markdown}")).mkString("\n")
}
