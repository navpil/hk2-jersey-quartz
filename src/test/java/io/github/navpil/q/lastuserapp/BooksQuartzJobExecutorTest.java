package io.github.navpil.q.lastuserapp;

import io.github.navpil.q.books.BookServiceImpl;
import io.github.navpil.q.common.SimpleJobFactory;
import io.github.navpil.q.quartz.BooksQuartzJobExecutor;
import io.github.navpil.q.quartz.UpdateBookServiceJob;
import org.junit.Test;

public class BooksQuartzJobExecutorTest {

    @Test
    public void demonstrateQuartzJob() {
        SimpleJobFactory jobFactory = new SimpleJobFactory();
        jobFactory.register(UpdateBookServiceJob.class, () -> new UpdateBookServiceJob(new BookServiceImpl()));

        BooksQuartzJobExecutor jobExecutor = new BooksQuartzJobExecutor(jobFactory);
        jobExecutor.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        jobExecutor.shutdown();
    }
  
}