/*
 * Copyright 2012 Typesafe, Inc. All rights reserved.
 */

package misc



object Resource {

  import scala.language.reflectiveCalls
  def withResource[A <: Closable, B](resource: A)(f: A => B): B =
    try f(resource) finally resource.close()

  type Closable = { def close(): Unit }
}
