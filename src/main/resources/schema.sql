create table if not exists posts
(
    post_id     bigserial primary key,
    title       varchar(256)  not null,
    text        varchar(4000) not null,
    likes_count integer       not null default 0,
    image_uuid  varchar(36),
    deleted     boolean       not null default false
);

create table if not exists post_tag
(
    post_id bigint references posts (post_id),
    tag     varchar(256),

    primary key (post_id, tag)
);

create table if not exists comments
(
    comment_id bigserial primary key,
    post_id    bigint       not null references posts (post_id),
    text       varchar(512) not null,
    deleted    boolean      not null default false
);