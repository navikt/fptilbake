package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

import java.util.Optional;

public interface VurdertForeldelseRepository {

    Optional<VurdertForeldelse> finnVurdertForeldelse(Long behandlingId);

    Optional<Long> finnVurdertForeldelseAggregateId(Long behandlingId);

    boolean harVurdertForeldelseForBehandlingId(Long behandlingId);

    void lagre(Long behandlingId, VurdertForeldelse vurdertForeldelse);

    void slettForeldelse(Long behandlingId);
}
