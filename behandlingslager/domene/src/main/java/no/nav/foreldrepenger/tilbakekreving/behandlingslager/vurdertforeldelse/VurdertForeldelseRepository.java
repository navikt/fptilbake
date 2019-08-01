package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

import java.util.Optional;

public interface VurdertForeldelseRepository {

    void lagre(VurdertForeldelseAggregate vurdertForeldelseAggregate);

    void lagre(Long behandlingId, VurdertForeldelse vurdertForeldelse);

    Optional<VurdertForeldelseAggregate> finnVurdertForeldelseForBehandling(Long behandlingId);

    boolean harVurdertForeldelseForBehandlingId(Long behandlingId);
}
