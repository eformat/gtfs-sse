CREATE SOURCE gtfs
FROM KAFKA BROKER 'localhost:9092' TOPIC 'gtfs'
FORMAT TEXT;

CREATE MATERIALIZED VIEW all_gtfs AS
    SELECT (text::JSONB)->>'id' as id,
           (text::JSONB)->>'vid' as vid,
           (text::JSONB)->>'label' as label,
           CAST((text::JSONB)->'lastUpdate' as float) as lastUpdate,
           CAST((text::JSONB)->'lat' as float) as lat,
           CAST((text::JSONB)->'lon' as float) as lon
    FROM (SELECT * FROM gtfs);

CREATE MATERIALIZED VIEW ROUTE435 AS
    SELECT *
    FROM all_gtfs
    WHERE label = '435-1662';

CREATE MATERIALIZED VIEW ROUTE444 AS
    SELECT *
    FROM all_gtfs
    WHERE label = '444-1662';

CREATE MATERIALIZED VIEW ROUTEUQSL AS
    SELECT *
    FROM all_gtfs
    WHERE label = 'UQSL-1410';

CREATE MATERIALIZED VIEW ROUTEALL AS
    SELECT *
    FROM all_gtfs;
