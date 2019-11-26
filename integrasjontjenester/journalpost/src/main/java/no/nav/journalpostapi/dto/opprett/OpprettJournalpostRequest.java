package no.nav.journalpostapi.dto.opprett;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.AvsenderMottaker;
import no.nav.journalpostapi.dto.BehandlingTema;
import no.nav.journalpostapi.dto.Bruker;
import no.nav.journalpostapi.dto.Journalposttype;
import no.nav.journalpostapi.dto.Tema;
import no.nav.journalpostapi.dto.sak.Sak;
import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

public class OpprettJournalpostRequest {

    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Journalposttype journalposttype;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private AvsenderMottaker avsenderMottaker;
    private Bruker bruker;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Tema tema;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private BehandlingTema behandlingstema;
    private String tittel;
    private String journalførendeEnhet;
    private String eksternReferanseId;
    private Sak sak;

    private OpprettJournalpostRequest() {
    }

    public static class Builder {

        private OpprettJournalpostRequest kladd = new OpprettJournalpostRequest();

        public Builder medJournalposttype(Journalposttype journalposttype) {
            kladd.journalposttype = journalposttype;
            return this;
        }

        public Builder medAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
            kladd.avsenderMottaker = avsenderMottaker;
            return this;
        }

        public Builder medBruker(Bruker bruker) {
            kladd.bruker = bruker;
            return this;
        }

        public Builder medTema(Tema tema) {
            kladd.tema = tema;
            return this;
        }

        public Builder medBehandlingstema(BehandlingTema behandlingstema) {
            kladd.behandlingstema = behandlingstema;
            return this;
        }

        public Builder medTittel(String tittel) {
            kladd.tittel = tittel;
            return this;
        }

        public Builder medJournalførendeEnhet(String journalførendeEnhet) {
            kladd.journalførendeEnhet = journalførendeEnhet;
            return this;
        }

        public Builder medEksternReferanseId(String eksternReferanseId) {
            kladd.eksternReferanseId = eksternReferanseId;
            return this;
        }

        public Builder medSak(Sak sak) {
            kladd.sak = sak;
            return this;
        }

        public OpprettJournalpostRequest build() {
            Objects.requireNonNull(kladd.journalposttype, "Mangler journalposttype");
            switch (kladd.journalposttype){
                case INNGÅENDE:
                case UTGÅENDE:
                    Objects.requireNonNull(kladd.avsenderMottaker, "Mangler avsender/mottaker");
                    break;
                case NOTAT:
                    break;
            }
            return kladd;
        }
    }
}
