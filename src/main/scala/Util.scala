/**
 * Common utils for working with Spark
 */

package noodlebot.util

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag


/**
 * Helpers dealing directly with RDDs in Spark
 */
object SparkUtil {

  // @deprecated This is no longer needed - Use CSV files via H2O DataFrame
  def loadTSV[T <: Product: TypeTag: ClassTag](sc: SparkContext, parser: Array[String] => T, files: String*): Seq[RDD[T]] = {
    files.map( f => {
      sc.textFile(f).mapPartitionsWithIndex((idx: Int, lines: Iterator[String]) => {
        if (idx == 0) lines.drop(1) else lines
      }).map(line => parser(line.split('\t')))
    })
  }

}


/**
 * Provide a set of String->T conversions
 */
object conversions {

  def cleanStr(s: String): String = s.replaceAll("""(?m)\s+$""", "").replaceAll("\u00A0", "")

  def str(s: String): Option[String] = {
    val clean = cleanStr(s)
    if (clean.length > 0) Some(clean) else None
  }

  def int(s: String): Option[Int] = {
    val clean = cleanStr(s.replace(",", ""))
    if (clean.length > 0) Some(clean.toInt) else None
  }

  def float(s: String): Option[Float] = {
    val clean = cleanStr(s.replace(",", ""))
    if (clean.length > 0) Some(clean.toFloat) else None
  }

  def minutes(s: String): Option[Float] = {
    val clean = cleanStr(s)
    if (clean.length > 0) {
      val parts = s.split(':')
      val mins = float(parts(0))
      val secs = float(parts(1))
      val goodMins = if (mins.isDefined) mins.get else 0
      val goodSecs = if (secs.isDefined) secs.get / 60 else 0
      Some(goodMins + goodSecs)
    } else None
  }

}
