package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.dto.OpprettVerge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.VirksomhetTjeneste;

class OpprettVergeTjenesteTest extends FellesTestOppsett {

    private VergeRepository vergeRepository;
    private VirksomhetTjeneste virksomhetTjenesteMock;
    private OpprettVergeTjeneste opprettVergeTjeneste;

    @BeforeEach
    void setUp() {
        vergeRepository = new VergeRepository(entityManager);
        virksomhetTjenesteMock = Mockito.mock(VirksomhetTjeneste.class);
        opprettVergeTjeneste = new OpprettVergeTjeneste(vergeRepository, mockTpsTjeneste, virksomhetTjenesteMock, historikkinnslagRepository);
    }

    @Test
    void skal_lagre_verge_når_verge_er_organisasjon() {
        var vergeDto = lagVergeDto(VergeType.ADVOKAT);
        when(virksomhetTjenesteMock.validerOrganisasjon(anyString())).thenReturn(true);
        opprettVergeTjeneste.opprettVerge(behandling.getId(), behandling.getFagsakId(), vergeDto);
        var vergeEntitet = vergeRepository.finnVergeInformasjon(internBehandlingId);
        assertThat(vergeEntitet)
                .isPresent()
                .hasValueSatisfying(ve -> assertVerge(ve, vergeDto));
        assertHistorikkinnslag();
    }

    @Test
    void skal_lagre_verge_når_verge_er_person() {
        var vergeDto = lagVergeDto(VergeType.FBARN);
        when(mockTpsTjeneste.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getAktørId()));
        opprettVergeTjeneste.opprettVerge(behandling.getId(), behandling.getFagsakId(), vergeDto);

        var vergeEntitet = vergeRepository.finnVergeInformasjon(internBehandlingId);
        assertThat(vergeEntitet)
                .isPresent()
                .hasValueSatisfying(ve -> assertVerge(ve, vergeDto));
        assertHistorikkinnslag();
    }

    @Test
    void skal_feile_når_vergeorganisasjon_ikke_finnes() {
        var vergeDto = lagVergeDto(VergeType.ADVOKAT);
        when(virksomhetTjenesteMock.validerOrganisasjon(anyString())).thenReturn(false);
        assertThatIllegalStateException().isThrownBy(() -> opprettVergeTjeneste.opprettVerge(behandling.getId(), behandling.getFagsakId(), vergeDto))
                .withMessage("OrgansisasjonNummer er ikke gyldig");
    }

    private void assertVerge(VergeEntitet vergeEntitet, OpprettVerge expected) {
        assertThat(vergeEntitet).isNotNull().satisfies(ve -> {
            assertThat(ve.getKilde()).isEqualTo(KildeType.FPTILBAKE.name());
            assertThat(ve.getVergeType()).isEqualTo(expected.vergeType());
            assertThat(ve.getNavn()).isEqualTo(expected.navn());
            assertThat(ve.getGyldigFom()).isEqualTo(expected.gyldigFom());
            assertThat(ve.getGyldigTom()).isEqualTo(expected.gyldigTom());
            assertThat(ve.getBegrunnelse()).isEqualTo(expected.begrunnelse());
            if (VergeType.ADVOKAT.equals(expected.vergeType())) {
                assertThat(ve.getOrganisasjonsnummer()).isEqualTo(expected.organisasjonsnummer());
                assertThat(ve.getVergeAktørId()).isNull();
            } else {
                assertThat(ve.getVergeAktørId()).isEqualTo(behandling.getAktørId());
                assertThat(ve.getOrganisasjonsnummer()).isNull();
            }
        });
    }

    private void assertHistorikkinnslag() {
        var historikkinnslager = historikkinnslagRepository.hent(internBehandlingId);
        assertThat(historikkinnslager).hasSize(2);
        assertThat(historikkinnslager.getFirst()).satisfies(h -> {
            assertThat(h.getTittel()).isEqualTo("Tilbakekreving opprettet");
            assertThat(h.getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        });
        assertThat(historikkinnslager.getLast()).satisfies(h -> {
            assertThat(h.getAktør()).isEqualTo(HistorikkAktør.SAKSBEHANDLER);
            assertThat(h.getSkjermlenke()).isEqualTo(SkjermlenkeType.FAKTA_OM_VERGE);
            assertThat(h.getTekstLinjer()).hasSize(2).containsExactly("Opplysninger om verge/fullmektig er registrert.", "Dette er en begrunnelse.");
        });
    }

    private OpprettVerge lagVergeDto(VergeType vergeType) {
        return new OpprettVerge(
                "John Doe",
                VergeType.ADVOKAT.equals(vergeType) ? null : "12345678901",
                LocalDate.now().minusYears(1),
                LocalDate.now(),
                vergeType,
                VergeType.ADVOKAT.equals(vergeType) ? "123456789" : null, "Dette er en begrunnelse"
        );
    }
}
