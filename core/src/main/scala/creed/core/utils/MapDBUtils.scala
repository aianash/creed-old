package creed
package core
package utils

import org.mapdb._


object MapDBUtils {

  val ARRAY5 = new BTreeKeySerializer.ArrayKeySerializer(
    Array(Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR),
    Array(Serializer.BASIC, Serializer.BASIC, Serializer.BASIC, Serializer.BASIC, Serializer.BASIC))

  val ARRAY6 = new BTreeKeySerializer.ArrayKeySerializer(
    Array(Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR, Fun.COMPARATOR),
    Array(Serializer.BASIC, Serializer.BASIC, Serializer.BASIC, Serializer.BASIC, Serializer.BASIC, Serializer.BASIC))

}