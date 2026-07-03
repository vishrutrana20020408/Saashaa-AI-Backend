-- Guarded drop script for duplicate lowercase Supabase tables
-- Run this only after the merge migration has been applied and verified.
-- This script checks for:
--   1) duplicate tables being empty
--   2) no foreign key constraints referencing them
-- If any check fails, the script raises an exception and aborts.

BEGIN;

RAISE NOTICE 'Verifying duplicate lowercase tables are empty and unreferenced...';

-- 1) Ensure lowercase duplicate tables are empty
DO $$
BEGIN
    IF EXISTS(SELECT 1 FROM public.users) THEN
        RAISE EXCEPTION 'Cannot drop public.users: table is not empty';
    END IF;
    IF EXISTS(SELECT 1 FROM public.admin) THEN
        RAISE EXCEPTION 'Cannot drop public.admin: table is not empty';
    END IF;
    IF EXISTS(SELECT 1 FROM public.owners) THEN
        RAISE EXCEPTION 'Cannot drop public.owners: table is not empty';
    END IF;
    IF EXISTS(SELECT 1 FROM public.notification) THEN
        RAISE EXCEPTION 'Cannot drop public.notification: table is not empty';
    END IF;
    IF EXISTS(SELECT 1 FROM public.company) THEN
        RAISE EXCEPTION 'Cannot drop public.company: table is not empty';
    END IF;
END$$;

-- 2) Ensure no foreign key constraints reference the duplicate tables
DO $$
DECLARE
    ref_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO ref_count
    FROM pg_constraint c
    JOIN pg_class cl ON c.conrelid = cl.oid
    JOIN pg_class clto ON c.confrelid = clto.oid
    WHERE c.contype = 'f'
      AND clto.relname IN ('users', 'admin', 'owners', 'notification', 'company')
      AND cl.relnamespace = 'public'::regnamespace;

    IF ref_count > 0 THEN
        RAISE EXCEPTION 'Cannot drop duplicate tables: % foreign key references still exist', ref_count;
    END IF;
END$$;

RAISE NOTICE 'Dropping duplicate lowercase tables...';

DROP TABLE IF EXISTS public.users CASCADE;
DROP TABLE IF EXISTS public.admin CASCADE;
DROP TABLE IF EXISTS public.owners CASCADE;
DROP TABLE IF EXISTS public.notification CASCADE;
DROP TABLE IF EXISTS public.company CASCADE;

RAISE NOTICE 'Duplicate lowercase tables dropped successfully.';

COMMIT;
