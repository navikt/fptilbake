package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record XacmlResponse(@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("Response") List<Result> response) {

    public static record Result(@JsonProperty("Decision") Decision decision,
                                @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("Obligations") List<ObligationOrAdvice> obligations,
                                @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("AssociatedAdvice") List<ObligationOrAdvice> associatedAdvice) {
    }

    public static record ObligationOrAdvice(@JsonProperty("Id") String id,
                                            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("AttributeAssignment") List<AttributeAssignment> attributeAssignment) {
    }

    public static record AttributeAssignment(@JsonProperty("AttributeId") String attributeId, @JsonProperty("Value") Object value) {
    }

}
