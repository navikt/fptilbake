package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.AnnetÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.UtsettelseArbeid;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.UtsettelseÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeilutbetalingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.UnderÅrsakDto;

public class FeilutbetalingÅrsakTjenesteTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private FeilutbetalingRepository feilutbetalingRepository = new FeilutbetalingRepository(repoRule.getEntityManager());
    private FeilutbetalingÅrsakTjeneste feilutbetalingÅrsakTjeneste = new FeilutbetalingÅrsakTjeneste(feilutbetalingRepository);

    @Test
    public void hentAlleÅrsaker() {
        List<FeilutbetalingÅrsakDto> feilutbetalingÅrsaker = feilutbetalingÅrsakTjeneste.hentAlleÅrsaker();

        assertThat(feilutbetalingÅrsaker).isNotEmpty();
        assertThat(feilutbetalingÅrsaker.size()).isEqualTo(21);

        feilutbetalingÅrsaker.sort(Comparator.comparing(FeilutbetalingÅrsakDto::getÅrsakKode));
        assertThat(feilutbetalingÅrsaker.get(0).getÅrsakKode()).isEqualTo(AnnetÅrsakType.ANNET.getKode());

        Optional<FeilutbetalingÅrsakDto> feilutbetalingÅrsakDto = feilutbetalingÅrsaker.stream()
                .filter(årsak -> årsak.getÅrsakKode().equals(UtsettelseÅrsakType.ARBEID.getKode())).findAny();
        assertThat(feilutbetalingÅrsakDto).isPresent();

        List<UnderÅrsakDto> underÅrsaker = feilutbetalingÅrsakDto.get().getUnderÅrsaker();
        underÅrsaker.sort(Comparator.comparing(UnderÅrsakDto::getUnderÅrsakKode));

        assertThat(underÅrsaker.size()).isEqualTo(2);
        assertThat(underÅrsaker.get(0).getUnderÅrsakKode()).isEqualTo(UtsettelseArbeid.UTSETTELSE_ARBEID_DELTID.getKode());
    }
}