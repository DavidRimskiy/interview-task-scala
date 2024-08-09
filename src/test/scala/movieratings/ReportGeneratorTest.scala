package movieratings

import java.nio.file.{Files, Paths}
import java.io.{BufferedWriter, FileWriter}
import org.scalatest.funsuite.AnyFunSuite

class ReportGeneratorTest extends AnyFunSuite {

  // Helper method to create temporary test files
  private def createTempFile(content: String): String = {
    val tempFile = Files.createTempFile("testfile", ".txt")
    val writer = new BufferedWriter(new FileWriter(tempFile.toFile))
    writer.write(content)
    writer.close()
    tempFile.toAbsolutePath.toString
  }

  test("loadMovieTitles should load a valid CSV file correctly") {
    val content = "1,1994,The Shawshank Redemption\n2,1972,The Godfather\n"
    val filename = createTempFile(content)

    val movies = ReportGenerator.loadMovieTitles(filename)

    assert(movies.size == 2)
    assert(movies(1) == ReportGenerator.Movie(1, 1994, "The Shawshank Redemption"))
    assert(movies(2) == ReportGenerator.Movie(2, 1972, "The Godfather"))
  }

  test("loadMovieTitles should handle an invalid line format gracefully") {
    val content = "1,1994,The Shawshank Redemption\nInvalidLine\n2,1972,The Godfather\n"
    val filename = createTempFile(content)

    val movies = ReportGenerator.loadMovieTitles(filename)

    assert(movies.size == 2)
    assert(movies.contains(1))
    assert(movies.contains(2))
  }

  test("processMovieFile should calculate correct average rating and number of reviews") {
    val content = "1,\n,5,\n,4,\n,3,\n"
    val filename = createTempFile(content)

    val result = ReportGenerator.processMovieFile(filename)

    assert(result.isDefined)
    assert(result.get._1 == 1) // Movie ID
    assert(result.get._2 == 4.0) // Average rating
    assert(result.get._3 == 3) // Number of reviews
  }

  test("generateReport should produce correct report entries") {
    val movieTitlesContent = "1,1994,The Shawshank Redemption\n2,1972,The Godfather\n"
    val movieFileContent = "1,\n,5,\n,4,\n,3,\n"
    val movieTitlesFilename = createTempFile(movieTitlesContent)
    val movieFilesDir = Files.createTempDirectory("moviefiles").toString

    val movieFilePath = Paths.get(movieFilesDir, "1.txt")
    Files.write(movieFilePath, movieFileContent.getBytes)

    val report = ReportGenerator.generateReport(movieFilesDir, movieTitlesFilename)

    assert(report.nonEmpty)
    assert(report.head.head == "The Shawshank Redemption")
    assert(report.head(1) == 1994)
    assert(report.head(2) == 4.0)
    assert(report.head(3) == 3)
  }

  test("main method should generate a CSV file") {
    val movieTitlesContent = "1,1994,The Shawshank Redemption\n2,1972,The Godfather\n"
    val movieFileContent = "1,\n,5,\n,4,\n,3,\n"
    val movieTitlesFilename = createTempFile(movieTitlesContent)
    val movieFilesDir = Files.createTempDirectory("moviefiles").toString

    val movieFilePath = Paths.get(movieFilesDir, "1.txt")
    Files.write(movieFilePath, movieFileContent.getBytes)

    val outputFileName = Files.createTempFile("output", ".csv").toAbsolutePath.toString

    ReportGenerator.main(Array(movieFilesDir, movieTitlesFilename, outputFileName))

    assert(Files.exists(Paths.get(outputFileName)))
  }
}
