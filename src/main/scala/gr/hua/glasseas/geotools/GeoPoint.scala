package gr.hua.glasseas.geotools

case class GeoPoint(longitude: Double, latitude: Double) {

  override def toString: String = s"$longitude,$latitude"

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[GeoPoint]) {
      val gp = obj.asInstanceOf[GeoPoint]
      if (longitude == gp.longitude && latitude == gp.latitude) true
      else false
    }
    else false
  }

  override def hashCode(): Int = {
    val tmp = (latitude + ((longitude + 1) / 2)).toInt
    Math.abs((longitude + (tmp * tmp)).toInt)
  }

}
