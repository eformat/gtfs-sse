CREATE SOURCE gtfs
FROM KAFKA BROKER 'localhost:9092' TOPIC 'gtfs'
FORMAT TEXT;

CREATE MATERIALIZED VIEW all_gtfs AS
    SELECT (text::JSONB)->'id' as id,
           (text::JSONB)->'label' as label,
           (text::JSONB)->'lastUpdate' as lastUpdate,
           (text::JSONB)->'lat' as lat,
           (text::JSONB)->'lon' as lon
    FROM (SELECT * FROM gtfs);

CREATE MATERIALIZED VIEW BUS435 AS
    SELECT id, label, CAST(lastUpdate AS float), CAST(lat as float), CAST(lon as float)
    FROM all_gtfs
    WHERE label = '"435-1662"';
