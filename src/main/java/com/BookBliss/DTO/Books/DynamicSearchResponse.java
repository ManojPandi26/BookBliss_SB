package com.BookBliss.DTO.Books;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicSearchResponse {
    private List<BookSearchResponse> exactMatches;
    private List<BookSearchResponse> suggestedMatches;
    private List<String> trendingSearches;
    private List<String> recentSearches;

}
