package com.nortadas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link PageResult}: the framework-free page value's invariant
 * checks and its pagination math ({@code totalPages}, {@code lastPageNumber},
 * {@code hasPrevious}, {@code hasNext}) at the boundaries that matter — zero
 * elements, an exact multiple of the page size, and a partial last page.
 */
class PageResultTest {

    private static PageResult<String> page(int pageNumber, int pageSize, long totalElements) {
        return new PageResult<>(List.of(), pageNumber, pageSize, totalElements);
    }

    // ---- invariants ---------------------------------------------------------

    @Test
    @DisplayName("rejects null content")
    void rejectsNullContent() {
        assertThatThrownBy(() -> new PageResult<>(null, 0, 10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("content");
    }

    @Test
    @DisplayName("rejects a negative page number")
    void rejectsNegativePageNumber() {
        assertThatThrownBy(() -> page(-1, 10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageNumber");
    }

    @Test
    @DisplayName("rejects a non-positive page size")
    void rejectsNonPositivePageSize() {
        assertThatThrownBy(() -> page(0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageSize");
    }

    @Test
    @DisplayName("rejects a negative total")
    void rejectsNegativeTotal() {
        assertThatThrownBy(() -> page(0, 10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalElements");
    }

    // ---- totalPages / lastPageNumber ---------------------------------------

    @Test
    @DisplayName("an empty result still spans one page")
    void emptyResultHasOnePage() {
        PageResult<String> page = page(0, 20, 0);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.lastPageNumber()).isZero();
    }

    @ParameterizedTest(name = "total={0}, size={1} -> {2} pages")
    @CsvSource({
            "40, 20, 2",   // exact multiple
            "41, 20, 3",   // one over a multiple -> partial extra page
            "39, 20, 2",   // partial last page
            "1, 20, 1",    // single element
            "20, 20, 1"    // exactly one full page
    })
    @DisplayName("computes total pages with ceiling division")
    void computesTotalPages(long total, int size, int expectedPages) {
        PageResult<String> page = page(0, size, total);
        assertThat(page.totalPages()).isEqualTo(expectedPages);
        assertThat(page.lastPageNumber()).isEqualTo(expectedPages - 1);
    }

    // ---- hasPrevious / hasNext ---------------------------------------------

    @Test
    @DisplayName("the first of several pages has a next but no previous")
    void firstPageNavigation() {
        PageResult<String> first = page(0, 20, 40);
        assertThat(first.hasPrevious()).isFalse();
        assertThat(first.hasNext()).isTrue();
    }

    @Test
    @DisplayName("the last of several pages has a previous but no next")
    void lastPageNavigation() {
        PageResult<String> last = page(1, 20, 40);
        assertThat(last.hasPrevious()).isTrue();
        assertThat(last.hasNext()).isFalse();
    }

    @Test
    @DisplayName("a middle page has both a previous and a next")
    void middlePageNavigation() {
        PageResult<String> middle = page(1, 20, 60);
        assertThat(middle.hasPrevious()).isTrue();
        assertThat(middle.hasNext()).isTrue();
    }

    @Test
    @DisplayName("the sole page has neither a previous nor a next")
    void solePageNavigation() {
        PageResult<String> only = page(0, 20, 5);
        assertThat(only.hasPrevious()).isFalse();
        assertThat(only.hasNext()).isFalse();
    }
}
