package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.selvbetjening;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class SendtVarselInformasjon {
    @JsonProperty("ytelseType")
    private String ytelseType;
    @JsonProperty("aktørId")
    private AktørId aktørId;
    @JsonProperty("norskIdent")
    private String norskIdent;
    @JsonProperty("saksnummer")
    private String saksnummer;
    @JsonProperty("journalpostId")
    private String journalpostId;
    @JsonProperty("dokumentId")
    private String dokumentId;
    @JsonProperty("dialogId")
    private String dialogId;
    @JsonProperty("hendelse")
    private String hendelse = "TILBAKEKREVING_SPM";
    @JsonProperty("gyldigTil")
    private LocalDate gyldigTil;
    @JsonProperty("opprettet")
    private LocalDateTime opprettet;
    @JsonProperty("aktiv")
    private boolean aktiv = true;

    private SendtVarselInformasjon() {
    }

    public String getYtelseType() {
        return ytelseType;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public String getNorskIdent() {
        return norskIdent;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public String getDialogId() {
        return dialogId;
    }

    public String getHendelse() {
        return hendelse;
    }

    public LocalDate getGyldigTil() {
        return gyldigTil;
    }

    public LocalDateTime getOpprettet() {
        return opprettet;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SendtVarselInformasjon kladd = new SendtVarselInformasjon();

        public Builder medYtelseType(FagsakYtelseType ytelseType) {
            kladd.ytelseType = ytelseType.getKode();
            return this;
        }

        public Builder medAktørId(AktørId aktørId) {
            kladd.aktørId = aktørId;
            return this;
        }

        public Builder medNorskIdent(String norskIdent) {
            kladd.norskIdent = norskIdent;
            return this;
        }

        public Builder medSaksnummer(Saksnummer saksnummer) {
            kladd.saksnummer = saksnummer.getVerdi();
            return this;
        }

        public Builder medJournalpostId(String journalpostId) {
            kladd.journalpostId = journalpostId;
            return this;
        }

        public Builder medDokumentId(String dokumentId) {
            kladd.dokumentId = dokumentId;
            return this;
        }

        public Builder medDialogId(String dialogId) {
            kladd.dialogId = dialogId;
            return this;
        }

        public Builder medGyldigTil(LocalDate gyldigTil) {
            kladd.gyldigTil = gyldigTil;
            return this;
        }

        public Builder medOpprettet(LocalDateTime opprettet) {
            kladd.opprettet = opprettet;
            return this;
        }

        public SendtVarselInformasjon build() {
            Objects.requireNonNull(kladd.ytelseType, "mangler ytelseType");
            Objects.requireNonNull(kladd.aktørId, "mangler aktørId");
            Objects.requireNonNull(kladd.norskIdent, "mangler norskIdent (FNR/DNR)");
            Objects.requireNonNull(kladd.saksnummer, "mangler saksnummer");
            Objects.requireNonNull(kladd.journalpostId, "mangler journalpostId");
            Objects.requireNonNull(kladd.dokumentId, "mangler dokumentId");
            Objects.requireNonNull(kladd.dialogId, "mangler dialogId");
            Objects.requireNonNull(kladd.hendelse, "mangler hendelse");
            Objects.requireNonNull(kladd.gyldigTil, "mangler gyldigTil");
            Objects.requireNonNull(kladd.opprettet, "mangler opprettet");
            return kladd;
        }

    }

}
