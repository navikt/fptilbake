package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.saksbehandler.dto;

import java.time.LocalDateTime;

public class InnloggetNavAnsattDto {

    private String brukernavn;
    private String navn;
    private boolean kanSaksbehandle;
    private boolean kanVeilede;
    private boolean kanBeslutte;
    private boolean kanOverstyre;
    private boolean kanBehandleKodeEgenAnsatt;
    private boolean kanBehandleKode6;
    private boolean kanBehandleKode7;
    private LocalDateTime funksjonellTid;

    private InnloggetNavAnsattDto() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBrukernavn() {
        return brukernavn;
    }

    public String getNavn() {
        return navn;
    }

    public boolean getKanSaksbehandle() {
        return kanSaksbehandle;
    }

    public boolean getKanVeilede() {
        return kanVeilede;
    }

    public boolean getKanBeslutte() {
        return kanBeslutte;
    }

    public boolean getKanOverstyre() {
        return kanOverstyre;
    }

    public boolean getKanBehandleKodeEgenAnsatt() {
        return kanBehandleKodeEgenAnsatt;
    }

    public boolean getKanBehandleKode6() {
        return kanBehandleKode6;
    }

    public boolean getKanBehandleKode7() {
        return kanBehandleKode7;
    }

    public LocalDateTime getFunksjonellTid() {
        return funksjonellTid;
    }

    public static class Builder {
        private InnloggetNavAnsattDto kladd = new InnloggetNavAnsattDto();


        public Builder setBrukernavn(String brukernavn) {
            this.kladd.brukernavn = brukernavn;
            return this;
        }

        public Builder setNavn(String navn) {
            this.kladd.navn = navn;
            return this;
        }

        public Builder setKanSaksbehandle(Boolean kanSaksbehandle) {
            this.kladd.kanSaksbehandle = kanSaksbehandle;
            return this;
        }

        public Builder setKanVeilede(Boolean kanVeilede) {
            this.kladd.kanVeilede = kanVeilede;
            return this;
        }

        public Builder setKanBeslutte(Boolean kanBeslutte) {
            this.kladd.kanBeslutte = kanBeslutte;
            return this;
        }

        public Builder setKanOverstyre(Boolean kanOverstyre) {
            this.kladd.kanOverstyre = kanOverstyre;
            return this;
        }

        public Builder setKanBehandleKodeEgenAnsatt(Boolean kanBehandleKodeEgenAnsatt) {
            this.kladd.kanBehandleKodeEgenAnsatt = kanBehandleKodeEgenAnsatt;
            return this;
        }

        public Builder setKanBehandleKode6(Boolean kanBehandleKode6) {
            this.kladd.kanBehandleKode6 = kanBehandleKode6;
            return this;
        }

        public Builder setKanBehandleKode7(Boolean kanBehandleKode7) {
            this.kladd.kanBehandleKode7 = kanBehandleKode7;
            return this;
        }

        public InnloggetNavAnsattDto create() {
            this.kladd.funksjonellTid = LocalDateTime.now();
            return kladd;
        }
    }
}
