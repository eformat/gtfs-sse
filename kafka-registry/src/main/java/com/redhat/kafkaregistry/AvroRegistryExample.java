package com.redhat.kafkaregistry;

import com.google.common.io.ByteStreams;
import com.google.transit.realtime.GtfsRealtime;
import io.reactivex.Flowable;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Path("/gtfs")
public class AvroRegistryExample {

    private static final Logger log = LoggerFactory.getLogger(AvroRegistryExample.class);

    @Inject
    VehicleService vehicleService;

    @ConfigProperty(name = "gtfs.url", defaultValue = "https://gtfsrt.api.translink.com.au/Feed/SEQ")
    public String optUrl;

    @ConfigProperty(name = "gtfs.feed", defaultValue = "bne-bus")
    public String optFeed;

    final String optApiKey = "";

    @ConfigProperty(name = "gtfs.pollValue", defaultValue = "5")
    public int pollValue;

    private Record readGtfs() {
        GtfsRealtime.FeedMessage msg = null;
        byte[] gtfs;
        Record record = null;
        try (InputStream in = GetGtfs.feedUrlStream(optApiKey, optFeed, optUrl)) {
            gtfs = ByteStreams.toByteArray(in);
            msg = GtfsRealtime.FeedMessage.parseFrom(gtfs);

            long timestamp;
            if (msg != null && msg.hasHeader() && msg.getHeader().hasTimestamp()) {
                timestamp = msg.getHeader().getTimestamp();
            } else {
                timestamp = new GregorianCalendar().getTimeInMillis();
            }
            Schema schema = new Schema.Parser().parse(
                    new File(getClass().getClassLoader().getResource("gtfs-schema.avsc").getFile())
            );

            String vehiclesAsJsonString = vehicleService.getVehiclesAsString(msg);

            record = new GenericData.Record(schema);
            record.put("time", timestamp);
            record.put("data", vehiclesAsJsonString);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return record;
    }

    /**
     * Send gtfs data to kafka topic
     *
     * @return
     * @throws IOException
     */
    @Outgoing("gtfs-out")
    public Flowable<Record> generate() throws IOException {
        return Flowable.interval(pollValue, TimeUnit.SECONDS)
                .onBackpressureDrop()
                .map(tick -> {
                    return readGtfs();
                });
    }

    @Inject
    @Channel("gtfs-in")
    Publisher<Record> rawData;

    /**
     * Read kafka topic and send as SSE
     *
     * @return
     */
    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    //@SseElementType(MediaType.APPLICATION_JSON) //avro/binary
    public Publisher<Record> stream() {
        return rawData;
    }

}
