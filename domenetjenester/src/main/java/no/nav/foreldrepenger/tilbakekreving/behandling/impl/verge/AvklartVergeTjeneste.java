package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.VirksomhetTjeneste;

@ApplicationScoped
public class AvklartVergeTjeneste {

    private VergeRepository vergeRepository;
    private PersoninfoAdapter tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;

    AvklartVergeTjeneste() {
        // for CDI
    }

    @Inject
    public AvklartVergeTjeneste(VergeRepository vergeRepository,
                                PersoninfoAdapter tpsTjeneste,
                                VirksomhetTjeneste virksomhetTjeneste,
                                HistorikkTjenesteAdapter historikkTjenesteAdapter) {
        this.vergeRepository = vergeRepository;
        this.tpsTjeneste = tpsTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
    }

    public void lagreVergeInformasjon(Long behandlingId,
                                      VergeDto vergeDto) {
        VergeEntitet.Builder builder = VergeEntitet.builder()
                .medKilde(KildeType.FPTILBAKE.name())
                .medGyldigPeriode(vergeDto.getFom(), vergeDto.getTom())
                .medNavn(vergeDto.getNavn())
                .medVergeType(vergeDto.getVergeType())
                .medBegrunnelse(vergeDto.getBegrunnelse());
        if (VergeType.ADVOKAT.equals(vergeDto.getVergeType())) {
            if (!virksomhetTjeneste.validerOrganisasjon(vergeDto.getOrganisasjonsnummer())) {
                throw new IllegalStateException("OrgansisasjonNummer er ikke gyldig");
            }
            builder.medOrganisasjonnummer(vergeDto.getOrganisasjonsnummer());
        } else {
            builder.medVergeAktørId(hentAktørId(vergeDto.getFnr()));
        }
        VergeEntitet vergeEntitet = builder.build();
        vergeRepository.lagreVergeInformasjon(behandlingId, vergeEntitet);
        lagHistorikkInnslagForVerge(behandlingId);
    }

    private AktørId hentAktørId(String fnr) {
        Optional<AktørId> aktørId = tpsTjeneste.hentAktørForFnr(new PersonIdent(fnr));
        if (aktørId.isEmpty()) {
            throw BehandlingFeil.fantIkkePersonIdentMedFnr();
        }
        return aktørId.get();
    }

    private void lagHistorikkInnslagForVerge(Long behandlingId) {
        HistorikkinnslagOld historikkinnslag = new HistorikkinnslagOld();
        historikkinnslag.setType(HistorikkinnslagType.REGISTRER_OM_VERGE);
        historikkinnslag.setBehandlingId(behandlingId);
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);

        HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();
        tekstBuilder.medSkjermlenke(SkjermlenkeType.FAKTA_OM_VERGE)
                .medHendelse(HistorikkinnslagType.REGISTRER_OM_VERGE)
                .build(historikkinnslag);

        historikkTjenesteAdapter.lagInnslag(historikkinnslag);
    }
}
