package io.github.navpil.q.books;

import java.util.List;

    public interface BookService {

    List<String> listBooks();

    String getLastUpdated();

    void changeLastUpdated();
}
