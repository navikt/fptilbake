package no.nav.journalpostapi.dto.opprett;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.journalpostapi.dto.AvsenderMottaker;
import no.nav.journalpostapi.dto.BehandlingTema;
import no.nav.journalpostapi.dto.Bruker;
import no.nav.journalpostapi.dto.Journalposttype;
import no.nav.journalpostapi.dto.Tema;
import no.nav.journalpostapi.dto.dokument.Dokument;
import no.nav.journalpostapi.dto.sak.Sak;
import no.nav.journalpostapi.dto.serializer.KodelisteSomKodeSerialiserer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpprettJournalpostRequest {

    private AvsenderMottaker avsenderMottaker;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private BehandlingTema behandlingstema;
    private Bruker bruker;
    private List<Dokument> dokumenter;
    private String eksternReferanseId;
    @JsonProperty("journalfoerendeEnhet")
    private Integer journalførendeEnhet;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Journalposttype journalposttype;
    private Sak sak;
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private Tema tema;
    private String tittel;

    private OpprettJournalpostRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private OpprettJournalpostRequest kladd = new OpprettJournalpostRequest();
        private Dokument hoveddokument;
        private List<Dokument> vedlegg = new ArrayList<>();

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
            kladd.journalførendeEnhet = Integer.valueOf(journalførendeEnhet);
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

        public Builder medHoveddokument(Dokument dokument) {
            hoveddokument = dokument;
            return this;
        }

        public Builder leggTilVedlegg(Dokument dokument) {
            vedlegg.add(dokument);
            return this;
        }

        public OpprettJournalpostRequest build() {
            Objects.requireNonNull(hoveddokument, "Mangler hoveddokument");
            Objects.requireNonNull(kladd.journalposttype, "Mangler journalposttype");
            switch (kladd.journalposttype) {
                case INNGÅENDE:
                case UTGÅENDE:
                    Objects.requireNonNull(kladd.avsenderMottaker, "Mangler avsender/mottaker");
                    break;
                case NOTAT:
                    break;
            }
            kladd.dokumenter = new ArrayList<>();
            kladd.dokumenter.add(hoveddokument); //må være først i listen
            kladd.dokumenter.addAll(vedlegg);
            return kladd;
        }
    }
}
