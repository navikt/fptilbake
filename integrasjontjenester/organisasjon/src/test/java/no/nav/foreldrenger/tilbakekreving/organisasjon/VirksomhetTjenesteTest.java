package no.nav.foreldrenger.tilbakekreving.organisasjon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.Virksomhet;
import no.nav.foreldrepenger.tilbakekreving.organisasjon.VirksomhetTjeneste;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.feil.OrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Landkoder;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoeklerAdresseleddSemistrukturerteAdresser;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SammensattNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumerImpl;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;

public class VirksomhetTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private static final String ORGANISASJON_NR="1234567890";
    private static final String ORGANISASJON_NAVN="Test organisasjon";

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repositoryRule.getEntityManager());
    private OrganisasjonConsumer organisasjonConsumerMock = mock(OrganisasjonConsumerImpl.class);
    private VirksomhetTjeneste virksomhetTjeneste = new VirksomhetTjeneste(organisasjonConsumerMock,kodeverkRepository);

    @Test
    public void skal_hente_organisasjon_for_gyldig_organisasjon_nummer() throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        when(organisasjonConsumerMock.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(lagOrganisasjonRespons());
        Virksomhet virksomhet = virksomhetTjeneste.hentOrganisasjon(ORGANISASJON_NR);
        assertThat(virksomhet).isNotNull();
        assertThat(virksomhet.getOrgnr()).isEqualTo(ORGANISASJON_NR);
        assertThat(virksomhet.getAdresselinje1()).isEqualTo("12,XXX Veien");
        assertThat(virksomhet.getAdresselinje2()).isEqualTo("xxx");
        assertThat(virksomhet.getPoststed()).isEqualTo("OSLO");
        assertThat(virksomhet.getLandkode()).isEqualTo("NOR");
        assertThat(virksomhet.getNavn()).isEqualTo(ORGANISASJON_NAVN);
    }

    @Test
    public void skal_ikke_validere_organisasjon_for_ugyldig_organisasjon_nummer() throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        when(organisasjonConsumerMock.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
            .thenThrow(new HentOrganisasjonUgyldigInput("Ugyldig input", new UgyldigInput()));
        assertThat(virksomhetTjeneste.validerOrganisasjon(ORGANISASJON_NR)).isFalse();
    }

    @Test
    public void skal_ikke_validere_organisasjon_for_organisasjon_nummer_ikke_funnet() throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        when(organisasjonConsumerMock.hentOrganisasjon(any(HentOrganisasjonRequest.class)))
            .thenThrow(new HentOrganisasjonOrganisasjonIkkeFunnet("Organisasjon er ikke funnet", new OrganisasjonIkkeFunnet()));
        assertThat(virksomhetTjeneste.validerOrganisasjon(ORGANISASJON_NR)).isFalse();
    }

    private HentOrganisasjonResponse lagOrganisasjonRespons() {
        HentOrganisasjonResponse hentOrganisasjonResponse = new HentOrganisasjonResponse();
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer(ORGANISASJON_NR);
        UstrukturertNavn ustrukturertNavn = new UstrukturertNavn();
        ustrukturertNavn.getNavnelinje().add(ORGANISASJON_NAVN);
        organisasjon.setNavn((SammensattNavn) ustrukturertNavn);
        OrganisasjonsDetaljer organisasjonsDetaljer = new OrganisasjonsDetaljer();
        SemistrukturertAdresse semistrukturertAdresse = new SemistrukturertAdresse();
        Landkoder landkoder = new Landkoder();
        landkoder.setKodeRef("NOR");
        semistrukturertAdresse.setLandkode(landkoder);
        List<NoekkelVerdiAdresse> adresseDetaljer = new ArrayList<>();
        NoekkelVerdiAdresse noekkelVerdiAdresse = new NoekkelVerdiAdresse();
        NoeklerAdresseleddSemistrukturerteAdresser addressLine1 = new NoeklerAdresseleddSemistrukturerteAdresser();
        addressLine1.setKodeRef("adresselinje1");
        noekkelVerdiAdresse.setNoekkel(addressLine1);
        noekkelVerdiAdresse.setVerdi("12,XXX Veien");
        adresseDetaljer.add(noekkelVerdiAdresse);

        noekkelVerdiAdresse = new NoekkelVerdiAdresse();
        NoeklerAdresseleddSemistrukturerteAdresser addressLine2 = new NoeklerAdresseleddSemistrukturerteAdresser();
        addressLine2.setKodeRef("adresselinje2");
        noekkelVerdiAdresse.setNoekkel(addressLine2);
        noekkelVerdiAdresse.setVerdi("xxx");
        adresseDetaljer.add(noekkelVerdiAdresse);

        noekkelVerdiAdresse = new NoekkelVerdiAdresse();
        NoeklerAdresseleddSemistrukturerteAdresser kommunenr = new NoeklerAdresseleddSemistrukturerteAdresser();
        kommunenr.setKodeRef("kommunenr");
        noekkelVerdiAdresse.setNoekkel(kommunenr);
        noekkelVerdiAdresse.setVerdi("0103");
        adresseDetaljer.add(noekkelVerdiAdresse);
        semistrukturertAdresse.getAdresseledd().addAll(adresseDetaljer);
        organisasjonsDetaljer.getForretningsadresse().add((SemistrukturertAdresse) semistrukturertAdresse);
        organisasjon.setOrganisasjonDetaljer(organisasjonsDetaljer);
        hentOrganisasjonResponse.setOrganisasjon(organisasjon);
        return hentOrganisasjonResponse;
    }

}
