create table if not exists "user"
(
    id varchar not null
        constraint user_pk
            primary key,
    username varchar not null,
    password varchar not null,
    coins integer default 20,
    token varchar,
    name varchar,
    bio varchar,
    image varchar
);

alter table "user" owner to basem;

create unique index if not exists user_user_id_uindex
    on "user" (id);

create unique index if not exists user_username_uindex
    on "user" (username);

create unique index if not exists user_token_uindex
    on "user" (token);

create table if not exists package
(
    id varchar not null
        constraint package_pk
            primary key,
    fk_user varchar
        constraint fk_user
            references "user"
            on delete cascade
);

alter table package owner to basem;

create table if not exists card
(
    id varchar not null
        constraint card_pk
            primary key,
    name varchar not null,
    damage double precision not null,
    card_package_id_fk varchar
        constraint card_package_id_fk
            references package
            on delete cascade
);

alter table card owner to basem;

create unique index if not exists card_id_uindex
    on card (id);

create table if not exists deck
(
    id varchar not null
        constraint deck_pk
            primary key,
    fk_card1 varchar
        constraint fk_card1
            references card
            on delete cascade,
    fk_card2 varchar
        constraint fk_card2
            references card
            on delete cascade,
    fk_card3 varchar
        constraint fk_card3
            references card
            on delete cascade,
    fk_card4 varchar
        constraint fk_card4
            references card
            on delete cascade,
    fk_user varchar
        constraint fk_user
            references "user"
            on delete cascade
);

alter table deck owner to basem;

create unique index if not exists deck_fk_card1_uindex
    on deck (fk_card1);

create unique index if not exists deck_fk_card2_uindex
    on deck (fk_card2);

create unique index if not exists deck_fk_card3_uindex
    on deck (fk_card3);

create unique index if not exists deck_fk_card4_uindex
    on deck (fk_card4);

create unique index if not exists deck_id_uindex
    on deck (id);

create unique index if not exists deck_fk_user_uindex
    on deck (fk_user);

create table if not exists stat
(
    total integer default 0,
    draws integer default 0,
    id varchar not null
        constraint stat_pk
            primary key,
    elo integer default 100 not null,
    wins integer default 0,
    defeats integer default 0,
    fk_user varchar not null
        constraint fk_user
            references "user"
            on delete cascade
);

alter table stat owner to basem;

create unique index if not exists stat_fk_user_uindex
    on stat (fk_user);

create unique index if not exists stat_id_uindex
    on stat (id);

create table if not exists battle
(
    id varchar not null
        constraint battle_pk
            primary key,
    fk_player1 varchar
        constraint fk_player1
            references "user"
            on delete cascade,
    fk_player2 varchar
        constraint fk_player2
            references "user"
            on delete cascade,
    fk_winner varchar
        constraint fk_winner
            references "user"
            on delete cascade
);

alter table battle owner to basem;

create unique index if not exists battle_id_uindex
    on battle (id);

create table if not exists round
(
    id varchar not null
        constraint round_pk
            primary key,
    fk_card1 varchar
        constraint fk_card1
            references card
            on delete cascade,
    fk_card2 varchar
        constraint fk_card2
            references card
            on delete cascade,
    fk_winner_card varchar
        constraint fk_winner_card
            references card
            on delete cascade,
    fk_battle varchar
        constraint fk_battle
            references battle
            on delete cascade
);

alter table round owner to basem;

create unique index if not exists round_id_uindex
    on round (id);

create table if not exists trade
(
    id varchar not null
        constraint trade_pk
            primary key,
    fk_card varchar
        constraint fk_card
            references card
            on delete set null,
    type varchar not null,
    "minimumDamage" double precision not null,
    fk_user varchar not null
        constraint fk_user
            references "user"
            on delete cascade
);

alter table trade owner to basem;

create unique index if not exists trade_id_uindex
    on trade (id);

