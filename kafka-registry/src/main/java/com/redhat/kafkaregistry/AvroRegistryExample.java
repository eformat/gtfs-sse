package com.redhat.kafkaregistry;

import io.reactivex.Flowable;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Path("/prices")
public class AvroRegistryExample {

    private Random random = new Random();
    private String[] symbols = new String[]{"RHT", "IBM", "MSFT", "AMZN"};

    /**
     * Generate price data
     * @return
     * @throws IOException
     */
    private Record generatePrice() throws IOException {
        Schema schema = new Schema.Parser().parse(
                new File(getClass().getClassLoader().getResource("price-schema.avsc").getFile())
        );
        Record record = new GenericData.Record(schema);
        record.put("symbol", symbols[random.nextInt(4)]);
        record.put("price", String.format("%.2f", random.nextDouble() * 100));
        return record;
    }

    /**
     * Send prices to kafka topic
     * @return
     * @throws IOException
     */
    @Outgoing("price-out")
    public Flowable<Record> generate() throws IOException {
        return Flowable.interval(1000, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .map(tick -> {
                    return generatePrice();
                });
    }

    @Inject
    @Channel("price-in") Publisher<Record> prices;

    /**
     * Read kafka topic and send as SSE
     * @return
     */
    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    //@SseElementType(MediaType.APPLICATION_JSON) //avro/binary
    public Publisher<Record> stream() {
        return prices;
    }
}
