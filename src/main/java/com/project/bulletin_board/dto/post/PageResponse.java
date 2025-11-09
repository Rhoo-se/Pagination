package com.project.bulletin_board.dto.post;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T>{

    private final List<T> content;
    private final int totalPages;
    private final long totalElements;
    private final int size;
    private final int number; // 현재 페이지 번호
    private final boolean first;
    private final boolean last;
    private final boolean hasNext;
    private final boolean hasPrevious;


    public PageResponse(Page<T> page){
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }
}
