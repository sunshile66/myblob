package com.myblob.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> results;
    private long count;
    private int page;
    private int size;

    @JsonProperty("total_pages")
    private int totalPages;

    /** Alias for 'results' — Spring Data Page compatibility */
    @JsonProperty("content")
    public List<T> getContent() { return results; }

    /** Alias for 'count' — Spring Data Page compatibility */
    @JsonProperty("totalElements")
    public long getTotalElements() { return count; }

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .results(page.getContent())
                .count(page.getTotalElements())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .build();
    }
}
