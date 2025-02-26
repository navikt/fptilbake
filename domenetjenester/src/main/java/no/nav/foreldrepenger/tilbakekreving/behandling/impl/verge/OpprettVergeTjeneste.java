package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.dto.OpprettVergeDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.VirksomhetTjeneste;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType.FAKTA_OM_VERGE;

@ApplicationScoped
public class OpprettVergeTjeneste {

    private VergeRepository vergeRepository;
    private PersoninfoAdapter personinfoAdapter;
    private VirksomhetTjeneste virksomhetTjeneste;
    private HistorikkinnslagRepository historikkRepository;

    OpprettVergeTjeneste() {
        // for CDI
    }

    @Inject
    public OpprettVergeTjeneste(VergeRepository vergeRepository,
                                PersoninfoAdapter personinfoAdapter,
                                VirksomhetTjeneste virksomhetTjeneste,
                                HistorikkinnslagRepository historikkRepository) {
        this.vergeRepository = vergeRepository;
        this.personinfoAdapter = personinfoAdapter;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.historikkRepository = historikkRepository;
    }

    public void opprettVerge(Long behandlingId, Long fagsakId, OpprettVergeDto dto) {
        VergeEntitet.Builder vergeBuilder = VergeEntitet.builder()
                .medKilde(KildeType.FPTILBAKE.name())
                .medVergeType(dto.vergeType())
                .medNavn(dto.navn())
                .medGyldigPeriode(dto.gyldigFom(), dto.gyldigTom())
                .medBegrunnelse(dto.begrunnelse());

        if (VergeType.ADVOKAT.equals(dto.vergeType())) {
            if (!virksomhetTjeneste.validerOrganisasjon(dto.organisasjonsnummer())) {
                throw new IllegalStateException("OrgansisasjonNummer er ikke gyldig");
            }
            vergeBuilder.medOrganisasjonnummer(dto.organisasjonsnummer());
        } else {
            var personIdent = new PersonIdent(dto.fnr());
            vergeBuilder.medVergeAktørId(hentAktørId(personIdent));
        }

        var harEksisterendeVerge = vergeRepository.finnVergeInformasjon(behandlingId).isPresent();

        vergeRepository.lagreVergeInformasjon(behandlingId, vergeBuilder.build());
        opprettHistorikkinnslag(behandlingId, fagsakId, dto, harEksisterendeVerge);
    }

    private AktørId hentAktørId(PersonIdent personIdent) {
        return personinfoAdapter.hentAktørForFnr(personIdent)
                .orElseThrow(BehandlingFeil::fantIkkePersonIdentMedFnr);
    }

    private void opprettHistorikkinnslag(Long behandlingId, Long fagsakId, OpprettVergeDto dto, boolean erEndring) {
        var builder = new Historikkinnslag.Builder().medFagsakId(fagsakId)
                .medBehandlingId(behandlingId)
                .medAktør(HistorikkAktør.SAKSBEHANDLER)
                .medTittel(FAKTA_OM_VERGE)
                .addLinje(String.format("Opplysninger om verge/fullmektig er %s.", erEndring ? "endret" : "registrert"))
                .addLinje(dto.begrunnelse());

        historikkRepository.lagre(builder.build());
    }
}
