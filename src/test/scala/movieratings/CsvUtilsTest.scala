package movieratings

import java.io.{File, FileWriter}
import org.apache.commons.csv.CSVRecord
import org.scalatest.funsuite.AnyFunSuite

class CsvUtilsTest extends AnyFunSuite {

  // Helper method to create temporary CSV file with given content
  private def createTempCsvFile(content: String): File = {
    val tempFile = File.createTempFile("testcsv", ".csv")
    val writer = new FileWriter(tempFile)
    writer.write(content)
    writer.close()
    tempFile
  }

  test("readFromFileAsList should correctly read CSV records from a file") {
    val content =
      """John,41,Plumber
        |Misato-san,29,Operations Director
        |""".stripMargin
    val file = createTempCsvFile(content)

    val records: List[CSVRecord] = CsvUtils.readFromFileAsList(file)

    assert(records.size == 2)
    assert(records.head.get(0) == "John")
    assert(records.head.get(1) == "41")
    assert(records.head.get(2) == "Plumber")

    assert(records(1).get(0) == "Misato-san")
    assert(records(1).get(1) == "29")
    assert(records(1).get(2) == "Operations Director")
  }

  test("writeToFile should correctly write records to a CSV file") {
    val records = List(
      List("John", 41, "Plumber"),
      List("Misato-san", 29, "Operations Director")
    )
    val tempFile = File.createTempFile("outputcsv", ".csv")

    CsvUtils.writeToFile(records, tempFile)

    val writtenRecords: List[CSVRecord] = CsvUtils.readFromFileAsList(tempFile)

    assert(writtenRecords.size == 2)
    assert(writtenRecords.head.get(0) == "John")
    assert(writtenRecords.head.get(1) == "41")
    assert(writtenRecords.head.get(2) == "Plumber")

    assert(writtenRecords(1).get(0) == "Misato-san")
    assert(writtenRecords(1).get(1) == "29")
    assert(writtenRecords(1).get(2) == "Operations Director")
  }

  test("writeToFile should handle empty records") {
    val records = List.empty[List[Any]]
    val tempFile = File.createTempFile("emptycsv", ".csv")

    CsvUtils.writeToFile(records, tempFile)

    val writtenRecords: List[CSVRecord] = CsvUtils.readFromFileAsList(tempFile)

    assert(writtenRecords.isEmpty)
  }
}
