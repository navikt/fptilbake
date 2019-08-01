package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "BehandlingType")
@DiscriminatorValue(BehandlingType.DISCRIMINATOR)
public class BehandlingType extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_TYPE";

    /**
     * Konstanter for å skrive ned kodeverdi. For å hente ut andre data konfigurert, må disse leses fra databasen (eks.
     * for å hente offisiell kode for et Nav kodeverk).
     */
    public static final BehandlingType TILBAKEKREVING = new BehandlingType("BT-007"); //$NON-NLS-1$
    public static final BehandlingType REVURDERING_TILBAKEKREVING = new BehandlingType("BT-008"); //$NON-NLS-1$
    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden gjør samme nytten.
     */
    public static final BehandlingType UDEFINERT = new BehandlingType("-"); //$NON-NLS-1$

    private static final Map<String, BehandlingType> TILGJENGELIGE = Map.of(
        REVURDERING_TILBAKEKREVING.getKode(), REVURDERING_TILBAKEKREVING,
        TILBAKEKREVING.getKode(), TILBAKEKREVING
    );

    @Transient
    private Integer behandlingstidFristUker;
    @Transient
    private Boolean behandlingstidVarselbrev;

    protected BehandlingType() {
        // Hibernate trenger den
    }

    protected BehandlingType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public int getBehandlingstidFristUker() {
        if (behandlingstidFristUker == null) {
            String behandlingstidFristUkerStr = getJsonField("behandlingstidFristUker");
            behandlingstidFristUker = Integer.parseInt(behandlingstidFristUkerStr);
        }
        return behandlingstidFristUker;
    }

    public boolean isBehandlingstidVarselbrev() {
        if (behandlingstidVarselbrev == null) {
            behandlingstidVarselbrev = false;
            String behandlingstidVarselbrevStr = getJsonField("behandlingstidVarselbrev");
            if (behandlingstidVarselbrevStr != null) {
                this.behandlingstidVarselbrev = new BooleanToStringConverter().convertToEntityAttribute(behandlingstidVarselbrevStr);
            }
        }
        return behandlingstidVarselbrev;
    }

    public static BehandlingType fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        throw BehandlingTypeFeil.FEILFACTORY.ugyldigBehandlingType(kode).toException();
    }

    interface BehandlingTypeFeil extends DeklarerteFeil {

        BehandlingTypeFeil FEILFACTORY = FeilFactory.create(BehandlingTypeFeil.class);

        @TekniskFeil(feilkode = "FPT-312906", feilmelding = "BehandlingType '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigBehandlingType(String behandlingType);
    }
}
