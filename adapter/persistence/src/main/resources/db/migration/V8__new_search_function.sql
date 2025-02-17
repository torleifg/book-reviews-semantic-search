drop function if exists search_books(text, int);

create function search_books(search_query text, result_limit int)
    returns table
            (
                external_id     text,
                vector_store_id uuid,
                deleted         boolean,
                metadata        jsonb,
                rank            real
            ) as
$$
begin
return query
select b.external_id,
       b.vector_store_id,
       b.deleted,
       b.metadata,
       ts_rank(
               setweight(to_tsvector('simple', coalesce(b.metadata ->> 'title', '')), 'B') ||
               setweight(to_tsvector('simple', coalesce(b.metadata ->> 'description', '')), 'D') ||
               setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'contributors', 'name'), '')),
                         'A') ||
               setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'about', 'term'), '')),
                         'C') ||
               setweight(to_tsvector('simple', coalesce(jsonb_array_to_text(b.metadata -> 'genreAndForm', 'term'), '')),
                         'C'),
               plainto_tsquery('simple', search_query)
       ) as rank
from book b
where to_tsvector('simple',
                  coalesce(b.metadata ->> 'title', '') || ' ' ||
                  coalesce(b.metadata ->> 'description', '') || ' ' ||
                  coalesce(jsonb_array_to_text(b.metadata -> 'contributors', 'name'), '') || ' ' ||
                  coalesce(jsonb_array_to_text(b.metadata -> 'about', 'term'), '') || ' ' ||
                  coalesce(jsonb_array_to_text(b.metadata -> 'genreAndForm', 'term'), ''))
                  @@ plainto_tsquery('simple', search_query)
order by rank desc
    limit result_limit;
end;
$$
language plpgsql;
