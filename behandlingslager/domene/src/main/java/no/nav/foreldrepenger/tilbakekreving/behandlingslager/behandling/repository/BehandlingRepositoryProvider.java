package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

/**
 * Provider for å enklere å kunne hente ut ulike repository uten for mange injection points.
 */
@ApplicationScoped
public class BehandlingRepositoryProvider {

    private EntityManager entityManager;
    private FagsakRepository fagsakRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepository behandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private HistorikkRepository historikkRepository;
    private BehandlingLåsRepository behandlingLåsRepository;
    private FagsakLåsRepository fagsakLåsRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private FaktaFeilutbetalingRepository faktaFeilutbetalingRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private VurdertForeldelseRepository vurdertForeldelseRepository;
    private VilkårsvurderingRepository vilkårsvurderingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private VarselRepository varselRepository;
    private VedtaksbrevFritekstRepository vedtaksbrevFritekstRepository;
    private BrevSporingRepository brevSporingRepository;
    private VergeRepository vergeRepository;


    BehandlingRepositoryProvider() {
        // for CDI proxy
    }

    @Inject
    public BehandlingRepositoryProvider(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;

        this.behandlingRepository = new BehandlingRepository(entityManager);
        this.behandlingresultatRepository = new BehandlingresultatRepository(entityManager);
        this.fagsakRepository = new FagsakRepository(entityManager);
        this.aksjonspunktRepository = new AksjonspunktRepository(entityManager);
        this.historikkRepository = new HistorikkRepository(entityManager);
        this.behandlingLåsRepository = new BehandlingLåsRepository(entityManager);
        this.fagsakLåsRepository = new FagsakLåsRepository(entityManager);
        this.grunnlagRepository = new KravgrunnlagRepository(entityManager);
        this.faktaFeilutbetalingRepository = new FaktaFeilutbetalingRepository(entityManager);
        this.eksternBehandlingRepository = new EksternBehandlingRepository(entityManager);
        this.vurdertForeldelseRepository = new VurdertForeldelseRepository(entityManager);
        this.vilkårsvurderingRepository = new VilkårsvurderingRepository(entityManager);
        this.behandlingVedtakRepository = new BehandlingVedtakRepository(entityManager);
        this.varselRepository = new VarselRepository(entityManager);
        this.vedtaksbrevFritekstRepository = new VedtaksbrevFritekstRepository(entityManager);
        this.brevSporingRepository = new BrevSporingRepository(entityManager);
        this.vergeRepository = new VergeRepository(entityManager);
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    public BehandlingRepository getBehandlingRepository() {
        return behandlingRepository;
    }

    public BehandlingresultatRepository getBehandlingresultatRepository() {
        return behandlingresultatRepository;
    }

    public AksjonspunktRepository getAksjonspunktRepository() {
        return aksjonspunktRepository;
    }

    public FagsakRepository getFagsakRepository() {
        // bridge metode før sammenkobling medBehandling
        return fagsakRepository;
    }

    public HistorikkRepository getHistorikkRepository() {
        return historikkRepository;
    }

    public BehandlingLåsRepository getBehandlingLåsRepository() {
        return behandlingLåsRepository;
    }

    public FagsakLåsRepository getFagsakLåsRepository() {
        return fagsakLåsRepository;
    }

    public KravgrunnlagRepository getGrunnlagRepository() {
        return grunnlagRepository;
    }

    public FaktaFeilutbetalingRepository getFaktaFeilutbetalingRepository() {
        return faktaFeilutbetalingRepository;
    }

    public EksternBehandlingRepository getEksternBehandlingRepository() {
        return eksternBehandlingRepository;
    }

    public VurdertForeldelseRepository getVurdertForeldelseRepository() {
        return vurdertForeldelseRepository;
    }

    public VilkårsvurderingRepository getVilkårsvurderingRepository() {
        return vilkårsvurderingRepository;
    }

    public BehandlingVedtakRepository getBehandlingVedtakRepository() {
        return behandlingVedtakRepository;
    }

    public VarselRepository getVarselRepository() {
        return varselRepository;
    }

    public VedtaksbrevFritekstRepository getVedtaksbrevFritekstRepository() {
        return vedtaksbrevFritekstRepository;
    }

    public BrevSporingRepository getBrevSporingRepository() {
        return brevSporingRepository;
    }

    public VergeRepository getVergeRepository() {
        return vergeRepository;
    }


}
