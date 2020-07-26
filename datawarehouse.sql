CREATE SOURCE gtfs
FROM KAFKA BROKER 'localhost:9092' TOPIC 'gtfs'
FORMAT TEXT;

CREATE MATERIALIZED VIEW ROUTEALL AS
    SELECT (text::JSONB)->>'id' as id,
           (text::JSONB)->>'vid' as vid,
           (text::JSONB)->>'label' as label,
           CAST((text::JSONB)->'lastUpdate' as float) as lastUpdate,
           CAST((text::JSONB)->'lat' as float) as lat,
           CAST((text::JSONB)->'lon' as float) as lon
    FROM (SELECT * FROM gtfs);
