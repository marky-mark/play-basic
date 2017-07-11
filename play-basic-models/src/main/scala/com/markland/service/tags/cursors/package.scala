package com.markland.service.tags

import com.markland.service.Cursor
import com.markland.service.models.PageNext
import com.markland.service.refs.NextRef

package object cursors {
  type NextCursor = Cursor[NextRef]
  type PageNextCursor =Cursor[PageNext]
}
