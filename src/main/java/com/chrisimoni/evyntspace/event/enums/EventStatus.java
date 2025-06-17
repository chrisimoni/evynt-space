package com.chrisimoni.evyntspace.event.enums;

public enum EventStatus {
    PENDING_PUBLISH, // Event is complete and awaiting its scheduledPublishTime.
    PUBLISHED,      // Event is live and visible to the public.
    ARCHIVED        // Event is no longer active/visible (optional, for historical data).
}
