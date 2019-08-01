ALTER TABLE KRAV_GRUNNLAG_431 add (
  EKSTERN_KRAVGRUNNLAG_ID varchar2(9 char)
);

comment on column KRAV_GRUNNLAG_431.EKSTERN_KRAVGRUNNLAG_ID is 'Referanse til kravgrunnlagID fra OSTBK. Brukes ved omgjøring for å hente nytt grunnlag.';
