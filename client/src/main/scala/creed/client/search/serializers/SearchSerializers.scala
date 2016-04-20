package creed
package client
package search
package serializers

import scala.util.Try

import java.io.{ ObjectOutputStream, ByteArrayOutputStream, ObjectInputStream, ByteArrayInputStream }

import akka.actor._
import akka.serialization._
import akka.util.ClassLoaderObjectInputStream

import commons.core.util.UnsafeUtil
import commons.catalogue.collection._


class SearchSerializer extends Serializer {
  import UnsafeUtil._

  def includeManifest = true

  def identifier = 7891111

  /**
    *
    * @param Parameter1 - blah blah
    * @return Return value - blah blah
    */
  def toBinary(obj: AnyRef): Array[Byte] = obj match {
    case SearchResult(searchId, items, itemScores) =>
      (for {
        one   <- normalToBinary(searchId)
        two   <- Try(items.toBinary)
        three <- normalToBinary(itemScores)
      } yield {
        val pos = Array.ofDim[Byte](INT_SIZE_BYTES * 2)
        UNSAFE.putInt(pos, BYTE_ARRAY_BASE_OFFSET, one.size)
        UNSAFE.putInt(pos, BYTE_ARRAY_BASE_OFFSET + INT_SIZE_BYTES, two.size)
        pos ++ one ++ two ++ three
      }) get
    case _ => throw new IllegalArgumentException("Provided parameter cannot be serialized by SearchSerializer")
  }

  private def normalToBinary(obj: AnyRef) = Try {
    val bos = new ByteArrayOutputStream
    val out = new ObjectOutputStream(bos)
    out.writeObject(obj)
    out.close()
    bos.toByteArray
  }

  private def normalFromBinary(bytes: Array[Byte]): AnyRef = {
    val in = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val obj = in.readObject
    in.close()
    obj
  }

  /** Description of function
    *
    * @param Parameter1 - blah blah
    * @return Return value - blah blah
    */
  def fromBinary(binary: Array[Byte], clazz: Option[Class[_]]): AnyRef =
    if(clazz.get isAssignableFrom classOf[SearchResult]) {
      var pos = BYTE_ARRAY_BASE_OFFSET
      val oneSize = UNSAFE.getInt(binary, pos)
      pos += INT_SIZE_BYTES
      val twoSize = UNSAFE.getInt(binary, pos)
      pos += INT_SIZE_BYTES

      val idBinary = Array.ofDim[Byte](oneSize)
      UNSAFE.copyMemory(binary, pos, idBinary, BYTE_ARRAY_BASE_OFFSET, oneSize)
      val searchId = normalFromBinary(idBinary).asInstanceOf[SearchId]
      pos += oneSize

      val itemsBinary = Array.ofDim[Byte](twoSize)
      UNSAFE.copyMemory(binary, BYTE_ARRAY_BASE_OFFSET + 2 * INT_SIZE_BYTES + oneSize, itemsBinary, BYTE_ARRAY_BASE_OFFSET, twoSize)
      try {
        val items = CatalogueItems(itemsBinary)
        pos += twoSize
        val scoresBinary = Array.ofDim[Byte](binary.size - twoSize - oneSize - (2 * INT_SIZE_BYTES))
        UNSAFE.copyMemory(binary, pos, scoresBinary, BYTE_ARRAY_BASE_OFFSET, scoresBinary.size)
        val scores = normalFromBinary(scoresBinary).asInstanceOf[Seq[ItemScore]]
        SearchResult(searchId, items, scores)
      } catch {
        case ex: Exception => ex.printStackTrace; throw ex
      }
    } else throw new IllegalArgumentException("Provided parameter bytes array doesnot belong to any of SearchSerializer related objects=")


}