package com.example.gtfs;

import com.example.data.Vehicle;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.annotations.SseElementType;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.util.List;

@Path("/query")
public class QueryService {

    private final Logger log = LoggerFactory.getLogger(QueryService.class);

    @Inject
    EntityManager entityManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getAllRoutes",
            summary = "get all vehicles",
            description = "This operation returns all vehicles for all routes",
            deprecated = false,
            hidden = false)
    public List<Vehicle> getAllRoutes() {
        return Vehicle.listAll();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "countAll",
            summary = "count all vehicles",
            description = "This operation returns a count of all vehicle movements",
            deprecated = false,
            hidden = false)
    public Long countAll() {
        return Vehicle.count();
    }

    @GET
    @Path("/route435")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getRoutes",
            summary = "get 435 routes",
            description = "This operation returns all vehicle movements for route 435",
            deprecated = false,
            hidden = false)
    public List<Vehicle> getRoute435() {
        return Vehicle.Route435.listAll();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/count435")
    @Operation(operationId = "count435",
            summary = "count 435 vehicles",
            description = "This operation returns a count of all 435 vehicle movements",
            deprecated = false,
            hidden = false)
    public Long count435() {
        return Vehicle.Route435.count();
    }

    @GET
    @Path("/{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getRouteById",
            summary = "get routes by id",
            description = "This operation returns all vehicle movements for route by id",
            deprecated = false,
            hidden = false)
    public List<Vehicle> getRouteById(@PathParam String route_id) {
        return entityManager.createQuery("select r from ROUTE" + route_id + " r", Vehicle.class).getResultList();
    }

    @GET
    @Path("/count/{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getRouteCountById",
            summary = "get vehicle count by route id",
            description = "This operation returns a count of all vehicle movements by route id",
            deprecated = false,
            hidden = false)
    public Long getRouteCountById(@PathParam String route_id) {
        return entityManager.createQuery("select count(*) from ROUTE" + route_id, Long.class).getSingleResult();
    }

    @GET
    @Path("/stream/{route_id}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON) //avro/binary
    @Operation(operationId = "stream",
            summary = "stream gtfs data by id",
            description = "This operation returns vehicle gtfs data by id",
            deprecated = false,
            hidden = false)
    public Publisher<Vehicle> stream(@PathParam String route_id) {
        Multi<Long> ticks = Multi.createFrom().ticks().every(Duration.ofSeconds(5)).onOverflow().drop();
        return ticks.on().subscribed(subscription -> log.info("We are subscribed!"))
                .on().cancellation(() -> log.info("Downstream has cancelled the interaction"))
                .onFailure().invoke(failure -> log.warn("Failed with " + failure.getMessage()))
                .onCompletion().invoke(() -> log.info("Completed"))
                .onItem().produceMulti(
                        x -> routeMulti(route_id)
                ).merge();
    }

    private Multi<Vehicle> routeMulti(String route_id) {
        return Multi.createFrom().iterable(getRouteById(route_id)).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}
