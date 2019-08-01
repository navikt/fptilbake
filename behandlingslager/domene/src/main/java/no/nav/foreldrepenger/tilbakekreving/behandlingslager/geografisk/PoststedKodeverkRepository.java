package no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.AdresseType;

public interface PoststedKodeverkRepository {

    Optional<Poststed> finnPoststed(String postnummer);

    Optional<AdresseType> finnAdresseType(String kode);

}
