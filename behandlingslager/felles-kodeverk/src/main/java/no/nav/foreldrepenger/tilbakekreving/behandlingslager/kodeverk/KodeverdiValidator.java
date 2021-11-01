package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import java.util.Objects;

import javax.validation.ConstraintValidatorContext;


public class KodeverdiValidator extends KodeverkValidator<Kodeverdi> {

    @Override
    public boolean isValid(Kodeverdi kodeliste, ConstraintValidatorContext context) {
        if (Objects.equals(null, kodeliste)) {
            return true;
        }
        var ok = true;

        if (!gyldigKode(kodeliste.getKode())) {
            context.buildConstraintViolationWithTemplate(invKode);
            ok = false;
        }

        if (!gyldigKodeverk(kodeliste.getKodeverk())) {
            context.buildConstraintViolationWithTemplate(invNavn);
            ok = false;
        }

        return ok;
    }
}
