package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;

import java.util.Objects;

import javax.validation.ConstraintValidatorContext;

public class KodelisteValidator extends KodeverkValidator<Kodeliste> {

    @Override
    public boolean isValid(Kodeliste kodeliste, ConstraintValidatorContext context) {
        if (Objects.equals(null, kodeliste)) {
            return true;
        }
        boolean ok = true;

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
