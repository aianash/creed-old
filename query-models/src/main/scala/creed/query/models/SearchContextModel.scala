package creed
package query
package models

import java.io.File
import java.nio.file.{FileSystems, Path}

import commons.catalogue.attributes._

import org.apache.lucene.index._
import org.apache.lucene.search.{Query => _, _}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.store.{FSDirectory, MMapDirectory}
import org.apache.lucene.document.{Document, StringField, TextField, Field}

import it.unimi.dsi.fastutil.objects.{Object2FloatOpenHashMap, Object2IntOpenHashMap}

import client.search._
import datasets.ALTItemRelevanceDataset


class SearchContextModel {
  def searchContext(searchId: SearchId, query: Query, styles: Set[ClothingStyle]) = SearchContext(searchId, query)
}

object SearchContextModel {

  // private[models] val
}