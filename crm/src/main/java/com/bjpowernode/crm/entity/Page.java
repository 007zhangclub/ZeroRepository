package com.bjpowernode.crm.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Page<T> extends R <T>{
    //分页数据
    private List<T> records;
    private Integer totalCounts;
    private Integer totalPages;
    private Integer pageNo;
    private Integer pageSize;
}
