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

CREATE MATERIALIZED VIEW ALLVID AS
    SELECT r.vid, max(lat) as furthestnorth, min(lat) as furthestsouth, max(lon) as furthesteast, min(lon) as furthestwest, count(*) as count
    FROM ROUTEALL r
    GROUP BY r.vid;

CREATE MATERIALIZED VIEW NORTH AS
    SELECT a.vid, max(a.furthestnorth) as furthestnorth
    FROM ALLVID a
    GROUP BY a.vid
    ORDER BY furthestnorth DESC
    LIMIT 1;

CREATE MATERIALIZED VIEW NORTHLABEL AS
    SELECT DISTINCT n.vid, n.furthestnorth, r.label
    FROM NORTH n, ROUTEALL r
    WHERE n.vid = r.vid;

CREATE MATERIALIZED VIEW SOUTH AS
    SELECT a.vid, min(a.furthestsouth) as furthestsouth
    FROM ALLVID a
    GROUP BY a.vid
    ORDER BY furthestsouth ASC
    LIMIT 1;

CREATE MATERIALIZED VIEW SOUTHLABEL AS
    SELECT DISTINCT s.vid, s.furthestsouth, r.label
    FROM SOUTH s, ROUTEALL r
    WHERE s.vid = r.vid;
