ALTER TABLE OKO_XML_MOTTATT ADD SAKSNUMMER VARCHAR2(50 CHAR);

comment on column OKO_XML_MOTTATT.SAKSNUMMER is 'saksnummer(som økonomi har sendt)';
