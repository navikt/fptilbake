package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.KildeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;

public class AvklartVergeTjenesteTest extends FellesTestOppsett {

    private VergeRepository vergeRepository = new VergeRepository(repoRule.getEntityManager());
    private AvklartVergeTjeneste avklartVergeTjeneste = new AvklartVergeTjeneste(vergeRepository, mockTpsTjeneste, historikkTjenesteAdapter);

    @Test
    public void skal_lagre_verge_informasjon_når_verge_er_advokat() {
        VergeDto vergeDto = lagVergeDto(VergeType.ADVOKAT);
        avklartVergeTjeneste.lagreVergeInformasjon(internBehandlingId, vergeDto);
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(internBehandlingId);
        assertThat(vergeEntitet).isNotEmpty();
        VergeEntitet vergeOrg = vergeEntitet.get();
        fellesVergeAssert(vergeDto, vergeOrg);
        assertThat(vergeOrg.getOrganisasjonsnummer()).isNotEmpty();
        assertThat(vergeOrg.getVergeType()).isEqualByComparingTo(VergeType.ADVOKAT);
        fellesHistorikkAssert();
    }

    @Test
    public void skal_lagre_verge_informasjon_når_verge_er_ikke_advokat() {
        VergeDto vergeDto = lagVergeDto(VergeType.FBARN);
        when(mockTpsTjeneste.hentAktørForFnr(any(PersonIdent.class))).thenReturn(Optional.of(behandling.getAktørId()));
        avklartVergeTjeneste.lagreVergeInformasjon(internBehandlingId, vergeDto);
        Optional<VergeEntitet> vergeEntitet = vergeRepository.finnVergeInformasjon(internBehandlingId);
        assertThat(vergeEntitet).isNotEmpty();
        VergeEntitet vergePerson = vergeEntitet.get();
        fellesVergeAssert(vergeDto, vergePerson);
        assertThat(vergePerson.getVergeAktørId()).isNotNull();
        assertThat(vergePerson.getVergeType()).isEqualByComparingTo(VergeType.FBARN);
        fellesHistorikkAssert();
    }

    private void fellesVergeAssert(VergeDto vergeDto, VergeEntitet vergeEntitet) {
        assertThat(vergeEntitet.getGyldigFom()).isEqualTo(vergeDto.getFom());
        assertThat(vergeEntitet.getGyldigTom()).isEqualTo(vergeDto.getTom());
        assertThat(vergeEntitet.getNavn()).isEqualTo(vergeDto.getNavn());
        assertThat(vergeEntitet.getKilde()).isEqualTo(KildeType.FPTILBAKE.name());
        assertThat(vergeEntitet.getBegrunnelse()).isEqualTo("begrunnelse");
    }

    private void fellesHistorikkAssert() {
        List<Historikkinnslag> historikkinnslager = historikkRepository.hentHistorikk(internBehandlingId);
        assertThat(historikkinnslager).isNotEmpty();
        assertThat(historikkinnslager.size()).isEqualTo(1);
        assertThat(historikkinnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.REGISTRER_OM_VERGE);
        List<HistorikkinnslagDel> historikkinnslagDeler = historikkinnslager.get(0).getHistorikkinnslagDeler();
        assertThat(historikkinnslagDeler).isNotEmpty();
        assertThat(historikkinnslagDeler.size()).isEqualTo(1);
        assertThat(historikkinnslagDeler.get(0).getSkjermlenke()).isNotEmpty();
        assertThat(historikkinnslagDeler.get(0).getSkjermlenke().get()).isEqualTo(SkjermlenkeType.FAKTA_OM_VERGE.getKode());
        assertThat(historikkinnslagDeler.get(0).getHendelse()).isNotEmpty();
    }

    private VergeDto lagVergeDto(VergeType vergeType) {
        VergeDto vergeDto = new VergeDto();
        vergeDto.setFnr("12345678901");
        vergeDto.setFom(LocalDate.now().minusYears(1));
        vergeDto.setTom(LocalDate.now());
        vergeDto.setNavn("John Doe");
        vergeDto.setOrganisasjonsnummer("123456789");
        vergeDto.setVergeType(vergeType);
        vergeDto.setBegrunnelse("begrunnelse");
        return vergeDto;
    }
}
