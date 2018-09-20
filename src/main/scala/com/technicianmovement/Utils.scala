package com.technicianmovement

import scala.util.parsing.json.{JSONObject, JSONArray}
import java.text.SimpleDateFormat

object Utils {
  def getErrorMessage(date: Long, turbineID: String, person: String, error: String, errorState: String):String = {
    val df:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    val dateStr = df.format(date)
     JSONObject.apply(
        Map("date" -> dateStr,
            "turbine" -> turbineID,
            "person" -> person,
            "error" -> error,
            "error_state" -> errorState)).toString()
      
      
  }
}