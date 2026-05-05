DO $$
DECLARE
    seq_record RECORD;
    table_name text;
BEGIN
    FOR seq_record IN SELECT c.relname FROM pg_class c WHERE c.relkind = 'S' AND c.relname LIKE '%_id_seq' LOOP
        table_name := substring(seq_record.relname from 1 for length(seq_record.relname)-7);
        EXECUTE format('SELECT setval(%L, COALESCE((SELECT MAX(id) FROM %I), 1))', seq_record.relname, table_name);
        RAISE NOTICE 'Updated sequence % for table %', seq_record.relname, table_name;
    END LOOP;
END;
$$;
