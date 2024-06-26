package gr.hua.glasseas.geotools

import java.io.FileWriter
import java.util.UUID

import gr.hua.glasseas.ml.clustering.DBScan
import gr.hua.glasseas.{BalancedPartitioner, GlasseasContext, LocalDatabase}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

case class ClusterStatistics(startLon: Double, startLat: Double, endLon: Double, endLat: Double, avgCog: Double, avgSpeed: Double, speedDeviation: Double, cogDeviation: Double) {
  override def toString: String = s"$startLon,$startLat,$endLon,$endLat,$avgCog,$avgSpeed,$speedDeviation,$cogDeviation"
}

class Preprocessor extends Serializable {

  def getVoyageClusters(itineraries: RDD[(String,ArrayBuffer[AISPosition])], numPartitions: Int, saveToFile: Boolean): Unit/*RDD[(String,ArrayBuffer[(ClusterStatistics,Polygon)])]*/ = {

    val st = new SpatialToolkit
    val voyageConvexHulls = itineraries.keyBy(_._1).partitionBy(new BalancedPartitioner(numPartitions,itineraries.map(_._1))).glom().flatMap {
      arr =>
        arr.map(_._2).toMap.map{
          x =>
            val filtered = x._2.filter{
              p =>
                LocalDatabase.getEnclosingWaypoint(GeoPoint(p.longitude,p.latitude)) match {
                  case Some(wp) => false
                  case None => true
                }
            }
            (x._1,filtered)
        }.filterNot(x => x._2.isEmpty).map {
          case (itinerary,positions) =>
            val dbscan = new DBScan(positions, 0.03, 5)
            val clusters = dbscan.getTrajectoryClusters
              .filter(cl => cl.map(_.annotation).distinct.size > 1) // remove clusters that contain one voyage
            val convexHulls = clusters.filter(_.size > 10).map{ // keep clusters that contain more than ten positions
              cluster =>
                (getClusterStats(cluster),st.getConvexHull(cluster.map(p => GeoPoint(p.longitude,p.latitude)).toList))
            }


            (itinerary,convexHulls)
        }
    }
    if (saveToFile) {
      println("Saving results to file...")
      val w = new FileWriter("itineraries_clusters.csv")
      w.write("ITINERARY,POLYGON,START_LON,START_LAT,END_LON,END_LAT,AVG_COG,AVG_SPEED,SPEED_DEVIATION,COG_DEVIATION\n")
      voyageConvexHulls.collect.foreach {
        case (itinerary, polygons) =>
          polygons.foreach(p => w.write(s"$itinerary,${p._2.toString},${p._1.toString}\n"))
      }
      w.close()
      println("Save complete.")

//      println("Saving results to file...")
//      val w = new FileWriter("voyage_clusters_new.csv")
//      w.write("ITINERARY,CLUSTER_ID,MMSI,IMO,LATITUDE,LONGITUDE,COG,HEADING,SOG,TIMESTAMP,NAME,SHIP_TYPE,DESTINATION,ANNOTATION\n")
//      voyageConvexHulls.collect.foreach {
//        case (voyageId, clusters) =>
//          val test = clusters.zipWithIndex
//
//          test.foreach{
//            case (positions,clusterId) =>
//              positions.foreach(p => w.write(s"$voyageId,$clusterId,${p.toString}\n"))
//          }
//      }
//      w.close()
//      println("Save complete.")
    }
    //voyageHulls
  }

  /*def getVoyageClustersFromFile(filename: String, sc: SparkContext, numPartitions: Int, saveToFile: Boolean): Unit/*RDD[(String,ArrayBuffer[(ClusterStatistics,Polygon)])]*/ = {
    val gc = new GlasseasContext
    val data = gc.readVoyageData(filename,sc)
    getVoyageClusters(data,numPartitions,saveToFile)
  }*/

  private def getClusterStats(cluster: ArrayBuffer[AISPosition]): ClusterStatistics = {
        val numElements = cluster.size
        var startLon = 0.0
        var startLat = 0.0
        var endLon = 0.0
        var endLat = 0.0
        val avgSpeed = cluster.map(_.speed).sum / numElements
        val avgCog = cluster.map(_.cog).sum / numElements
        val speedStandardDeviation = Math.sqrt(cluster.map(x => Math.pow(x.speed - avgSpeed, 2)).sum / numElements)
        val cogStandardDeviation = Math.sqrt(cluster.map(x => Math.pow(x.cog - avgCog, 2)).sum / numElements)
        if (avgCog >= 0 && avgCog <= 90) {
          startLon = cluster.map(_.longitude).min
          endLon = cluster.map(_.longitude).max
          startLat = cluster.map(_.latitude).min
          endLat = cluster.map(_.latitude).max
        }
        else if (avgCog > 90 && avgCog <= 180) {
          startLon = cluster.map(_.longitude).min
          startLat = cluster.map(_.latitude).max
          endLon = cluster.map(_.longitude).max
          endLat = cluster.map(_.latitude).min
        }
        else if (avgCog > 180 && avgCog <= 270) {
          startLon = cluster.map(_.longitude).max
          endLon = cluster.map(_.longitude).min
          startLat = cluster.map(_.latitude).max
          endLat = cluster.map(_.latitude).min
        }
        else {
          startLon = cluster.map(_.longitude).max
          endLon = cluster.map(_.longitude).min
          startLat = cluster.map(_.latitude).min
          endLat = cluster.map(_.latitude).max
        }
    ClusterStatistics(startLon,startLat,endLon,endLat,avgCog,avgSpeed,speedStandardDeviation,cogStandardDeviation)
  }

}
