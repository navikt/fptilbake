package no.nav.foreldrepenger.tilbakekreving.varselrespons;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.VarselresponsRepository;

@ApplicationScoped
public class VarselresponsTjeneste {

    private VarselresponsRepository varselresponsRepository;

    public VarselresponsTjeneste() {
        // CDI
    }

    @Inject
    public VarselresponsTjeneste(VarselresponsRepository varselresponsRepository) {
        this.varselresponsRepository = varselresponsRepository;
    }

    public void lagreRespons(long behandlingId, ResponsKanal responsKanal, Boolean akseptertFaktagrunnlag) {
        Objects.requireNonNull(behandlingId);
        Optional<Varselrespons> eksisterende = varselresponsRepository.hentRespons(behandlingId);
        if (!eksisterende.isPresent()) {
            Varselrespons varselrespons = Varselrespons.builder()
                    .medBehandlingId(behandlingId)
                    .setAkseptertFaktagrunnlag(akseptertFaktagrunnlag)
                    .setKilde(responsKanal.getDbKode())
                    .build();
            varselresponsRepository.lagre(varselrespons);
        }
    }

    public void lagreRespons(long behandlingId, ResponsKanal kanal) {
        lagreRespons(behandlingId, kanal, null);
    }

    public Optional<Varselrespons> hentRespons(long behandlingId) {
        return varselresponsRepository.hentRespons(behandlingId);
    }

}
