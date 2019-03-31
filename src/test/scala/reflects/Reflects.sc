import scala.reflect._

object DbConnections extends Enumeration {
  type ConnectionType = Value
  object MariaDb {type MariaDbConnection = String}
  object MySql {type MySqlConnection = String}
  object Oracle {type OracleConnection = String}
  object PostgreSql {type PostgreSqlConnection = String}
  val MariaDbConnections,
  MySqlConnection,
  OracleConnection,
  PostgreSqlConnection = Value
}

trait DbReader{
  def getField(fieldName: String): String
  def getConnection: DbConnections.ConnectionType
}

class MariaDbReader(rowId: String, mariaDbConnection: DbConnections.ConnectionType) extends DbReader {
  override def getField(fieldName: String): String =  s"MariaDB imaginary value: $fieldName"
  override def getConnection:DbConnections.ConnectionType = mariaDbConnection
}

class MySqlDbReader(rowId: String, mySqlConnection: DbConnections.ConnectionType) extends DbReader {
  override def getField(fieldName: String): String =  s"$mySqlConnection imaginary value: $fieldName"
  override def getConnection:DbConnections.ConnectionType = mySqlConnection
}

class OracleDbReader(rowId: String, oracleConnection: DbConnections.ConnectionType) extends DbReader {
  override def getField(fieldName: String): String =  s"Oracle imaginary value: $fieldName"
  override def getConnection:DbConnections.ConnectionType = oracleConnection
}

class PostgreSqlDbReader(rowId: String, postgreSqlConnection: DbConnections.ConnectionType) extends DbReader {
  override def getField(fieldName: String): String =  s"Impala imaginary value: $fieldName"
  override def getConnection:DbConnections.ConnectionType = postgreSqlConnection
}

class ColourJob(val jobName: String, val dbReader: DbReader) {
  val colourField = dbReader getField "ColourField"
  val colourField_1 = colourField + "_1"
  val colourField_2 = colourField + "_2"
}
class BlueJob(jobName: String,  dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val blueField = dbReader getField "BlueField"
  val blueField_1 = blueField + "_1"
}
class AzureJob(jobName: String,  dbReader: DbReader) extends BlueJob(jobName, dbReader) {
  val azureField = dbReader getField "AzureBlueField"
  val azureField_1 = azureField + "_1"
  val azureField_2 = azureField + "_2"
}
class CyanJob(jobName: String, dbReader: DbReader) extends BlueJob(jobName, dbReader) {
  val cyanField = dbReader getField "CyanBlueField"
  val cyanField_1 = cyanField + "_1"
  val cyanField_2 = cyanField + "_2"
}
class GreenJob(jobName: String,  dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val greenField = dbReader getField "GreenField"
  val greenField_1 = greenField + "_1"
  val greenField_2 = greenField + "_2"
}

class RedJob(jobName: String, dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val redField = dbReader getField "RedField"
  val redField_1 = redField + "_1"
  val redField_2 = redField + "_2"
}

class JobFactory(var jobName: String) {
  val ColourJob = classTag[ColourJob]
  val BlueJobType = classTag[BlueJob]
  val AzureType = classTag[AzureJob]
  val CyanJobType = classTag[CyanJob]
  val GreenJobType = classTag[GreenJob]
  val RedJobType = classTag[RedJob]

  def detectAndCreateJob(jobName: String, dbReader: DbReader, classType: Object) = {
    classType match {
      case BlueJobType => new BlueJob(jobName, dbReader)
      case AzureType => new AzureJob(jobName, dbReader)
      case CyanJobType => new CyanJob(jobName, dbReader)
      case GreenJobType => new GreenJob(jobName, dbReader)
      case RedJobType => new RedJob(jobName, dbReader)
      case _ => new ColourJob(jobName, dbReader)
    }
  }

  def setJobName(newJobName: String) = { jobName = newJobName; this}

  def readFromOracle[T](rowId: String)(implicit tag: ClassTag[T]): T = {
    detectAndCreateJob("", new OracleDbReader(rowId, DbConnections.OracleConnection), tag).asInstanceOf[T]
  }

  def readFromPostgreSql[T](rowId: String)(implicit tag: ClassTag[T]): T = {
    detectAndCreateJob("", new PostgreSqlDbReader(rowId, DbConnections.PostgreSqlConnection), tag).asInstanceOf[T]
  }

  def readFromMariaDB[T](rowId: String)(implicit tag: ClassTag[T]): T = {
    detectAndCreateJob("", new MariaDbReader(rowId, DbConnections.MariaDbConnections), tag).asInstanceOf[T]
  }

  def readFromMySql[T](rowId: String)(implicit tag: ClassTag[T]): T = {
    detectAndCreateJob("", new MySqlDbReader(rowId, DbConnections.MySqlConnection), tag).asInstanceOf[T]
  }
}

object JobFactory {
  def apply(jobName: String = "default job name"): JobFactory = new JobFactory(jobName)
}


//val cyanJob = new CyanJob("My-second-cyan-job", new PostgreSqlDbReader("thisIsTheRowId012930",new DbConnections.PostgreSqlConnection(...)))

val cyanJob = JobFactory().readFromOracle[CyanJob]("RowId1234")
cyanJob.cyanField




