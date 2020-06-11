package com.example.gtfs;

import com.google.common.io.ByteStreams;
import com.google.transit.realtime.GtfsRealtime;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
    @ConfigProperty(name = "gtfs.pollValue", defaultValue = "10")
    public int pollValue;

    /**
     * Blocking read from gtfs source
     * @return
     */
    private Multi<String> readGtfs() {
        Multi<String> blocking = Multi.createFrom().iterable(_read()).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
        return blocking;
    }

    /**
     * Send gtfs data to kafka topic
     *
     * @return
     * @throws IOException
     */
    @Outgoing("gtfs-out")
    public Multi<String> generate() throws IOException {
        Multi<Long> ticks = Multi.createFrom().ticks().every(Duration.ofSeconds(pollValue)).onOverflow().drop();
        return ticks.onItem().produceMulti(
                x -> readGtfs()
        ).merge();
    }

    @Inject
    @Channel("gtfs-in")
    Publisher<String> rawData;

    /**
     * Read kafka topic and send as SSE
     *
     * @return
     */
    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON) //avro/binary
    public Publisher<String> stream() {
        return rawData;
    }

    /**
     * internal read method from realtime gtfs datasource
     * @return
     */
    private List<String> _read() {
        GtfsRealtime.FeedMessage msg = null;
        byte[] gtfs;
        List<String> vehicles = null;
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
