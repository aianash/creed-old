package creed
package core
package utils

import org.mapdb._


object MapDBUtils {

  val ARRAY5 = new BTreeKeySerializer.ArrayKeySerializer(
    Array(Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR),
    Array(Serializer.JAVA, Serializer.JAVA, Serializer.JAVA, Serializer.JAVA, Serializer.JAVA))

  val ARRAY6 = new BTreeKeySerializer.ArrayKeySerializer(
    Array(Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR),
    Array(Serializer.JAVA, Serializer.JAVA, Serializer.JAVA, Serializer.JAVA, Serializer.JAVA, Serializer.JAVA))

}