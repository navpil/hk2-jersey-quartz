package io.github.navpil.q.oldschool;

import io.github.navpil.q.BooksApplicationWithUberHack;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.ServiceLocator;

@Path("dummyhorriblehack")
@Immediate
public class InitializeUberContextResource {

    @Inject
    public InitializeUberContextResource(ServiceLocator locator) {
        UberContextHorribleHack.putServiceLocator(BooksApplicationWithUberHack.NAME, locator);
    }

    //Jersey complains about resources with no endpoints
    @GET
    public Response dummy() {
        return Response.ok().build();
    }


}
