package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevFritekstRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakLåsRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårsvurderingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

/**
 * Provider for å enklere å kunne hente ut ulike repository uten for mange injection points.
 */
@ApplicationScoped
public class BehandlingRepositoryProviderImpl implements BehandlingRepositoryProvider {

    private EntityManager entityManager;
    private FagsakRepository fagsakRepository;
    private KodeverkRepositoryImpl kodeverkRepository;
    private AksjonspunktRepositoryImpl aksjonspunktRepository;
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


    BehandlingRepositoryProviderImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingRepositoryProviderImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;

        this.kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        this.behandlingRepository = new BehandlingRepositoryImpl(entityManager);
        this.behandlingresultatRepository = new BehandlingresultatRepositoryImpl(entityManager);
        this.fagsakRepository = new FagsakRepositoryImpl(entityManager);
        this.aksjonspunktRepository = new AksjonspunktRepositoryImpl(entityManager, this.kodeverkRepository);
        this.historikkRepository = new HistorikkRepository(entityManager);
        this.behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
        this.fagsakLåsRepository = new FagsakLåsRepositoryImpl(entityManager);
        this.grunnlagRepository = new KravgrunnlagRepository(entityManager);
        this.faktaFeilutbetalingRepository = new FaktaFeilutbetalingRepository(entityManager);
        this.eksternBehandlingRepository = new EksternBehandlingRepositoryImpl(entityManager);
        this.vurdertForeldelseRepository = new VurdertForeldelseRepositoryImpl(entityManager);
        this.vilkårsvurderingRepository = new VilkårsvurderingRepository(entityManager);
        this.behandlingVedtakRepository = new BehandlingVedtakRepository(entityManager);
        this.varselRepository = new VarselRepository(entityManager);
        this.vedtaksbrevFritekstRepository = new VedtaksbrevFritekstRepository(entityManager);
        this.brevSporingRepository = new BrevSporingRepository(entityManager);
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public BehandlingRepository getBehandlingRepository() {
        return behandlingRepository;
    }

    @Override
    public BehandlingresultatRepository getBehandlingresultatRepository() {
        return behandlingresultatRepository;
    }

    @Override
    public KodeverkRepository getKodeverkRepository() {
        return kodeverkRepository;
    }

    @Override
    public AksjonspunktRepository getAksjonspunktRepository() {
        return aksjonspunktRepository;
    }

    @Override
    public FagsakRepository getFagsakRepository() {
        // bridge metode før sammenkobling medBehandling
        return fagsakRepository;
    }

    @Override
    public HistorikkRepository getHistorikkRepository() {
        return historikkRepository;
    }

    @Override
    public BehandlingLåsRepository getBehandlingLåsRepository() {
        return behandlingLåsRepository;
    }

    @Override
    public FagsakLåsRepository getFagsakLåsRepository() {
        return fagsakLåsRepository;
    }

    @Override
    public KravgrunnlagRepository getGrunnlagRepository() {
        return grunnlagRepository;
    }

    @Override
    public FaktaFeilutbetalingRepository getFaktaFeilutbetalingRepository() {
        return faktaFeilutbetalingRepository;
    }

    @Override
    public EksternBehandlingRepository getEksternBehandlingRepository() {
        return eksternBehandlingRepository;
    }

    @Override
    public VurdertForeldelseRepository getVurdertForeldelseRepository() {
        return vurdertForeldelseRepository;
    }

    @Override
    public VilkårsvurderingRepository getVilkårsvurderingRepository() {
        return vilkårsvurderingRepository;
    }

    @Override
    public BehandlingVedtakRepository getBehandlingVedtakRepository() {
        return behandlingVedtakRepository;
    }

    @Override
    public VarselRepository getVarselRepository() {
        return varselRepository;
    }

    @Override
    public VedtaksbrevFritekstRepository getVedtaksbrevFritekstRepository() {
        return vedtaksbrevFritekstRepository;
    }

    @Override
    public BrevSporingRepository getBrevSporingRepository() {
        return brevSporingRepository;
    }

}
