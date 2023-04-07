package io.github.navpil.q.oldschool;

import io.github.navpil.q.BooksApplication;
import io.github.navpil.q.books.BookService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/nonmanaged/bookshack")
public class NonManagedOldSchoolServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (PrintWriter pw = resp.getWriter()) {
            BookService bookService = UberContextHorribleHack.getClassForApp(
                    BooksApplication.NAME, BookService.class);
            pw.write("Book Service was updated: " + bookService.getLastUpdated());
            pw.flush();
        }
    }
}
