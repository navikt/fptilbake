package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.SaksbehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.BehandlingInfo;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

// mapping for BehandlingInfo klassen
@SqlResultSetMapping(name = "PipBehandlingInfo", classes = {
        @ConstructorResult(targetClass = BehandlingInfo.class, columns = {
                @ColumnResult(name = "behandlingId", type = Long.class),
                @ColumnResult(name = "saksnummer"),
                @ColumnResult(name = "aktørId"),
                @ColumnResult(name = "behandlingstatus"),
                @ColumnResult(name = "ansvarligSaksbehandler")
        })
})

@Entity(name = "Behandling")
@Table(name = "BEHANDLING")
public class Behandling extends BaseEntitet {

    private static final Comparator<BaseEntitet> COMPARATOR_OPPRETTET_TID = Comparator
            .comparing(BaseEntitet::getOpprettetTidspunkt, (a, b) -> {
                if (a != null && b != null) {
                    return a.compareTo(b);
                } else if (a == null && b == null) {
                    return 0;
                } else {
                    return a == null ? -1 : 1;
                }
            });
    private static final Comparator<BaseEntitet> COMPARE_ENDRET_TID = Comparator
            .comparing(BaseEntitet::getEndretTidspunkt, (a, b) -> {
                if (a != null && b != null) {
                    return a.compareTo(b);
                } else if (a == null && b == null) {
                    return 0;
                } else {
                    return a == null ? -1 : 1;
                }
            });

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING")
    private Long id;

    @NaturalId
    @Column(name = "uuid")
    private UUID uuid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fagsak_id", nullable = false, updatable = false)
    private Fagsak fagsak;

    @Convert(converter = BehandlingStatus.KodeverdiConverter.class)
    @Column(name = "behandling_status", nullable = false)
    private BehandlingStatus status = BehandlingStatus.OPPRETTET;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "behandling")
    private List<BehandlingStegTilstand> behandlingStegTilstander = new ArrayList<>(1);

    @Convert(converter = BehandlingType.KodeverdiConverter.class)
    @Column(name = "behandling_type", nullable = false)
    private BehandlingType behandlingType = BehandlingType.UDEFINERT;

    // CascadeType.ALL + orphanRemoval=true må til for at aksjonspunkter skal bli slettet fra databasen ved fjerning fra HashSet
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "behandling", orphanRemoval = true, cascade = CascadeType.ALL, targetEntity = Aksjonspunkt.class)
    private Set<Aksjonspunkt> aksjonspunkter = new HashSet<>();

    /**
     * Er egentlig OneToOne, men må mappes slik da JPA/Hibernate ikke støtter OneToOne på annet enn shared PK.
     */
    @OneToMany(mappedBy = "behandling", targetEntity = BehandlingÅrsak.class)
    private Set<BehandlingÅrsak> behandlingÅrsaker = new HashSet<>(1);

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    // Har sysdate som default value
    @Column(name = "opprettet_dato")
    private LocalDateTime opprettetDato;

    @Column(name = "avsluttet_dato")
    private LocalDateTime avsluttetDato;

    @Column(name = "ansvarlig_saksbehandler")
    private String ansvarligSaksbehandler;

    @Column(name = "ansvarlig_beslutter")
    private String ansvarligBeslutter;

    @Column(name = "behandlende_enhet")
    private String behandlendeEnhetId;

    @Column(name = "behandlende_enhet_navn")
    private String behandlendeEnhetNavn;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "MANUELT_OPPRETTET", nullable = false)
    private boolean manueltOpprettet = false;

    @Convert(converter = SaksbehandlingType.verdiConverter.class)
    @Column(name = "SAKSBEHANDLING_TYPE", nullable = false)
    private SaksbehandlingType saksbehandlingType = SaksbehandlingType.ORDINÆR;

    Behandling() {
        // Hibernate
    }

    private Behandling(Fagsak fagsak, BehandlingType type) {
        Objects.requireNonNull(fagsak, "Behandling må tilknyttes parent Fagsak"); //$NON-NLS-1$
        this.fagsak = fagsak;
        if (type != null) {
            this.behandlingType = type;
        }

        this.uuid = UUID.randomUUID();
    }

    /**
     * Skal kun brukes av BehandlingskontrollTjeneste for prod kode slik at events fyres.
     */
    public static Builder nyBehandlingFor(Fagsak fagsak, BehandlingType behandlingType) {
        return new Builder(fagsak, behandlingType);
    }

    /**
     * Skal kun brukes av BehandlingskontrollTjeneste for prod kode slik at events fyres.
     * <p>
     * Denne oppretter en Builder for å bygge en {@link Behandling} basert på et eksisterende behandling.
     * <p>
     * Ved Endringssøknad eller REVURD_OPPR er det normalt DENNE som skal brukes.
     * <p>
     * NB! FOR TESTER - FORTREKK (ScenarioMorSøkerEngangsstønad) eller (ScenarioFarSøkerEngangsstønad). De forenkler
     * test oppsett basert på vanlige defaults.
     */
    public static Behandling.Builder fraTidligereBehandling(Behandling forrigeBehandling, BehandlingType behandlingType) {
        return new Builder(forrigeBehandling, behandlingType);
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Long getFagsakId() {
        return getFagsak().getId();
    }

    public BehandlingStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Oppdater behandlingssteg og tilhørende status.
     * <p>
     * NB::NB::NB Dette skal normalt kun gjøres fra Behandlingskontroll slik at
     * bokføring og events blir riktig. Er ikke en del av offentlig API.
     *
     * @param stegTilstand - tilstand for steg behandlingen er i
     */
    void oppdaterBehandlingStegOgStatus(BehandlingStegTilstand stegTilstand) {
        Objects.requireNonNull(stegTilstand, "behandlingStegTilstand"); //$NON-NLS-1$

        // legg til ny
        this.behandlingStegTilstander.add(stegTilstand);
        var behandlingSteg = stegTilstand.getBehandlingSteg();
        this.status = behandlingSteg.getDefinertBehandlingStatus();
    }


    /**
     * Marker behandling som avsluttet.
     */
    public void avsluttBehandling() {
        lukkBehandlingStegStatuser(this.behandlingStegTilstander, BehandlingStegStatus.UTFØRT);
        this.status = BehandlingStatus.AVSLUTTET;
        this.avsluttetDato = LocalDateTime.now();
    }

    private void lukkBehandlingStegStatuser(Collection<BehandlingStegTilstand> stegTilstander, BehandlingStegStatus sluttStatusForSteg) {
        stegTilstander.stream()
                .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getBehandlingStegStatus()))
                .forEach(t -> t.setBehandlingStegStatus(sluttStatusForSteg));
    }

    public BehandlingType getType() {
        return behandlingType;
    }

    public Optional<BehandlingStegTilstand> getBehandlingStegTilstand() {
        List<BehandlingStegTilstand> tilstander = behandlingStegTilstander.stream()
                .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getBehandlingStegStatus()))
                .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException("Utvikler-feil: Kan ikke ha flere steg samtidig åpne: " + tilstander); //$NON-NLS-1$
        }

        return tilstander.isEmpty() ? Optional.empty() : Optional.of(tilstander.get(0));
    }

    public Optional<BehandlingStegTilstand> getSisteBehandlingStegTilstand() {
        // sjekk "ikke-sluttstatuser" først
        Optional<BehandlingStegTilstand> sisteAktive = getBehandlingStegTilstand();

        if (sisteAktive.isPresent()) {
            return sisteAktive;
        }

        Comparator<BehandlingStegTilstand> comparatorOpprettet = compareOpprettetTid();
        Comparator<BehandlingStegTilstand> comparatorEndret = compareEndretTid();
        Comparator<BehandlingStegTilstand> comparator = comparatorOpprettet.reversed().thenComparing(Comparator.nullsLast(comparatorEndret).reversed());

        // tar nyeste.
        return behandlingStegTilstander.stream().sorted(comparator).findFirst();
    }

    public Optional<BehandlingStegTilstand> getBehandlingStegTilstand(BehandlingStegType stegType) {
        List<BehandlingStegTilstand> tilstander = behandlingStegTilstander.stream()
                .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getBehandlingStegStatus())
                        && Objects.equals(stegType, t.getBehandlingSteg()))
                .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException(
                    "Utvikler-feil: Kan ikke ha flere steg samtidig åpne for stegType[" + stegType + "]: " + tilstander); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return tilstander.isEmpty() ? Optional.empty() : Optional.of(tilstander.get(0));
    }


    public BehandlingStegType getAktivtBehandlingSteg() {
        BehandlingStegTilstand stegTilstand = getBehandlingStegTilstand().orElse(null);
        return stegTilstand == null ? null : stegTilstand.getBehandlingSteg();
    }

    /**
     * @deprecated - fjernes når alle behandlinger har UUID og denne er satt NOT NULL i db. Inntil da sikrer denne lagring av UUID
     */
    @Deprecated
    @PreUpdate
    protected void onUpdateMigrerUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Behandling)) {
            return false;
        }
        Behandling other = (Behandling) object;
        return Objects.equals(getFagsak(), other.getFagsak())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getOpprettetTidspunkt(), other.getOpprettetTidspunkt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFagsak(), getType(), getOpprettetTidspunkt());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" //$NON-NLS-1$
                + (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "fagsak=" + fagsak + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "status=" + status + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "type=" + behandlingType + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "steg=" + (getBehandlingStegTilstand().orElse(null)) + "," //$NON-NLS-1$ //$NON-NLS-2$
                + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public Fagsak getFagsak() {
        return fagsak;
    }

    /**
     * Internt API, IKKE BRUK.
     */
    void addAksjonspunkt(Aksjonspunkt aksjonspunkt) {
        aksjonspunkter.add(aksjonspunkt);
    }

    /**
     * Internt API, IKKE BRUK.
     */
    void fjernAksjonspunkt(Aksjonspunkt aksjonspunkt) {
        Set<Aksjonspunkt> beholdes = aksjonspunkter.stream()
                .filter(ap -> !ap.getAksjonspunktDefinisjon().getKode()
                        .equals(aksjonspunkt.getAksjonspunktDefinisjon().getKode()))
                .collect(toSet());
        aksjonspunkter.clear();
        aksjonspunkter.addAll(beholdes);
    }

    public Set<Aksjonspunkt> getAksjonspunkter() {
        return getAksjonspunkterStream()
                .collect(toSet());
    }

    public Optional<Aksjonspunkt> getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon definisjon) {
        return getAksjonspunkterStream()
                .filter(a -> a.getAksjonspunktDefinisjon().equals(definisjon))
                .findFirst();
    }

    public Aksjonspunkt getAksjonspunktFor(AksjonspunktDefinisjon definisjon) {
        return getAksjonspunkterStream()
                .filter(a -> a.getAksjonspunktDefinisjon().equals(definisjon))
                .findFirst()
                .orElseThrow(() -> new TekniskException("FPT-473718", String.format("Behandling har ikke aksjonspunkt for definisjon [%s].", definisjon.getKode())));
    }

    public List<Aksjonspunkt> getÅpneAksjonspunkter() {
        return getÅpneAksjonspunkterStream()
                .collect(Collectors.toList());
    }

    public Set<Aksjonspunkt> getAlleAksjonspunkterInklInaktive() {
        return aksjonspunkter;
    }

    public List<Aksjonspunkt> getÅpneAksjonspunkter(AksjonspunktType aksjonspunktType) {
        return getÅpneAksjonspunkterStream()
                .filter(ad -> aksjonspunktType.equals(ad.getAksjonspunktDefinisjon().getAksjonspunktType()))
                .collect(Collectors.toList());
    }

    public List<Aksjonspunkt> getÅpneAksjonspunkter(Collection<AksjonspunktDefinisjon> matchKriterier) {
        return getÅpneAksjonspunkterStream()
                .filter(a -> matchKriterier.contains(a.getAksjonspunktDefinisjon()))
                .collect(Collectors.toList());
    }

    public List<Aksjonspunkt> getAksjonspunkterMedTotrinnskontroll() {
        return getAksjonspunkterStream()
                .filter(a -> !a.erAvbrutt() && a.isToTrinnsBehandling())
                .collect(Collectors.toList());
    }

    public LocalDate getFristDatoBehandlingPåVent() {
        return getFørsteÅpneAutopunkt()
            .filter(ap -> ap.getFristTid() != null)
            .map(Aksjonspunkt::getFristTid)
            .map(LocalDateTime::toLocalDate).orElse(null);
    }

    public AksjonspunktDefinisjon getBehandlingPåVentAksjonspunktDefinisjon() {
        return getFørsteÅpneAutopunkt().map(Aksjonspunkt::getAksjonspunktDefinisjon).orElse(null);
    }

    public Venteårsak getVenteårsak() {
        return getFørsteÅpneAutopunkt().map(Aksjonspunkt::getVenteårsak).orElse(null);
    }

    private Optional<Aksjonspunkt> getFørsteÅpneAutopunkt() {
        return getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).stream()
                .findFirst();
    }

    public boolean isBehandlingPåVent() {
        return !getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).isEmpty();
    }


    private Stream<Aksjonspunkt> getAksjonspunkterStream() {
        return aksjonspunkter.stream();
    }

    private Stream<Aksjonspunkt> getÅpneAksjonspunkterStream() {
        return getAksjonspunkterStream()
                .filter(Aksjonspunkt::erÅpentAksjonspunkt);
    }


    public BehandlingStegStatus getBehandlingStegStatus() {
        BehandlingStegTilstand stegTilstand = getBehandlingStegTilstand().orElse(null);
        return stegTilstand == null ? null : stegTilstand.getBehandlingStegStatus();
    }


    public boolean erSaksbehandlingAvsluttet() {
        return erAvsluttet() || erUnderIverksettelse();
    }

    public boolean erOrdinærSaksbehandlingAvsluttet() {
        return erAvsluttet() || erUnderIverksettelse() || erTilBeslutter();
    }

    public boolean erTilBeslutter() {
        return Objects.equals(BehandlingStatus.FATTER_VEDTAK, getStatus());
    }

    public boolean erUnderIverksettelse() {
        return Objects.equals(BehandlingStatus.IVERKSETTER_VEDTAK, getStatus());
    }

    public boolean erAvsluttet() {
        return Objects.equals(BehandlingStatus.AVSLUTTET, getStatus());
    }

    public AktørId getAktørId() {
        return getFagsak().getNavBruker().getAktørId();
    }

    public Stream<BehandlingStegTilstand> getBehandlingStegTilstandHistorikk() {
        return behandlingStegTilstander.stream().sorted(COMPARATOR_OPPRETTET_TID);
    }

    public Long getVersjon() {
        return versjon;
    }

    public LocalDateTime getOpprettetDato() {
        return opprettetDato;
    }

    public LocalDateTime getAvsluttetDato() {
        return avsluttetDato;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public String getAnsvarligBeslutter() {
        return ansvarligBeslutter;
    }

    public String getBehandlendeEnhetId() {
        return behandlendeEnhetId;
    }

    public void setBehandlendeEnhetId(String behandlendeEnhetId) {
        this.behandlendeEnhetId = behandlendeEnhetId;
    }

    public String getBehandlendeEnhetNavn() {
        return behandlendeEnhetNavn;
    }

    public void setBehandlendeEnhetNavn(String behandlendeEnhetNavn) {
        this.behandlendeEnhetNavn = behandlendeEnhetNavn;
    }

    public OrganisasjonsEnhet getBehandlendeOrganisasjonsEnhet() {
        return new OrganisasjonsEnhet(behandlendeEnhetId, behandlendeEnhetNavn);
    }

    public void setBehandlendeOrganisasjonsEnhet(OrganisasjonsEnhet enhet) {
        this.behandlendeEnhetId = enhet.getEnhetId();
        this.behandlendeEnhetNavn = enhet.getEnhetNavn();
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public void setAnsvarligBeslutter(String ansvarligBeslutter) {
        this.ansvarligBeslutter = ansvarligBeslutter;
    }

    public List<BehandlingÅrsak> getBehandlingÅrsaker() {
        if (this.behandlingÅrsaker.size() > 1) {
            throw new TekniskException("FPT-473718", String.format("Behandling %s kan ha bare en behandlingsårsak", this.getId()));
        }
        return new ArrayList<>(this.behandlingÅrsaker);
    }

    void leggTilBehandlingÅrsaker(BehandlingÅrsak behandlingÅrsak) {
        if (erAvsluttet()) {
            throw new IllegalStateException("Utvikler-feil: kan ikke legge til årsaker på en behandling som er avsluttet.");
        }
        this.behandlingÅrsaker.add(behandlingÅrsak);
    }

    public boolean isManueltOpprettet() {
        return manueltOpprettet;
    }

    public SaksbehandlingType getSaksbehandlingType() {
        return saksbehandlingType;
    }

    public boolean isAutomatiskSaksbehandlet() {
        return SaksbehandlingType.AUTOMATISK_IKKE_INNKREVING_LAVT_BELØP.equals(saksbehandlingType);
    }

    public void skruPåAutomatiskSaksbehandlingPgaInnkrevingAvLavtBeløp() {
        this.saksbehandlingType = SaksbehandlingType.AUTOMATISK_IKKE_INNKREVING_LAVT_BELØP;
    }

    public VedtaksbrevType utledVedtaksbrevType() {
        return erBehandlingRevurderingOgHarÅrsakFeilutbetalingBortfalt() ? VedtaksbrevType.FRITEKST_FEILUTBETALING_BORTFALT : VedtaksbrevType.ORDINÆR;
    }

    private boolean erBehandlingRevurderingOgHarÅrsakFeilutbetalingBortfalt() {
        return BehandlingType.REVURDERING_TILBAKEKREVING.equals(this.behandlingType) && this.behandlingÅrsaker.stream()
                .anyMatch(behandlingÅrsak -> BehandlingÅrsakType.RE_FEILUTBETALT_BELØP_HELT_ELLER_DELVIS_BORTFALT.equals(behandlingÅrsak.getBehandlingÅrsakType()));
    }

    @SuppressWarnings("unchecked")
    private static <V extends BaseEntitet> Comparator<V> compareOpprettetTid() {
        return (Comparator<V>) COMPARATOR_OPPRETTET_TID;
    }

    @SuppressWarnings("unchecked")
    private static <V extends BaseEntitet> Comparator<V> compareEndretTid() {
        return (Comparator<V>) COMPARE_ENDRET_TID;
    }

    public static class Builder {

        private final BehandlingType behandlingType;
        private Fagsak fagsak;
        private Behandling forrigeBehandling;
        /**
         * optional
         */
        private LocalDateTime opprettetDato;
        private LocalDateTime avsluttetDato;
        private boolean manueltOpprettet;

        private BehandlingÅrsak.Builder behandlingÅrsakBuilder;

        private Builder(Fagsak fagsak, BehandlingType behandlingType) {
            this(behandlingType);
            Objects.requireNonNull(fagsak, "fagsak"); //$NON-NLS-1$
            this.fagsak = fagsak;
        }

        private Builder(Behandling forrigeBehandling, BehandlingType behandlingType) {
            this(behandlingType);
            this.forrigeBehandling = forrigeBehandling;
        }

        private Builder(BehandlingType behandlingType) {
            Objects.requireNonNull(behandlingType, "behandlingType"); //$NON-NLS-1$
            this.behandlingType = behandlingType;
        }

        /**
         * Fix opprettet dato.
         */
        public Builder medOpprettetDato(LocalDateTime tid) {
            this.opprettetDato = tid;
            return this;
        }

        /**
         * Fix avsluttet dato.
         */
        public Builder medAvsluttetDato(LocalDateTime tid) {
            this.avsluttetDato = tid;
            return this;
        }

        public Builder medBehandlingÅrsak(BehandlingÅrsak.Builder årsakBuilder) {
            this.behandlingÅrsakBuilder = årsakBuilder;
            return this;
        }

        public Builder medManueltOpprettet(boolean manueltOpprettet) {
            this.manueltOpprettet = manueltOpprettet;
            return this;
        }


        /**
         * Bygger en Behandling.
         * <p>
         * Husk: Har du brukt riktig Factory metode for å lage en Builder? :
         * <ul>
         * <li>{@link Behandling#fraTidligereBehandling(Behandling, BehandlingType)} (&lt;- BRUK DENNE HVIS DET ER
         * TIDLIGERE BEHANDLINGER PÅ SAMME FAGSAK)</li>
         * </ul>
         */
        public Behandling build() {
            Behandling behandling;

            if (forrigeBehandling != null) {
                behandling = new Behandling(forrigeBehandling.getFagsak(), behandlingType);
            } else {
                behandling = new Behandling(fagsak, behandlingType);
            }

            behandling.opprettetDato = opprettetDato != null ? opprettetDato : LocalDateTime.now();
            if (avsluttetDato != null) {
                behandling.avsluttetDato = avsluttetDato;
            }

            if (behandlingÅrsakBuilder != null) {
                behandlingÅrsakBuilder.buildFor(behandling);
            }
            behandling.manueltOpprettet = manueltOpprettet;

            return behandling;
        }
    }

}
