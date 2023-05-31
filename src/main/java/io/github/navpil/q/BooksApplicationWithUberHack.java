package io.github.navpil.q;

import io.github.navpil.q.books.BookService;
import io.github.navpil.q.books.BookServiceImpl;
import io.github.navpil.q.oldschool.UberContextHorribleHack;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

@ApplicationPath("/booksappwithhack/*")
public class BooksApplicationWithUberHack extends ResourceConfig {

    public static final String NAME = "BooksApplicationWithUberHack";

    public BooksApplicationWithUberHack() {
        super(
                //While this is possible, using BooksApplicationContainerLifecycleListener is arguably cleaner
//                InitializeUberContextResource.class
        );
        // Usual scanning...
        packages("io.github.navpil.q.books");

        register(new HorribleHackContainerLifecycleListener());
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                //Singleton BookService, so that we can see inner state updates through the controller
                bind(BookServiceImpl.class).to(BookService.class)
                        .in(Singleton.class);
            }
        });
    }

    public static class HorribleHackContainerLifecycleListener implements ContainerLifecycleListener {
        public void onStartup(Container container)
        {
            ServiceLocator serviceLocator = container.getApplicationHandler().getInjectionManager()
                    .getInstance(ServiceLocator.class);
            UberContextHorribleHack.putServiceLocator(BooksApplicationWithUberHack.NAME, serviceLocator);

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
