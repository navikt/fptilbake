package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public class HendelseMedUndertype {
    private HendelseType hendelseType;
    private HendelseUnderType hendelseUnderType;

    public HendelseMedUndertype(HendelseType hendelseType, HendelseUnderType hendelseUnderType) {
        this.hendelseType = hendelseType;
        this.hendelseUnderType = hendelseUnderType;
    }

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public HendelseUnderType getHendelseUnderType() {
        return hendelseUnderType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HendelseMedUndertype that = (HendelseMedUndertype) o;
        return Objects.equals(hendelseType, that.hendelseType) &&
                Objects.equals(hendelseUnderType, that.hendelseUnderType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hendelseType, hendelseUnderType);
    }

    @Override
    public String toString() {
        return "HendelseMedUndertype{" +
                "type=" + hendelseType.getKode() +
                ", underType=" + hendelseUnderType.getKode() +
                '}';
    }
}
