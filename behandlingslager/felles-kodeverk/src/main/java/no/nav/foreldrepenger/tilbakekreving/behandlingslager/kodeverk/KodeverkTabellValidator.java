package no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk;
import java.util.Objects;

import javax.validation.ConstraintValidatorContext;


public class KodeverkTabellValidator extends KodeverkValidator<KodeverkTabell> {

    @Override
    public boolean isValid(KodeverkTabell kodeverkTabell, ConstraintValidatorContext context) {
        if(Objects.equals(null, kodeverkTabell)) {
            return true;
        }
        boolean ok = true;

        if(!gyldigKode(kodeverkTabell.getKode())) {
            context.buildConstraintViolationWithTemplate(invKode);
            ok = false;
        }

        if(!gyldigNavn(kodeverkTabell.getNavn())) {
            context.buildConstraintViolationWithTemplate(invNavn);
            ok = false;
        }


        return ok;
    }
}
