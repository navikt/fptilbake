package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.fordelling;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class AbacJournalpostMottakDto extends JournalpostMottakDto implements AbacDto {

    public AbacJournalpostMottakDto() {
        super();
    }

    public AbacJournalpostMottakDto(String saksnummer, String journalpostId, String behandlingstemaOffisiellKode, String dokumentTypeIdOffisiellKode,
                                    LocalDateTime forsendelseMottattTidspunkt, String payloadXml) {
        super(saksnummer, journalpostId, behandlingstemaOffisiellKode, dokumentTypeIdOffisiellKode, forsendelseMottattTidspunkt, payloadXml);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, getSaksnummer());
    }

}
