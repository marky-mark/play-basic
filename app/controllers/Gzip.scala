package controllers

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream


object Gzip {

  def encode(data: Array[Byte]): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream(data.length)
    val gzip = new GZIPOutputStream(outputStream)
    gzip.write(data)
    gzip.close()
    val compressed = outputStream.toByteArray
    outputStream.close()
    compressed
  }

}
