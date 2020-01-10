package com.tundra.cr

object DbQuirks {
  def fromPipelineString(s: String): Option[String] = s match {
    case "empty" => None
    case ss      => Some(ss)
  }

  def fromPipelineBoolean(n: Long): Boolean =
    n == 1
}
