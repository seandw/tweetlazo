package org.cognoseed.tweetlazo

import java.text.Normalizer

/** Helpers for various operations in this project. */
object Utils {

  implicit class StringUtils(str: String) {
    def stripDuplicateAdjacents = str.drop(1).foldLeft(new StringBuilder(str.take(1))){
      case (builder, newChar) if newChar != builder.last => builder.append(newChar)
      case (builder, _) => builder
    }.mkString

    def stripAccents = Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}", "")

    def stripPunctuation = str.replaceAll("(?U)\\p{Punct}", "")
  }

}
