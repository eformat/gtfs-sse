# gtfs bus positions into kafka+sse, timely data flow

- Retrieves Brisbane transport GTFS realtime data
- Uses Quarkus reactice app to store data in kafka
- Connects Materialize.io streaming database to kafka
- Browse data using SQL

Uses:

- [Kafka](htps://strimzi.io)
- [Materalize](https://materialize.io)
- [Apicurio Schema Registry](https://github.com/Apicurio/apicurio-registry)

Requires:

- podman-compose (or docker-compose)
- jdk11, maven 3.6+

Run demo
```bash
# create materalize data directory
mkdir -p /tmp/mzdata/

# run infra locally (docker-compose should work also)
podman-compose up -d

# maven compile and run java app
cd kafka-registry
mvn compile quarkus:dev

# browse kafka topic
kafkacat -b localhost:9092 -C -o end -q -u -t gtfs

# server side events available (for fun)
http http://localhost:8080/gtfs/stream --stream

# delete topic if you need to reset data (restart materaliaze container as well)
/opt/kafka_2.12-2.2.0/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic gtfs
```

TimelyDataFlow using materialize.io
```
# psql -h localhost -p 6875 materialize

CREATE SOURCE gtfs
FROM KAFKA BROKER 'localhost:9092' TOPIC 'gtfs'
FORMAT TEXT;

SHOW COLUMNS FROM gtfs;
DROP SOURCE gtfs;

CREATE MATERIALIZED VIEW all_gtfs AS
    SELECT (text::JSONB)->'id' as id,
           (text::JSONB)->'label' as label,
           (text::JSONB)->'lastUpdate' as lastUpdate,
           (text::JSONB)->'lat' as lat,
           (text::JSONB)->'lon' as lon
    FROM (SELECT * FROM gtfs);

SELECT * from all_gtfs;
SHOW COLUMNS FROM all_gtfs;

DROP VIEW all_gtfs;

CREATE MATERIALIZED VIEW BUS435 AS
    SELECT id, label, CAST(lastUpdate AS float), CAST(lat as float), CAST(lon as float)
    FROM all_gtfs
    WHERE label = '"435-1607"';

SELECT * from BUS435;
SHOW COLUMNS FROM BUS435;

DROP VIEW BUS435;
```

Apicurio schema registry
```
http://localhost:8081/ui/artifacts
http://localhost:8081/api
```

#### FIXME
- using text not avro, materalize cannot browse apicurio even in compat mode for avro schema 
