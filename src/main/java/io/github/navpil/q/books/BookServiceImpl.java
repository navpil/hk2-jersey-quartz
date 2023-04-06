package io.github.navpil.q.books;

import java.util.Date;
import java.util.List;

public class BookServiceImpl implements BookService {

    private volatile String lastUpdated = "null";

    public List<String> listBooks() {
        return List.of("Shevchenko", "Franko");
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void changeLastUpdated() {
        lastUpdated = "Updated at " + new Date();
    }
}
