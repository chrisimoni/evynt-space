package com.chrisimoni.evyntspace.user.dto;

import com.chrisimoni.evyntspace.common.dto.CommonPaginationAndSortCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchCriteria extends CommonPaginationAndSortCriteria {
    private String name;
}
