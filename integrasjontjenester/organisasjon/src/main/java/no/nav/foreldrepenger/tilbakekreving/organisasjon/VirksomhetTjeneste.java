package no.nav.foreldrepenger.tilbakekreving.organisasjon;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Poststed;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.PoststedKodeverkRepository;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonRestKlient;

@ApplicationScoped
public class VirksomhetTjeneste {

    private static final Logger log = LoggerFactory.getLogger(VirksomhetTjeneste.class);
    private OrganisasjonRestKlient organisasjonConsumer;
    private PoststedKodeverkRepository kodeverkRepository;

    VirksomhetTjeneste() {
        // for CDI
    }

    @Inject
    public VirksomhetTjeneste(OrganisasjonRestKlient organisasjonConsumer,
                              PoststedKodeverkRepository kodeverkRepository) {
        this.organisasjonConsumer = organisasjonConsumer;
        this.kodeverkRepository = kodeverkRepository;
    }

    public Virksomhet hentOrganisasjon(String orgNummer) {
        Objects.requireNonNull(orgNummer, "orgNummer");
        var response = organisasjonConsumer.hentOrganisasjonAdresse(orgNummer);
        var adresse = response.getKorrespondanseadresse();
        var builder = new Virksomhet.Builder()
            .medOrgnr(orgNummer)
            .medNavn(response.getNavn())
            .medAdresselinje1(adresse.getAdresselinje1())
            .medAdresselinje2(adresse.getAdresselinje2())
            .medAdresselinje3(adresse.getAdresselinje3())
            .medLandkode(adresse.getLandkode())
            .medPostNr(adresse.getPostnummer())
            .medPoststed(adresse.getPoststed());
        var antaNorsk = adresse.getLandkode() == null || Landkoder.NOR.getKode().equals(adresse.getLandkode()) || "NO".equals(adresse.getLandkode());
        if (antaNorsk && adresse.getPostnummer() != null) {
            kodeverkRepository.finnPostnummer(adresse.getPostnummer()).map(Poststed::getPoststednavn).ifPresent(builder::medPoststed);
        }
        return builder.build();
    }

    public boolean validerOrganisasjon(String orgNummer) {
        try {
            return hentOrganisasjon(orgNummer) != null;
        } catch (Exception e) {
            log.warn("Kan ikke hente organisasjon for orgNummer {}", orgNummer, e);
            return false;
        }
    }

}
