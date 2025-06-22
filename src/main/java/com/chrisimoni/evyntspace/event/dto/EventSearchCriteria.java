package com.chrisimoni.evyntspace.event.dto;

import com.chrisimoni.evyntspace.common.dto.CommonPaginationAndSortCriteria;
import com.chrisimoni.evyntspace.event.enums.EventStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EventSearchCriteria extends CommonPaginationAndSortCriteria {
        private String title;
        private EventStatus status;
        private String country;
        private UUID organizerId;
}