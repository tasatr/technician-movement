package com.technicianmovement

import scala.util.parsing.json.{JSONObject, JSONArray}

object Utils {
  def getErrorMessage(date: String, turbineID: String, person: String, error: String, errorState: String):String = {
     JSONObject.apply(
        Map("date" -> date,
            "turbine" -> turbineID,
            "person" -> person,
            "error" -> error,
            "error_state" -> errorState)).toString()
      
      
  }
}