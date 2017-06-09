package com.markland.service.tags

package object ids {
  type FlowId = com.markland.service.Id[String, com.markland.service.refs.FlowRef]
  type InfoId = com.markland.service.Id[java.util.UUID, com.markland.service.models.Info]
  type SalesChannelId = com.markland.service.Id[java.util.UUID, com.markland.service.refs.SalesChannelRef]
  type TrackingId = com.markland.service.Id[java.util.UUID, com.markland.service.refs.TrackingRef]
}