package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Region;

public interface BehandlingsgrunnlagKodeverkRepository extends BehandlingslagerRepository {

    SivilstandType finnSivilstandType(String kode);

    Landkoder finnLandkode(String kode);

    List<PersonstatusType> personstatusTyperFortsattBehandling();

    Region finnHøyestRangertRegion(List<String> statsborgerskap);

    Map<Landkoder, Region> finnRegionForStatsborgerskap(List<Landkoder> statsborgerskap);

    List<Region> finnRegioner(String kode);

}
