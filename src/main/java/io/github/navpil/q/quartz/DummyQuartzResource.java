package io.github.navpil.q.quartz;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.hk2.api.Immediate;

@Path("dummyquartz")
@Produces({MediaType.APPLICATION_JSON})
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
@Immediate
public class DummyQuartzResource {

    private final BooksQuartzJobExecutor executor;

    @Inject
    public DummyQuartzResource(BooksQuartzJobExecutor executor) {
        this.executor = executor;
    }

    @PostConstruct
    public void startupExecutor() {
        executor.start();
    }

    @PreDestroy
    public void shutdownExecutor() {
        executor.shutdown();
    }

    @GET
    public Response dummyEndpoint() {
        return Response.ok().build();
    }


}
