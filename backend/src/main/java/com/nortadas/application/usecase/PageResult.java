package com.nortadas.application.usecase;

import java.util.List;

/**
 * A minimal, framework-free page of results returned by the query use cases
 * (e.g. {@code GetBeachListUseCase}). Deliberately <em>not</em> Spring Data's
 * {@code Page}/{@code Pageable}: the application layer depends only on the domain
 * and plain Java (docs/architecture.md §1), so pagination is expressed with this
 * small value rather than by leaking a persistence-framework type across the
 * layer boundary. The web layer translates it into HAL pagination metadata and
 * links.
 *
 * @param content       the elements on this page (never {@code null}, possibly empty)
 * @param pageNumber    zero-based index of this page
 * @param pageSize      the requested page size (the maximum number of elements a
 *                      page may hold); must be positive
 * @param totalElements total number of elements across all pages
 * @param <T>           element type
 */
public record PageResult<T>(List<T> content, int pageNumber, int pageSize, long totalElements) {

    public PageResult {
        if (content == null) {
            throw new IllegalArgumentException("PageResult content cannot be null!");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("PageResult pageNumber cannot be negative!");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("PageResult pageSize must be positive!");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("PageResult totalElements cannot be negative!");
        }
    }

    /** Total number of pages spanning {@link #totalElements} at {@link #pageSize}; at least 1. */
    public int totalPages() {
        if (totalElements == 0) {
            return 1;
        }
        return (int) ((totalElements + pageSize - 1) / pageSize);
    }

    /** Zero-based index of the last page. */
    public int lastPageNumber() {
        return totalPages() - 1;
    }

    /** Whether a page precedes this one. */
    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    /** Whether another page follows this one. */
    public boolean hasNext() {
        return pageNumber < lastPageNumber();
    }
}
