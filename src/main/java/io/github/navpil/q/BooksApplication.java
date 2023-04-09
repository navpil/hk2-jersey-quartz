package io.github.navpil.q;

import io.github.navpil.q.books.BookService;
import io.github.navpil.q.books.BookServiceImpl;
import io.github.navpil.q.common.HK2JobFactory;
import io.github.navpil.q.common.ImmediateFeature;
import io.github.navpil.q.oldschool.UberContextHorribleHack;
import io.github.navpil.q.quartz.BooksQuartzJobExecutor;
import io.github.navpil.q.quartz.UpdateBookServiceJob;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.quartz.spi.JobFactory;

@ApplicationPath("/booksapp/*")
public class BooksApplication extends ResourceConfig {

    public static final String NAME = "BooksApplication";

    public BooksApplication() {
        super(
                //While this is possible, using BooksApplicationContainerLifecycleListener is arguably cleaner
//                InitializeUberContextHorribleHackResource.class
        );
        // Usual scanning...
        packages("io.github.navpil.q.books");
        // So that BooksQuartzJobExecutor is indeed immediately initialized,
        //    not needed if ContainerLifecycleListener is used for quartz job startup
        register(ImmediateFeature.class);

        register(new BooksApplicationContainerLifecycleListener());
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(BooksQuartzJobExecutor.class)
                        .in(Immediate.class);
                // HK2JobFactory will have reference to a service locator
                bind(HK2JobFactory.class).to(JobFactory.class);
                // Actual job we want to startup (can be many of them)
                bindAsContract(UpdateBookServiceJob.class);

                //Singleton BookService, so that we can see inner state updates through the controller
                bind(BookServiceImpl.class).to(BookService.class)
                        .in(Singleton.class);
            }
        });
    }

    public static class BooksApplicationContainerLifecycleListener implements ContainerLifecycleListener {
        public void onStartup(Container container)
        {
            ServiceLocator serviceLocator = container.getApplicationHandler().getInjectionManager()
                    .getInstance(ServiceLocator.class);
            UberContextHorribleHack.putServiceLocator(BooksApplication.NAME, serviceLocator);

            //Other things can be done here, for example:
            //   serviceLocator.getService(BooksQuartzJobExecutor.class).start()
        }

        public void onReload(Container container) {/*...*/}
        public void onShutdown(Container container) {
            //If BooksQuartzJobExecutor was started in onStartup, you may stop it here:
            //   serviceLocator.getService(BooksQuartzJobExecutor.class).shutdown();
            // alternatively you may store the reference to the service in this class field and stop it
            //   (in case it's not singleton)
        }
    }

}
