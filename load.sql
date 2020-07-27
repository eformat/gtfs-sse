CREATE SOURCE latestgtfs
FROM KAFKA BROKER 'localhost:9092' TOPIC 'latest-gtfs'
FORMAT TEXT
ENVELOPE UPSERT;

CREATE MATERIALIZED VIEW ROUTELATEST AS
    SELECT (text::JSONB)->>'id' as id,
           (text::JSONB)->>'vid' as vid,
           (text::JSONB)->>'label' as label,
           CAST((text::JSONB)->'lastUpdate' as float) as lastUpdate,
           CAST((text::JSONB)->'lat' as float) as lat,
           CAST((text::JSONB)->'lon' as float) as lon
    FROM (SELECT * FROM latestgtfs);

CREATE MATERIALIZED VIEW ROUTE435 AS
    SELECT *
    FROM ROUTELATEST
    WHERE label = '435-1662';

CREATE MATERIALIZED VIEW ROUTE444 AS
    SELECT *
    FROM ROUTELATEST
    WHERE label = '444-1662';

CREATE MATERIALIZED VIEW ROUTEUQSL AS
    SELECT *
    FROM ROUTELATEST
    WHERE label = 'UQSL-1410';
