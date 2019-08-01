package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FeilutbetalingÅrsakDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.ÅrsakUdefinert;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodelisteRelasjon;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeilutbetalingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.UnderÅrsakDto;

@ApplicationScoped
public class FeilutbetalingÅrsakTjeneste {

    private FeilutbetalingRepository feilutbetalingRepository;

    FeilutbetalingÅrsakTjeneste() {
        // For CDI
    }

    @Inject
    public FeilutbetalingÅrsakTjeneste(FeilutbetalingRepository feilutbetalingRepository) {
        this.feilutbetalingRepository = feilutbetalingRepository;
    }

    public List<FeilutbetalingÅrsakDto> hentAlleÅrsaker() {
        List<FeilutbetalingÅrsakDefinisjon> feilutbetalingÅrsaker = feilutbetalingRepository.henteAlleÅrsaker();
        List<FeilutbetalingÅrsakDto> årsaker = new ArrayList<>();
        if (!feilutbetalingÅrsaker.isEmpty()) {
            List<Kodeliste> kodelister = feilutbetalingRepository.henteKodeliste(feilutbetalingÅrsaker.stream()
                    .map(def -> def.getÅrsak())
                    .collect(Collectors.toList()));
            for (Kodeliste kodeliste : kodelister) {
                FeilutbetalingÅrsakDto feilutbetalingÅrsakDto = new FeilutbetalingÅrsakDto();
                feilutbetalingÅrsakDto.setÅrsakKode(kodeliste.getKode());
                feilutbetalingÅrsakDto.setÅrsak(kodeliste.getNavn());
                feilutbetalingÅrsakDto.setKodeverk(kodeliste.getKodeverk());
                List<KodelisteRelasjon> kodelisteRelasjoner = feilutbetalingRepository.henteKodelisteRelasjon(kodeliste.getKodeverk(), kodeliste.getKode());
                formUnderÅrsak(feilutbetalingÅrsakDto, kodelisteRelasjoner);

                årsaker.add(feilutbetalingÅrsakDto);
            }
        }
        return årsaker;
    }

    private void formUnderÅrsak(FeilutbetalingÅrsakDto feilutbetalingÅrsakDto, List<KodelisteRelasjon> kodelisteRelasjoner) {
        List<String> underÅrsakKodeverker = kodelisteRelasjoner.stream()
                .filter(kodelisteRelasjon -> !kodelisteRelasjon.getKode2().equals(ÅrsakUdefinert.UDEFINERT.getKode()))
                .map(kodelisteRelasjon -> kodelisteRelasjon.getKodeverk2())
                .collect(Collectors.toList());
        if (!underÅrsakKodeverker.isEmpty()) {
            List<Kodeliste> underÅrsakKodelister = feilutbetalingRepository.henteKodeliste(underÅrsakKodeverker);
            for (Kodeliste underÅrsakKodeListe : underÅrsakKodelister) {
                feilutbetalingÅrsakDto.leggTilUnderÅrsaker(new UnderÅrsakDto(underÅrsakKodeListe.getNavn(), underÅrsakKodeListe.getKode(),
                        underÅrsakKodeListe.getKodeverk()));
            }
        }
    }
}
