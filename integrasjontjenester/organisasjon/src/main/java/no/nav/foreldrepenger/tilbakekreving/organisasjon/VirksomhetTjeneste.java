package no.nav.foreldrepenger.tilbakekreving.organisasjon;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Poststed;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.GeografiskAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.NoekkelVerdiAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;

@ApplicationScoped
public class VirksomhetTjeneste {

    private static final Logger log = LoggerFactory.getLogger(VirksomhetTjeneste.class);
    private OrganisasjonConsumer organisasjonConsumer;
    private KodeverkRepository kodeverkRepository;

    VirksomhetTjeneste() {
        // for CDI
    }

    @Inject
    public VirksomhetTjeneste(OrganisasjonConsumer organisasjonConsumer,
                              KodeverkRepository kodeverkRepository) {
        this.organisasjonConsumer = organisasjonConsumer;
        this.kodeverkRepository = kodeverkRepository;
    }

    public Virksomhet hentOrganisasjon(String orgNummer) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput{
        Objects.requireNonNull(orgNummer, "orgNummer");
        HentOrganisasjonRequest hentOrganisasjonRequest = new HentOrganisasjonRequest(orgNummer);
        HentOrganisasjonResponse organisasjonResponse = organisasjonConsumer.hentOrganisasjon(hentOrganisasjonRequest);
        return mapOrganisasjonResponseToVirksomhet(organisasjonResponse.getOrganisasjon());
    }

    public boolean validerOrganisasjon(String orgNummer) {
        try {
            return hentOrganisasjon(orgNummer) != null;
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput e) {
            log.warn("Kan ikke hente organisasjon for orgNummer {}", orgNummer, e);
            return false;
        }
    }

    private Virksomhet mapOrganisasjonResponseToVirksomhet(Organisasjon organisasjon) {
        Virksomhet.Builder builder = new Virksomhet.Builder();
        builder.medNavn(((UstrukturertNavn) organisasjon.getNavn()).getNavnelinje().stream().filter(it -> !it.isEmpty())
            .reduce("", (a, b) -> a + " " + b).trim());
        builder.medOrgnr(organisasjon.getOrgnummer());
        List<GeografiskAdresse> adresser = organisasjon.getOrganisasjonDetaljer().getForretningsadresse();
        GeografiskAdresse qa = adresser.get(0);
        builder.medLandkode(qa.getLandkode().getKodeRef());
        List<NoekkelVerdiAdresse> al = ((SemistrukturertAdresse) qa).getAdresseledd();
        for (NoekkelVerdiAdresse noekkelVerdiAdresse : al) {
            String verdi = noekkelVerdiAdresse.getVerdi();
            switch (noekkelVerdiAdresse.getNoekkel().getKodeRef()) {
                case "adresselinje1":
                    builder.medAdresselinje1(verdi);
                    break;
                case "adresselinje2":
                    builder.medAdresselinje2(verdi);
                    break;
                case "adresselinje3":
                    builder.medAdresselinje3(verdi);
                    break;
                case "adresselinje4":
                    builder.medAdresselinje4(verdi);
                    break;
                case "kommunenr":
                    builder.medPoststed(kodeverkRepository.finn(Poststed.class, verdi).getNavn());
                    break;
                default:
                    break;
            }
        }
        return builder.build();
    }

}
