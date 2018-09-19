package com.example

import java.lang.System.currentTimeMillis
import scala.io.Source
import java.text.SimpleDateFormat
import scala.math.min
//import java.text.DateFormat

object TimeSettings {
  def getEarliestTime(file1: String, file2: String): Long = {
    
    //Get the earliest timestamp from the first input file
    val timeFromFirstFile = getFirstTimestampStr(file1)
    val firstTimeInSec = getTimestampInSec(timeFromFirstFile, "dd.MM.yyyy hh:mm")

    println(firstTimeInSec)
    
    //Get the earliest timestamp from the second input file
    val timeFromSecondFile = getFirstTimestampStr(file2)
    val secondTimeInSec = getTimestampInSec(timeFromSecondFile, "yyyy-MM-dd hh:mm:ss")
    
    
    println(secondTimeInSec)
    

    return min(firstTimeInSec, secondTimeInSec)
  }

  private def getFirstTimestampStr(filename: String): String = {
    val src = Source.fromFile(filename)
    val line = src.getLines.drop(1).take(1).toList.head //Take the first token of the second line (the first line holds column names)
    src.close
    val timestampStr = line.split(",").head
    return timestampStr
  }
  
  private def getTimestampInSec(timestamp: String, format: String): Long = {
    try {
       val formatter = new SimpleDateFormat(format);
       return formatter.parse(timestamp).getTime / 1000;
    } catch {
      case e: Exception => 
        println("ERROR parsing timestamp " + timestamp + " in format " + format);
        return System.currentTimeMillis / 1000
    }
  }
}

class TimeSettings(dataEarliestTime: Long) {
  val startTimeInSec: Long = System.currentTimeMillis / 1000 //Current time in seconds
  var dataStartTimeInSec: Long = dataEarliestTime //Timestamp from the first record

}

