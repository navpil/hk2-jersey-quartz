package io.github.navpil.q.quartz;

import io.github.navpil.q.books.BookService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@DisallowConcurrentExecution
public class UpdateBookServiceJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateBookServiceJob.class);

    private final BookService bookService;

    @Inject
    public UpdateBookServiceJob(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.warn("------ Will execute quartz job ----------");
        bookService.changeLastUpdated();
    }
}
