package io.github.navpil.q.oldschool;

import io.github.navpil.q.BooksApplication;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.ServiceLocator;

@Path("dummyhorriblehack")
@Singleton
@Immediate
public class InitializeUberContextHorribleHackResource {

    @Inject
    public InitializeUberContextHorribleHackResource(ServiceLocator locator) {
        UberContextHorribleHack.putServiceLocator(BooksApplication.NAME, locator);
    }

}
