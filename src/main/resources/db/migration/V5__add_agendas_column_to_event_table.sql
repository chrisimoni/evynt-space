ALTER TABLE events
    ADD COLUMN agendas JSONB DEFAULT '[]'::jsonb;