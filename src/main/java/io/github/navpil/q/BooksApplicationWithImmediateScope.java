package io.github.navpil.q;

import io.github.navpil.q.books.BookService;
import io.github.navpil.q.books.BookServiceImpl;
import io.github.navpil.q.common.HK2JobFactory;
import io.github.navpil.q.common.ImmediateFeature;
import io.github.navpil.q.quartz.BooksQuartzJobExecutor;
import io.github.navpil.q.quartz.UpdateBookServiceJob;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.quartz.spi.JobFactory;

@ApplicationPath("/booksapp-with-immediate/*")
public class BooksApplicationWithImmediateScope extends ResourceConfig {

    public BooksApplicationWithImmediateScope() {
        packages("io.github.navpil.q.books");

        // So that BooksQuartzJobExecutor is indeed immediately initialized,
        register(ImmediateFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(BooksQuartzJobExecutor.class)
                        .in(Immediate.class);
                // HK2JobFactory will have reference to a service locator
                bind(HK2JobFactory.class).to(JobFactory.class);
                // Actual job we want to startup (can be many of them)
                bindAsContract(UpdateBookServiceJob.class);

                bind("Started using Immediate scope").to(String.class).named("Description");

                //Singleton BookService, so that we can see inner state updates through the controller
                bind(BookServiceImpl.class).to(BookService.class)
                        .in(Singleton.class);
            }
        });
    }

}
