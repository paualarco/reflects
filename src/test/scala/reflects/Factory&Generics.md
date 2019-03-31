## Dynamic factory pattern for avoiding type erasure in Scala

This document explains a fashion way to perform a Factory pattern for dynamic 
object creation by using generics and scala reflects to 
avoid type erasure.

### Type erasure, type parameters and scala reflects
Firstly, what does Type Erasure mean?

Type erasure refers to a procedure performed by compilers at runtime process
to explicitly remove type parameters. 

Even the fact that with Scala you can pattern match
on a wide range of arbitrary types of values, you can not do that with type parameters.
Since it is a JVM-based language, and as Java does not know about generics, they are implemented
with type erasure so that the bytecode generated would be interoperable.

So in resume, it means that at runtime, the compiler is not able to differentiate between types, 
p.e: ```Array[Int]``` and ```Array[String]```.

And this could be a problem if you want to get a function that could dynamicaly return the 
generic type passed. There is an example below that will help to understand the problem.
In the example there is a class called Blue, with 4 child classes that represents the different types of blue. 
Also there is a function called ```returnGenericType```, that is aimed to return an instance of the blue sub type passed as generic type.
  ```scala
class Blue
class Cyan extends Blue
class Azure extends Blue
class Navy extends Blue
class Sky extends Blue

 def returnGernicType[B<:Blue]: T = {
    t match {
      case _: Cyan => new Cyan().asInstanceOf[T]
      case _: Azure => new Azure().asInstanceOf[T]
      case _: Navy => new Navy().asInstanceOf[T]
      case _: Sky => new Sky().asInstanceOf[T]
      case _ => new Blue().asInstanceOf[T]
    }
  //This does not compile
  ```
This is a dummy example just to show one capability that would be idealistic but that in fact is not allowed and can never work in Scala. Since you can not pattern 
match for the generic type ```[T]```, because pattern matching is resolved at runtime which means that the parameter type will be erased and therefore, lost.

So, in order to avoid the ```Type Erasure```, Scala provides ```Manifests```, divided into (```TypeTags```, ```ClassTags``` and ```WaekTypeTags```).

In which the problem has been solved using ClassTags as you can see in the following example. To do so, the generic type
 is captured using an implicit parameter of type: ```ClassType[T]```. Which it is used later to pattern match with the colour type stored before with the reflects function: ```classTag```.
  ```scala

import scala.reflect.ClassTag
import scala.reflect.classTag
class Blue
class Cyan extends Blue
class Azure extends Blue
class Navy extends Blue
class Sky extends Blue

object AvoidingTypeErasureExample {
  val BlueType = classTag[Blue]
  val CyanType = classTag[Cyan]
  val AzureType = classTag[Azure]
  val NavyType = classTag[Navy]
  val SkyType = classTag[Sky]

  def returnGernicType[T](implicit t: ClassTag[T]): T = {
    t match {
      case CyanType => new Cyan().asInstanceOf[T]
      case AzureType => new Azure().asInstanceOf[T]
      case NavyType => new Navy().asInstanceOf[T]
      case SkyType => new Sky().asInstanceOf[T]
      case _ => new Blue().asInstanceOf[T]
    }
  }
}
  ```
So now, type passed as type parameter can be used 
to perform pattern matching, therefore its instance could be 
returned as a result of the function.

The previous example was aimed to show how to avoid the Type Erasure problem.
Now, let´s mix this solution with an ObjectFactory pattern style and see what are the results and 
advantages of doing so.

### Factory pattern example
Let's expose the problem, we have a class object type called ColourJob, which it has a different number of classes inheriting from it 
(```BlueJob```, (```AzureJob```, ```CyanJob```, ```...```), ```GreenJob```, ```RedJob```, ```...```). The primary constructor of all classes that 
inherits from ```ColourJob``` are composed by ```(jobName: String, dbReader: DbReader)```. 
As it can be seen their values depends on their class, so they are not just overwriting the father´s variables.
 
  ```scala

class ColourJob(val jobName: String, val dbReader: DbReader) {
  val COLOUR_FIELD = dbReader getField "ColourField"
  val COLOUR_FIELD_1 = COLOUR_FIELD + "_1"
  val COLOUR_FIELD_2 = COLOUR_FIELD + "_2"
}
class BlueJob(jobName: String,  dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val BLUE_FIELD = dbReader getField "BlueField"
  val BLUE_FIELD_1 = BLUE_FIELD + "_1"
}
class AzureJob(jobName: String,  dbReader: DbReader) extends BlueJob(jobName, dbReader) {
  val AZURE_FIELD = dbReader getField "AzureBlueField"
  val AZURE_FIELD_1 = AZURE_FIELD + "_1"
  val AZURE_FIELD_2 = AZURE_FIELD + "_2"
}
class CyanJob(jobName: String, dbReader: DbReader) extends BlueJob(jobName, dbReader) {
  val CYAN_FIELD = dbReader getField "CyanBlueField"
  val CYAN_FIELD_1 = CYAN_FIELD + "_1"
  val CYAN_FIELD_2 = CYAN_FIELD + "_2"
}
class GreenJob(jobName: String,  dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val GREEN_FIELD = dbReader getField "GreenField"
  val GREEN_FIELD_1 = GREEN_FIELD + "_1"
  val GREEN_FIELD_2 = GREEN_FIELD + "_2"
}

class RedJob(jobName: String, dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val RED_FIELD = dbReader getField "RedField"
  val RED_FIELD_1 = RED_FIELD + "_1"
  val RED_FIELD_2 = RED_FIELD + "_2"
}
  ```
 Until that point if we want to create an instance of a Job, we could do that directly by using the following statement:
  ```scala
  val cyan = CyanJob("My-first-cyan-job", new DbReader)
   ```
 That is ok, but that case is not interesting as it is not contemplating the scenario that we where to arrive.
 So as it is logic, let´s make the DbReader is a trait that represents a fake Database Reader, which it will have
  different subtypes of it, in which in that case has been defined four of them as: 
    (```MariaDbReader```, ```MySqlReader```, ```OracleReader``` and ```PostgresSqlReader```)
    
  ```scala
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
  ```
  As you probably have already appreciated, each DbReader has a constructor parameter that represents the database connection.
  That parameter is just used to simulate a more realistic scenario, so they are not implemented.
  
  Let´s see what is happening now if we try to create a Job:
```scala
val cyanJob = new CyanJob("My-second-cyan-job", new PostgreSqlDbReader("thisIsTheRowId012930",new DbConnections.PostgreSqlConnection(...)))
```
As it could be seen, the creation statement is too long and tought to read, so it is a good practice to have the creation
of this object encapsulated in another class.     
  So given that scenario, the creation of an ObjectFactory fits so well, which here is its definition from Scala Design Patterns book:
  
  ´This design pattern deals with the creation of objects without explicitly specifying the actual class that
   the instance will have—it could be something that is decided at runtime based on many factors. 
   Some of these factors can include operating systems, different data types, or input parameters.
   It gives developers the peace of mind of just calling a method rather than invoking a concrete constructor.´
  
  Therefore, this JobFactory class will implement a different ´read´ method for each different dbReader given, in that case 
  (```readFromOracle```, ```readFromPostgreSql```, ```readFromMariaDB```, ```readFromMySql```).
```scala
def readFromOracle[T<:ColourJob](rowId: String)(implicit tag: ClassTag[T]): T = {
    detectAndCreateJob("", new OracleDbReader(rowId, DbConnections.OracleConnection), tag).asInstanceOf[T]
  }
```
Here is where we can use the scala reflects functions, 
  to capture the ```GenericType``` and return it an instance of itself with ```asInstanceOf[T]```. 
  (Where the generic type corresponds to a ```ColourJob``` or subtype),
  and as you can imagine, the ```´detectAndCreate´``` method implements a pattern matching by the tag given as parameter, which
  returns the corresponding ```JobType``` that matched.

```scala  
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
```

And at the end what we have is a JobFactory that allows the programmer to 
instantiate this classes through a clean and fashion way:
```scala  
val cyanJob = JobFactory().readFromOracle[CyanJob]("1234")
```

Moreover, the dynamic return type allows the developer to know what are functions
of the created job without having to use ´asInstanceOf[Job]´ to cast to its corresponding type and then use its definitions.

To finish, the whole example´s code has been pasted below. 
I hope you can find it useful!

```scala

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
  val COLOUR_FIELD = dbReader getField "ColourField"
  val COLOUR_FIELD_1 = COLOUR_FIELD + "_1"
  val COLOUR_FIELD_2 = COLOUR_FIELD + "_2"
}
class BlueJob(jobName: String,  dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val BLUE_FIELD = dbReader getField "BlueField"
  val BLUE_FIELD_1 = BLUE_FIELD + "_1"
}
class AzureJob(jobName: String,  dbReader: DbReader) extends BlueJob(jobName, dbReader) {
  val AZURE_FIELD = dbReader getField "AzureBlueField"
  val AZURE_FIELD_1 = AZURE_FIELD + "_1"
  val AZURE_FIELD_2 = AZURE_FIELD + "_2"
}
class CyanJob(jobName: String, dbReader: DbReader) extends BlueJob(jobName, dbReader) {
  val CYAN_FIELD = dbReader getField "CyanBlueField"
  val CYAN_FIELD_1 = CYAN_FIELD + "_1"
  val CYAN_FIELD_2 = CYAN_FIELD + "_2"
}
class GreenJob(jobName: String,  dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val GREEN_FIELD = dbReader getField "GreenField"
  val GREEN_FIELD_1 = GREEN_FIELD + "_1"
  val GREEN_FIELD_2 = GREEN_FIELD + "_2"
}

class RedJob(jobName: String, dbReader: DbReader) extends ColourJob(jobName, dbReader) {
  val RED_FIELD = dbReader getField "RedField"
  val RED_FIELD_1 = RED_FIELD + "_1"
  val RED_FIELD_2 = RED_FIELD + "_2"
}

import scala.reflect._
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
cyanJob.CYAN_FIELD

```