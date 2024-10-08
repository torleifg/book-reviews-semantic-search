package com.github.torleifg.semanticsearchonnx.book.web;

import com.github.torleifg.semanticsearchonnx.book.service.BookService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Controller
public class SearchController {
    private final BookService bookService;

    public SearchController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String search(Model model) {
        model.addAttribute("searchType", null);

        return "index";
    }

    @PostMapping
    public String search(Model model, @RequestParam(required = false) String query, @RequestParam String searchType) {
        final List<Map<String, Object>> results;

        if ("semantic".equals(searchType)) {
            final List<Document> documents;

            if (isNotBlank(query)) {
                model.addAttribute("query", query);
                documents = bookService.semanticSearch(query);
            } else {
                documents = bookService.passage();
            }

            results = new ArrayList<>(documents.size());

            for (final Document document : documents) {
                results.add(document.getMetadata());
            }
        } else {
            if (isNotBlank(query)) {
                model.addAttribute("query", query);
                results = bookService.fulltextSearch(query);
            } else {
                results = new ArrayList<>();
            }
        }

        model.addAttribute("searchType", searchType);
        model.addAttribute("results", results);

        return "index";
    }
}
