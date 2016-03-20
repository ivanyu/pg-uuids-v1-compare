# pg_uuids_v1_compare
PostgreSQL function to compare version 1 UUIDs based on timestamps, and tests for it.

**Example:**

`SELECT uuids_v1_compare(uuid('762ca430-eed5-11e5-8ca8-4bebdf3c407a'), uuid('762cf250-eed5-11e5-8ca8-4bebdf3c407a'))`
```uuids_v1_compare
smallint
----------------
-1
```
