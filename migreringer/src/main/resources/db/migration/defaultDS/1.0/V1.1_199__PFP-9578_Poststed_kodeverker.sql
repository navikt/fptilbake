create table poststed (
    poststednummer varchar2(16 char) not null
        constraint pk_poststed
            primary key,
    poststednavn varchar2(256 char) not null,
    gyldigfom date not null,
    gyldigtom date DEFAULT to_date('31.12.9999', 'dd.mm.yyyy') not null,
    opprettet_av varchar2(20 char) default 'VL' not null,
    opprettet_tid timestamp(3) default LOCALTIMESTAMP not null,
    endret_av varchar2(20 char),
    endret_tid timestamp(3)
);

comment on table poststed is 'Tabell for sentralt kodeverk Postnummer';
comment on column poststed.poststednummer is 'Postnummer';
comment on column poststed.poststednavn is 'Poststed';
comment on column poststed.gyldigFom is 'Gyldig fra dato';
comment on column poststed.gyldigTom is 'Gyldig til dato';
