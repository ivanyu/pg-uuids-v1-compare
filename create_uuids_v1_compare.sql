-- Compare two version 1 UUIDs by timestamps and counters.
-- Returns 1 IF u1 > u2; -1 IF u1 < u2; 0 IF u1 = u2.
CREATE OR REPLACE FUNCTION uuids_v1_compare(u1 uuid, u2 uuid)
    RETURNS SMALLINT AS $$
DECLARE
    u1_bytes BYTEA := decode(REPLACE(u1::TEXT, '-', ''), 'hex');
    u2_bytes BYTEA := decode(REPLACE(u2::TEXT, '-', ''), 'hex');
    version1 INTEGER := get_byte(u1_bytes, 6) >> 4;
    version2 INTEGER := get_byte(u2_bytes, 6) >> 4;
    time_hi1 INTEGER;
    time_hi2 INTEGER;
    time_mid1 INTEGER;
    time_mid2 INTEGER;
    time_low1 BIGINT;
    time_low2 BIGINT;
BEGIN
    -- Version must be 1.
    IF version1 <> 1 THEN
        RAISE EXCEPTION 'u1 version is %. Only version 1 must be passed.',
            version1;
    END IF;
    IF version2 <> 1 THEN
        RAISE EXCEPTION 'u2 version is %. Only version 1 must be passed.',
            version2;
    END IF;

    -- Compare time hi.
    -- 15 = 0xF = 1111
    -- The highest 4 bits is for version.
    time_hi1 = (get_byte(u1_bytes, 6) & 15) << 8 | get_byte(u1_bytes, 7);
    time_hi2 = (get_byte(u2_bytes, 6) & 15) << 8 | get_byte(u2_bytes, 7);


    IF time_hi1 > time_hi2 THEN
        RETURN 1;
    END IF;
    IF time_hi1 < time_hi2 THEN
        RETURN -1;
    END IF;

    -- Compare time mid
    time_mid1 = get_byte(u1_bytes, 4) << 8 | get_byte(u1_bytes, 5);
    time_mid2 = get_byte(u2_bytes, 4) << 8 | get_byte(u2_bytes, 5);

    IF time_mid1 > time_mid2 THEN
        RETURN 1;
    END IF;
    IF time_mid1 < time_mid2 THEN
        RETURN -1;
    END IF;

    -- Compare time low
    time_low1 = get_byte(u1_bytes, 0)::bigint << 24 |
                get_byte(u1_bytes, 1)::bigint << 16 |
                get_byte(u1_bytes, 2)::bigint <<  8 |
                get_byte(u1_bytes, 3)::bigint <<  0;
    time_low2 = get_byte(u2_bytes, 0)::bigint << 24 |
                get_byte(u2_bytes, 1)::bigint << 16 |
                get_byte(u2_bytes, 2)::bigint <<  8 |
                get_byte(u2_bytes, 3)::bigint <<  0;

    IF time_low1 > time_low2 THEN
        RETURN 1;
    END IF;
    IF time_low1 < time_low2 THEN
        RETURN -1;
    END IF;

    RETURN 0;
END;
$$ LANGUAGE plpgsql;
