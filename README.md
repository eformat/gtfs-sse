# registry-example-avro

Apicurio Schema Registry

https://developers.redhat.com/blog/2019/12/16/getting-started-with-red-hat-integration-service-registry/
https://access.redhat.com/documentation/en-us/red_hat_integration/2020-04/html-single/getting_started_with_service_registry/index#installing-registry-kafka-kubernetes-storage
https://debezium.io/blog/2020/04/09/using-debezium-wit-apicurio-api-schema-registry/
https://developers.redhat.com/blog/2019/12/17/replacing-confluent-schema-registry-with-red-hat-integration-service-registry/

```bash
podman-compose up
curl -X POST -H "Content-type: application/json; artifactType=AVRO" -H "X-Registry-ArtifactId: prices-value" --data '{"type":"record","name":"price","namespace":"com.redhat","fields":[{"name":"symbol","type":"string"},{"name":"price","type":"string"}]}' http://localhost:8081/api/artifacts -s | jq

cd kafka-registry
mvn compile quarkus:dev

kafkacat -b localhost:9092 -C -o end -q -u -t prices
```

browse registry
```
http://localhost:8081/ui/artifacts
http://localhost:8081/api
```
