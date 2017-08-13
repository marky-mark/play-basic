ALTER TABLE info ADD last_modified timestamp with time zone NOT NULL DEFAULT NOW();

ALTER TABLE sales_channels ADD name text;
INSERT INTO sales_channels VALUES ('75506ce9-ece6-4835-bbb1-83613c326be7');