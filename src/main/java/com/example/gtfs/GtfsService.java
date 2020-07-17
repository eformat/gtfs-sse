package com.example.gtfs;

import com.example.data.GetGtfs;
import com.example.data.Vehicle;
import com.google.common.io.ByteStreams;
import com.google.transit.realtime.GtfsRealtime;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@ApplicationScoped
@Path("/gtfs")
public class GtfsService {

    private static final Logger log = LoggerFactory.getLogger(GtfsService.class);

    @Inject
    VehicleService vehicleService;

    @ConfigProperty(name = "gtfs.url", defaultValue = "https://gtfsrt.api.translink.com.au/Feed/SEQ")
    public String optUrl;

    @ConfigProperty(name = "gtfs.feed", defaultValue = "bne-bus")
    public String optFeed;

    final String optApiKey = "";

    /* Poll time for updating from gtfs source */
    @ConfigProperty(name = "gtfs.pollValue", defaultValue = "3")
    public int pollValue;

    /**
     * Blocking read from gtfs source
     *
     * @return
     */
    private Multi<Vehicle> readGtfs() {
        Multi<Vehicle> blocking = Multi.createFrom().iterable(_read()).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
        return blocking;
    }

    /**
     * Send gtfs data to kafka topic
     *
     * @throws IOException
     * @returnrealtime
     */
    @Outgoing("gtfs-out")
    public Multi<Vehicle> generate() throws IOException {
        Multi<Long> ticks = Multi.createFrom().ticks().every(Duration.ofSeconds(pollValue)).onOverflow().drop();
        return ticks.onItem().produceMulti(
                x -> readGtfs()
        ).merge();
    }

    @Inject
    @Channel("gtfs-in")
    @Broadcast
    Publisher<Vehicle> rawData;

    /**
     * Read kafka topic and send as SSE
     *
     * @return
     */
    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON) //avro/binary
    @Operation(operationId = "stream",
            summary = "stream gtfs data",
            description = "This operation returns all vehicle gtfs data from kafka",
            deprecated = false,
            hidden = false)
    public Publisher<Vehicle> stream() {
        return rawData;
    }

    /**
     * internal read method from realtime gtfs datasource
     *
     * @return
     */
    private List<Vehicle> _read() {
        GtfsRealtime.FeedMessage msg = null;
        byte[] gtfs;
        List<Vehicle> vehicles = null;
        try (InputStream in = GetGtfs.feedUrlStream(optApiKey, optFeed, optUrl)) {
            gtfs = ByteStreams.toByteArray(in);
            msg = GtfsRealtime.FeedMessage.parseFrom(gtfs);
            vehicles = vehicleService.getVehicles(msg);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return vehicles;
    }

}
