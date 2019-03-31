package kafka_producer_consumer

import org.apache.spark.SparkConf

class Launcher {
  def main(args : Array[String]) {

    val sparkConf = new SparkConf().setAppName("TestingWithKafka")
    val ssc = new StreamingContext(sparkConf, Seconds(10))

    val topicToRead = "inputTopicName" //topic to read
    val topicToWrite = "outputTopicName" //topic to write
    val eventsKaka  = KafkaManager.read(topicToRead, ssc)

    eventsKaka.print()
    val dStream = eventsKaka.map( stream  => stream._2)
    KafkaManager.stringWriter(dStream, topicToWrite)

    ssc.start()

    ssc.awaitTermination()
  }
}
