CREATE TABLE sales_channels (
    id  UUID NOT NULL,
    CONSTRAINT sales_channel_pkey PRIMARY KEY (id)
);

CREATE TABLE info (
    id               UUID    NOT NULL,
    name             text,
    data             jsonb   NOT NULL,
    meta             text[],
    sales_channel_id UUID    NOT NULL,

    CONSTRAINT info_pkey PRIMARY KEY (id),
    CONSTRAINT info_fkey FOREIGN KEY (sales_channel_id)
        REFERENCES sales_channels (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);
