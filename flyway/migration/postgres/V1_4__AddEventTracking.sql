create table event_tracking(
  id            uuid                      not null        primary key,
  created_at    timestamp with time zone  not null,
  updated_at    timestamp with time zone,
  sales_channel_id     uuid                      not null,
  group_id      uuid,
  status        text                      not null,
  result        text,
  problems      jsonb,

  CONSTRAINT event_tracking_sc_fkey FOREIGN KEY (sales_channel_id)
        REFERENCES sales_channels (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);

create index on event_tracking(sales_channel_id, group_id);