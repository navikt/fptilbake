package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import javax.enterprise.util.AnnotationLiteral;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;

/**
 * AnnotationLiteral som kan brukes i CDI søk for å skille ('disambiguate') ulike implementasjoner med samme {@link BehandlingStegRef}.
 * <p>
 * Eks. for bruk i:<br>
 * {@link CDI#current#select(javax.enterprise.util.TypeLiteral, java.lang.annotation.Annotation...)}.
 */
class BehandlingTypeAnnotationLiteral extends AnnotationLiteral<BehandlingTypeRef> implements BehandlingTypeRef {

    private String kode;
    
    BehandlingTypeAnnotationLiteral() {
        this.kode = "*";
    }
    
    BehandlingTypeAnnotationLiteral(String behandlingType) {
        this.kode = behandlingType;
    }
    
    @Override
    public String value() {
        return kode;
    }
    
}