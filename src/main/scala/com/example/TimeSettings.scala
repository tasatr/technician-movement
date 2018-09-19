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
    val firstTime = getTimestamp(timeFromFirstFile, "dd.MM.yyyy hh:mm")

    println(firstTime)
    
    //Get the earliest timestamp from the second input file
    val timeFromSecondFile = getFirstTimestampStr(file2)
    val secondTime = getTimestamp(timeFromSecondFile, "yyyy-MM-dd hh:mm:ss")
    
    
    println(secondTime)
    

    return min(firstTime, secondTime)
  }

  private def getFirstTimestampStr(filename: String): String = {
    val src = Source.fromFile(filename)
    val line = src.getLines.drop(1).take(1).toList.head //Take the first token of the second line (the first line holds column names)
    src.close
    val timestampStr = line.split(",").head
    return timestampStr
  }
  
  //Returns timestamp in milliseconds
  def getTimestamp(timestamp: String, format: String): Long = {
    try {
       val formatter = new SimpleDateFormat(format);
       return formatter.parse(timestamp).getTime;
    } catch {
      case e: Exception => 
        println("ERROR parsing timestamp " + timestamp + " in format " + format);
        return System.currentTimeMillis
    }
  }
  
  //This program must run through 7 days of records in 14 mins
  def getConvertedTime(time: Long):Long = {
    //1 day in 2 mins : 24h = 1440m
    return time/720
  }
}



