package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.dto.OpprettVergeDto;
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
    void skal_lagre_verge_informasjon_når_verge_er_advokat() {
        var vergeDto = lagVergeDto(VergeType.ADVOKAT);
        when(virksomhetTjenesteMock.validerOrganisasjon(anyString())).thenReturn(true);
        opprettVergeTjeneste.lagreVergeInformasjon(behandling, vergeDto);
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(internBehandlingId);
        assertThat(vergeEntitet).isNotEmpty();
        VergeEntitet vergeOrg = vergeEntitet.get();
        fellesVergeAssert(vergeDto, vergeOrg);
        assertThat(vergeOrg.getOrganisasjonsnummer()).isNotEmpty();
        assertThat(vergeOrg.getVergeType()).isEqualByComparingTo(VergeType.ADVOKAT);
        fellesHistorikkAssert();
    }

    @Test
    void skal_lagre_verge_informasjon_når_verge_er_ikke_advokat() {
        var vergeDto = lagVergeDto(VergeType.FBARN);
        when(mockTpsTjeneste.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getAktørId()));
        opprettVergeTjeneste.lagreVergeInformasjon(behandling, vergeDto);
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(internBehandlingId);
        assertThat(vergeEntitet).isNotEmpty();
        VergeEntitet vergePerson = vergeEntitet.get();
        fellesVergeAssert(vergeDto, vergePerson);
        assertThat(vergePerson.getVergeAktørId()).isNotNull();
        assertThat(vergePerson.getVergeType()).isEqualByComparingTo(VergeType.FBARN);
        fellesHistorikkAssert();
    }

    @Test
    void skal_ikke_lagre_verge_informasjon_når_verge_er_advokat_men_orgnummer_ikke_finnes() {
        var vergeDto = lagVergeDto(VergeType.ADVOKAT);
        when(virksomhetTjenesteMock.validerOrganisasjon(anyString())).thenReturn(false);
        var e = assertThrows(IllegalStateException.class,
                () -> opprettVergeTjeneste.lagreVergeInformasjon(behandling, vergeDto));
        assertThat(e.getMessage()).contains("OrgansisasjonNummer er ikke gyldig");
    }

    private void fellesVergeAssert(OpprettVergeDto vergeDto, VergeEntitet vergeEntitet) {
        assertThat(vergeEntitet.getGyldigFom()).isEqualTo(vergeDto.gyldigFom());
        assertThat(vergeEntitet.getGyldigTom()).isEqualTo(vergeDto.gyldigTom());
        assertThat(vergeEntitet.getNavn()).isEqualTo(vergeDto.navn());
        assertThat(vergeEntitet.getKilde()).isEqualTo(KildeType.FPTILBAKE.name());
        assertThat(vergeEntitet.getBegrunnelse()).isEqualTo("begrunnelse");
    }

    private void fellesHistorikkAssert() {
        var historikkinnslager = historikkinnslagRepository.hent(internBehandlingId);
        assertThat(historikkinnslager).hasSize(2);
        var historikkinnslagTbkOpprettet = historikkinnslager.get(0);
        assertThat(historikkinnslagTbkOpprettet.getTittel()).isEqualTo("Tilbakekreving opprettet");
        assertThat(historikkinnslagTbkOpprettet.getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);

        var historikkinnslagRegistrerOmVerge = historikkinnslager.get(1);
        assertThat(historikkinnslagRegistrerOmVerge.getSkjermlenke()).isEqualTo(SkjermlenkeType.FAKTA_OM_VERGE);
        assertThat(historikkinnslagRegistrerOmVerge.getLinjer()).hasSize(1);
        assertThat(historikkinnslagRegistrerOmVerge.getLinjer().getFirst().getTekst()).contains("Registering av opplysninger om verge/fullmektig");
    }

    private OpprettVergeDto lagVergeDto(VergeType vergeType) {
        return new OpprettVergeDto(
                "John Doe",
                "12345678901",
                LocalDate.now().minusYears(1),
                LocalDate.now(),
                vergeType,
                null,
                "begrunnelse"
        );
    }
}
