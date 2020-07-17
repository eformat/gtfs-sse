package com.example.gtfs;

import com.example.data.Vehicle;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
    @Path("/435")
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
    @Path("/id/{route_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getRouteById",
            summary = "get routes by id",
            description = "This operation returns all vehicle movements for route by id",
            deprecated = false,
            hidden = false)
    public List<Vehicle> getRouteById(@PathParam String route_id) {
        return entityManager.createQuery("select r from ROUTE" + route_id + " r", Vehicle.class).getResultList();
    }
}
