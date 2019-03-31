package kafka_producer_consumer

import java.io.File
import java.util.Properties
import scala.io.Source
import kafka.serializer.StringDecoder
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.InputDStream
class KafkaUtils {

    val brokers = "9999:9092,9998:9092"

    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("client.id", "ScalaProducerExample")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    //This function reads from input topic, and returns InputDStream.
    def read(topicIn : String, ssc: StreamingContext) : InputDStream[(String, String)]= {

      System.out.println("--------------------------------------------"+topicIn)
      val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, Map[String,String]("metadata.broker.list" -> " hvi1x0257:9092,hvi1x0258:9092"), Set(topicIn))
      messages

    }

    //This function takes DStream in streaming and writes it in output Topic
    def stringWriter(stringData: DStream[String], topicOut: String) = {

      val stringDstream = new DStreamKafkaWriter(stringData)
      stringDstream.writeToKafka(props, s => new ProducerRecord[String, String](topicOut, s))
    }

    //This function is used to write in Batch to Kafka from File in localFileSystem
    def writeFromFile(filenamePath : String, topicToWrite: String) ={
      if (new File(filenamePath).exists()) {
        val t = System.currentTimeMillis()
        val producer = new KafkaProducer[String, String](props)
        for (line <- Source.fromFile(filenamePath).getLines) {
          println(line)
          val data = new ProducerRecord[String, String](topicToWrite, t.toString, line)
          producer.send(data)
        }
        producer.close()
      }
      else {
        System.out.println("--No such file directory!!!!")
      }
    }

    //This function writes in Batch to Kafka from input String
    def writeFromString(msg : String, topicToWrite: String) ={
      val t = System.currentTimeMillis()
      val producer = new KafkaProducer[String, String](props)
      val data = new ProducerRecord[String, String](topicToWrite, t.toString, msg)
      producer.send(data)
      producer.close()
    }


  }
}
