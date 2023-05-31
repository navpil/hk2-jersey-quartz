package io.github.navpil.q;

import io.github.navpil.q.books.BookService;
import io.github.navpil.q.books.BookServiceImpl;
import io.github.navpil.q.common.HK2JobFactory;
import io.github.navpil.q.quartz.BooksQuartzJobExecutor;
import io.github.navpil.q.quartz.UpdateBookServiceJob;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.quartz.spi.JobFactory;

@ApplicationPath("/booksapp/*")
public class BooksApplication extends ResourceConfig {

    public BooksApplication() {
        packages("io.github.navpil.q.books");

        //This one will start the quartz scheduler
        register(new BooksApplicationContainerLifecycleListener());
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(BooksQuartzJobExecutor.class).in(Singleton.class);
                // HK2JobFactory will have reference to a service locator
                bind(HK2JobFactory.class).to(JobFactory.class);
                // Actual job we want to startup (can be many of them)
                bindAsContract(UpdateBookServiceJob.class);

                bind("Started using ContainerLifecycleListener").to(String.class).named("Description");

                //Singleton BookService, so that we can see inner state updates through the controller
                bind(BookServiceImpl.class).to(BookService.class)
                        .in(Singleton.class);
            }
        });
    }

    public static class BooksApplicationContainerLifecycleListener implements ContainerLifecycleListener {

        private BooksQuartzJobExecutor executor;

        public void onStartup(Container container)
        {
            executor = container.getApplicationHandler().getInjectionManager()
                    .getInstance(BooksQuartzJobExecutor.class);
            //Technically there is no need to call ".start()" because starting is handled by @PostConstruct
            // but we still need to get the reference to the class it to be instantiated
            executor.start();
        }

        public void onReload(Container container) {/*...*/}
        public void onShutdown(Container container) {
            if (executor != null) {
                executor.shutdown();
            } else {
                container.getApplicationHandler().getInjectionManager()
                        .getInstance(BooksQuartzJobExecutor.class).shutdown();
            }
        }
    }

}
