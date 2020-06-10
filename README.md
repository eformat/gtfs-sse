# gtfs bus positions into kafka+sse

Using avro and schema registry
```bash
# run infra locally
podman-compose up

# compile
cd kafka-registry
mvn compile quarkus:dev

# browse topic
kafkacat -b localhost:9092 -C -o end -q -u -t gtfs

# sse
http http://localhost:8080/gtfs/stream --stream

# delete topic
/opt/kafka_2.12-2.2.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic gtfs
```

browse registry
```
http://localhost:8081/ui/artifacts
http://localhost:8081/api
```
