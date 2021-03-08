package no.nav.foreldrepenger.tilbakekreving.web.app.exceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktKode;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.feil.Feil;

public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger log = LoggerFactory.getLogger(ConstraintViolationMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Collection<FeltFeilDto> feilene = new ArrayList<>();

        Set<ConstraintViolation<?>> constraintViolations = exception.getConstraintViolations();
        for (ConstraintViolation<?> constraintViolation : constraintViolations) {
            String kode = getKode(constraintViolation.getLeafBean());
            String feltNavn = getFeltNavn(constraintViolation.getPropertyPath());
            feilene.add(new FeltFeilDto(feltNavn, constraintViolation.getMessage(), kode));
        }
        List<String> feltNavn = feilene.stream().map(FeltFeilDto::getNavn).collect(Collectors.toList());
        List<String> koder = feilene.stream().map(FeltFeilDto::getMetainformasjon).filter(Objects::nonNull).collect(Collectors.toList());

        FunksjonellException feil;
        if (koder.isEmpty()) {
            feil = FeltValideringFeil.feltverdiKanIkkeValideres(feltNavn);
        } else {
            feil = FeltValideringFeil.feltverdiKanIkkeValideres(feltNavn, koder);
        }
        log.warn(feil.getMessage());

        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new FeilDto(feil.getMessage(), feilene))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String getKode(Object leafBean) {
        return leafBean instanceof AksjonspunktKode ? ((AksjonspunktKode) leafBean).getKode() : null;
    }

    private String getFeltNavn(Path propertyPath) {
        return propertyPath instanceof PathImpl ? ((PathImpl) propertyPath).getLeafNode().toString() : null;
    }

}
