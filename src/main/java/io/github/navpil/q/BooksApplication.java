package io.github.navpil.q;

import io.github.navpil.q.common.HK2JobFactory;
import io.github.navpil.q.common.ImmediateFeature;
import io.github.navpil.q.quartz.DummyQuartzResource;
import io.github.navpil.q.quartz.UpdateBookServiceJob;
import io.github.navpil.q.quartz.BooksQuartzJobExecutor;
import io.github.navpil.q.books.BookService;
import io.github.navpil.q.books.BookServiceImpl;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.quartz.spi.JobFactory;

@ApplicationPath("/booksapp/*")
public class BooksApplication extends ResourceConfig {

    public BooksApplication() {
        // Need to register a dummy resource to eager loading of the Quartz Scheduler
        //   (other ways did not work, otherwise I would make an eager initializion of BooksQuartzJobExecutor)
        super(
                DummyQuartzResource.class
        );
        // Usual scanning...
        packages("io.github.navpil.q.books");
        // So that DummyQuartzResource is indeed immediately initialized
        register(ImmediateFeature.class);
        register(new AbstractBinder(){
            @Override
            protected void configure() {
                // Quartz Job Executor will be injected into a Dummy Quartz Resource and will control its lifecycle
                bind(BooksQuartzJobExecutor.class).to(BooksQuartzJobExecutor.class);
                // HK2JobFactory will have reference to a service locator
                bind(HK2JobFactory.class).to(JobFactory.class);
                // Actual job we want to startup (can be many of them)
                bind(UpdateBookServiceJob.class).to(UpdateBookServiceJob.class);

                //Singleton BookService, so that we can see inner state updates through the controller
                bind(BookServiceImpl.class).to(BookService.class)
                        .in(Singleton.class);
            }
        });
    }

}
