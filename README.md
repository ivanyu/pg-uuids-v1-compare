# pg-uuids-v1-compare
PostgreSQL function to compare version 1 (time-based) UUIDs based on timestamps, and tests for it.

**Example:**

```
SELECT uuids_v1_compare(uuid('762ca430-eed5-11e5-8ca8-4bebdf3c407a'),
                        uuid('762cf250-eed5-11e5-8ca8-4bebdf3c407a'))

uuids_v1_compare
smallint
----------------
-1
```

[A blog post](https://ivanyu.me/blog/2016/03/28/time-based-version-1-uuids-ordering-in-postgresql/) about it.
