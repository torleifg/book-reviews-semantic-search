create extension if not exists vector;
create extension if not exists hstore;
create extension if not exists "uuid-ossp";

create table if not exists vector_store
(
    id        uuid default uuid_generate_v4() primary key,
    content   text,
    metadata  json,
    embedding vector(384)
);
