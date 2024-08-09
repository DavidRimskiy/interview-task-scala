package movieratings

import java.io.File
import java.nio.charset.{Charset, CodingErrorAction}
import java.nio.file.{Files, Paths}
import java.util.stream.Collectors
import scala.io.Codec
import scala.jdk.CollectionConverters._
import scala.util.Try

object ReportGenerator {

  // Case class representing a Movie with its ID, year of release, and title
  case class Movie(id: Int, yearOfRelease: Int, title: String)

  /**
   * Load movie titles from a CSV file and return a Map of Movie ID to Movie objects.
   * Each line in the file is expected to have the format: movieID,yearOfRelease,title
   */
  def loadMovieTitles(filename: String): Map[Int, Movie] = {

    // Set up a codec with UTF-8 encoding and replace malformed input
    implicit val codec: Codec = Codec(Charset.forName("UTF-8"))
    codec.onMalformedInput(CodingErrorAction.REPLACE)

    // Read the file using Java's Files API
    Files.lines(Paths.get(filename)).collect(Collectors.toList()).asScala.flatMap { line =>  // Can be also used Source.fromFile(filename).getLines()
      val fields = line.split(",", 3)                                                        // as Scala approach, but it is not safe
      if (fields.length == 3) {
        Try {
          val movieID = fields(0).toInt
          val yearOfRelease = fields(1).toInt
          val title = fields(2)
          movieID -> Movie(movieID, yearOfRelease, title)
        }.toOption
      } else {
        println(s"Invalid line format: $line")
        None
      }
    }.toMap
  }

  /**
   * Process a movie rating file and return an Option containing a tuple of:
   * (movieID, average rating, number of reviews).
   * The file is expected to have the format:
   * movieID,rating,timestamp
   */
  def processMovieFile(filename: String): Option[(Int, Double, Int)] = {
    val lines = Files.lines(Paths.get(filename)).collect(Collectors.toList()).asScala
    val movieID = lines.head.dropRight(1).toInt // Remove trailing comma from the movieID
    val ratings = lines.tail.map { line =>
      val Array(_, rating, _) = line.split(",")
      rating.toInt
    }
    val numReviews = ratings.size
    val avgRating = ratings.sum.toDouble / numReviews
    Some((movieID, avgRating, numReviews))
  }


  /**
   * Generate a report based on movie files and titles.
   * The report is filtered by movies released between 1970 and 1990 with more than 1000 reviews.
   */
  def generateReport(movieFilesDir: String, movieTitlesFile: String): Iterable[List[Any]] = {
    val movieTitles = loadMovieTitles(movieTitlesFile)
    val report = new scala.collection.mutable.ListBuffer[List[Any]]

    // Get all .txt files from the directory
    val movieFiles = new File(movieFilesDir).listFiles.filter(_.isFile).filter(_.getName.endsWith(".txt"))

    // Process each movie file and generate a report entry if it meets the criteria
    movieFiles.flatMap { file =>
        processMovieFile(file.getAbsolutePath).flatMap { case (movieID, avgRating, numReviews) =>
          movieTitles.get(movieID).filter { movie =>
            movie.yearOfRelease >= 1970 && movie.yearOfRelease <= 1990 && numReviews > 1000
          }.map { movie =>
            List(movie.title, movie.yearOfRelease, avgRating, numReviews)
          }
        }
      }.toList
      .sortBy(movie => (-movie(2).asInstanceOf[Double], movie(0).asInstanceOf[String])) // Sort by rating descending, then by title
      .foreach(report += _)

    report
  }

  /**
   * Entry point for the application. Expects 3 arguments:
   * movieFilesDir, movieTitlesFile, and output filename.
   */
  def main(args: Array[String]): Unit = {

    if (args.length != 3) {
      println("Usage: ReportGenerator <movieFilesDir> <movieTitlesFile> <FileName>")
      sys.exit(1)
    }
    val movieFilesDir = args(0)
    val movieTitlesFile = args(1)

    // Generate the report and write it to the specified output file
    val report = generateReport(movieFilesDir, movieTitlesFile)
    CsvUtils.writeToFile(report, new File(args(2)))
  }
}
