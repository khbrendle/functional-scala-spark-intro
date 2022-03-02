package fss.app

import org.apache.spark.sql.SparkSession

object Utils {
  def withSpark(f: SparkSession => Any): Any = {
    val spark: SparkSession = SparkSession
      .builder()
      .master("local[2]") // provide at least 2 cores
      .appName("unit-testing")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    spark.conf.set("spark.sql.shuffle.partitions", 5)

    f(spark)
  }
}
