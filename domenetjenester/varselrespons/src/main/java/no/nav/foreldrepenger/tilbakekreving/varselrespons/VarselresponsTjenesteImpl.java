package no.nav.foreldrepenger.tilbakekreving.varselrespons;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons.VarselresponsRepository;

@ApplicationScoped
public class VarselresponsTjenesteImpl implements VarselresponsTjeneste {

    private VarselresponsRepository varselresponsRepository;

    public VarselresponsTjenesteImpl() {
        // CDI
    }

    @Inject
    public VarselresponsTjenesteImpl(VarselresponsRepository varselresponsRepository) {
        this.varselresponsRepository = varselresponsRepository;
    }

    @Override
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

    @Override
    public void lagreRespons(long behandlingId, ResponsKanal kanal) {
        lagreRespons(behandlingId, kanal, null);
    }

    @Override
    public Optional<Varselrespons> hentRespons(long behandlingId) {
        return varselresponsRepository.hentRespons(behandlingId);
    }

}
