ALTER TABLE foreldelse_periode ADD foreldelsesfrist DATE;
ALTER TABLE foreldelse_periode ADD oppdagelses_dato DATE;

COMMENT ON COLUMN foreldelse_periode.foreldelsesfrist IS 'Foreldelsesfrist for når feilutbetalingen kan innkreves';
COMMENT ON COLUMN foreldelse_periode.oppdagelses_dato IS 'Dato for når feilutbetalingen ble oppdaget';
