package no.nav.foreldrepenger.tilbakekreving.historikkv2;

public enum FeltType {
    INNVILGET("INNVILGET", "Oppfylt"),
    AVSLÅTT("AVSLÅTT", "Ikke oppfylt"),
    INNVILGET_UTTAK_AV_KVOTE("2003", "Innvilget uttak av kvote"),
    VILKAR_OPPFYLT("VILKAR_OPPFYLT", "Vilkåret er oppfylt"),
    GYLDIG_UTSETTELSE_PGA_ARBEID("2011", "Gyldig utsettelse pga 100% arbeid"),
    ARBEIDSTAKER("ARBEIDSTAKER", "Arbeidstaker"),
    INNVILGET_FELLESPERIODE_FOREDREPENGER("2002", "Innvilget fellesperiode/foreldrepenger"),
    GRADERING_AV_KVOTE("2031", "Gradering av kvote/overført kvote"),
    FEDREKVOTE("FEDREKVOTE", "Fedrekvote"),
    FELLESPERIODE("FELLESPERIODE", "Fellesperiode"),
    REDUSERT_UTTAKSGRAD("2038", "Redusert uttaksgrad pga. den andre forelderens uttak"),
    LOVLIG_OPPHOLD("LOVLIG_OPPHOLD", "Søker har lovlig opphold"),
    MØDREKVOTE("MØDREKVOTE", "Mødrekvote"),
    IKKE_STØNADSDAGER_IGJEN("4002", "Ikke stønadsdager igjen"),
    GRADERING_OPPFYLT("GRADERING_OPPFYLT", "Oppfylt"),
    INNVILGET_FORELDREPENGER_KUN_FAR_HAR_RETT("2004", "Innvilget foreldrepenger, kun far har rett"),
    GRADERING_AV_FELLESPERIODE_FORELDREPENGER("2030", "Gradering av fellesperiode/foreldrepenger"),
    ANNEN_FORELDER_HAR_IKKE_RETT("ANNEN_FORELDER_HAR_IKKE_RETT", "Annen forelder har ikke rett"),
    TILBAKEKR_OPPRETT("TILBAKEKR_OPPRETT", "Opprett tilbakekreving"),
    FASTSETT_RESULTAT_PERIODEN_AVKLARES_IKKE("FASTSETT_RESULTAT_PERIODEN_AVKLARES_IKKE", "Perioden kan ikke avklares"),
    FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT("FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT", "Sykdommen/skaden er dokumentert"),
    HAR_GYLDIG_GRUNN("HAR_GYLDIG_GRUNN", "Gyldig grunn for sen fremsetting av søknaden"),
    BRUK_MED_OVERSTYRTE_PERIODER("BRUK_MED_OVERSTYRTE_PERIODER", "Bruk arbeidsforholdet med overstyrt periode"),
    GRADERING_IKKE_OPPFYLT("GRADERING_IKKE_OPPFYLT", "Ikke oppfylt"),
    GYLDIG_UTSETTELSE_PGA_FERIE("2010", "Gyldig utsettelse pga ferie"),
    IKKE_BENYTT("IKKE_BENYTT", "Ikke benytt"),
    VARIG_ENDRET_NAERING("VARIG_ENDRET_NAERING", "Varig endret eller nystartet næring"),
    INGEN_INNVIRKNING("INGEN_INNVIRKNING", "Faresignalene hadde ingen innvirkning på behandlingen"),
    INNVILGET_FELLES_PERIODE_TIL_FAR("2037", "Innvilget fellesperiode til far"),
    VILKAR_IKKE_OPPFYLT("VILKAR_IKKE_OPPFYLT", "Vilkåret er ikke oppfylt"),
    FORTSETT_UTEN_INNTEKTSMELDING("FORTSETT_UTEN_INNTEKTSMELDING", "Gå videre uten inntektsmelding"),
    MANGLENDE_OPPLYSNINGER("MANGLENDE_OPPLYSNINGER", "Benytt i behandlingen, men har manglende opplysninger"),
    IKKE_OPPFYLT("IKKE_OPPFYLT", "ikke oppfylt"),
    BENYTT("BENYTT", "Benytt"),
    KONTAKT_ARBEIDSGIVER_VED_MANGLENDE_INNTEKTSMELDING("KONTAKT_ARBEIDSGIVER_VED_MANGLENDE_INNTEKTSMELDING", "Arbeidsgiver kontaktes"),
    FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT("FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT", "Innleggelsen er dokumentert"),
    TILBAKEKR_IGNORER("TILBAKEKR_IGNORER", "Avvent samordning, ingen tilbakekreving"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SELVSTENDIG_NÆRINGSDRIVENDE", "Selvstendig næringsdrivende"),
    UTSETTELSE_PGA_ARBEID_KUN_FAR_HAR_RETT("2016", "Utsettelse pga. 100% arbeid, kun far har rett"),
    GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT("2033", "Gradering foreldrepenger, kun far har rett"),
    ANNEN_PART_HAR_OVERLAPPENDE_UTTAK("4084", "Annen part har overlappende uttak, det er ikke søkt/innvilget samtidig uttak"),
    INNTEKT_IKKE_MED_I_BG("INNTEKT_IKKE_MED_I_BG", "Benytt i behandligen. Inntekten er ikke med i beregningsgrunnlaget"),
    OVERFØRING_OPPFYLT_ANNEN_PART_AVHENGIG_AV_HJELP("2021", "Overføring oppfylt, annen part er helt avhengig av hjelp til å ta seg av barnet"),
    OPPFYLT("OPPFYLT", "oppfylt"),
    SØKER_ER_IKKE_I_PERMISJON("SØKER_ER_IKKE_I_PERMISJON", "Søker er ikke i permisjon"),
    BOSATT_I_NORGE("BOSATT_I_NORGE", "Søker er bosatt i Norge"),
    INNVIRKNING("INNVIRKNING", "Faresignalene hadde innvirkning på behandlingen"),
    OPPHOLDSRETT("OPPHOLDSRETT", "Søker har ikke oppholdsrett (EØS)"),
    INNVILGET_FORELDREPENGER_FAR_HAR_RETT_MOR_UFØR("2036", "Innvilget foreldrepenger, kun far har rett og mor er ufør"),
    BOSATT_UTLAND("BOSATT_UTLAND", "Bosatt utland"),
    SØKER_ER_I_PERMISJON("SØKER_ER_I_PERMISJON", "Søker er i permisjon"),
    IKKE_BOSATT_I_NORGE("IKKE_BOSATT_I_NORGE", "Søker er ikke bosatt i Norge"),
    FRILANSER("FRILANSER", "Frilanser"),
    INNVILGET_FORELDREPENGER_FOER_FØDSEL("2006", "Innvilget foreldrepenger før fødsel"),
    ANNEN_FORELDER_HAR_RETT("ANNEN_FORELDER_HAR_RETT", "Annen forelder har rett"),
    SAMTIDIG_UTTAK_IKKE_GYLDIG_KOMBINASJON("4060", "Samtidig uttak - ikke gyldig kombinasjon"),
    DAGPENGER("DAGPENGER", "Dagpenger"),
    INGEN_VARIG_ENDRING_NAERING("INGEN_VARIG_ENDRING_NAERING", "Ingen varig endret eller nyoppstartet næring"),
    IKKE_BRUK("IKKE_BRUK", "Ikke bruk"),
    IKKE_TIDSBEGRENSET_ARBEIDSFORHOLD("IKKE_TIDSBEGRENSET_ARBEIDSFORHOLD", "ikke tidsbegrenset"),
    AVSLAG_OVERFØRING_AV_KVOTE_PGA_ANNEN_FORELDRE_HAR_RETT_PÅ_FP("4076", "Avslag overføring av kvote pga. annen forelder har rett til foreldrepenger"),
    HINDRE_TILBAKETREKK("HINDRE_TILBAKETREKK", "Ikke tilbakekrev fra søker"),
    AVSLAG_UTSETTELSE_INGEN_STØNADSDAGER_IGJEN("4034", "Avslag utsettelse - ingen stønadsdager igjen"),
    FORELDREPENGER_FØR_FØDSEL("FORELDREPENGER_FØR_FØDSEL", "Foreldrepenger før fødsel"),
    I_AKTIVITET("I_AKTIVITET", "Mor er i aktivitet"),
    IKKE_I_AKTIVITET_DOKUMENTERT("IKKE_I_AKTIVITET_DOKUMENTERT", "Mor er ikke i aktivitet"),
    IKKE_I_AKTIVITET_IKKE_DOKUMENTERT("IKKE_I_AKTIVITET_IKKE_DOKUMENTERT", "Aktiviteten er ikke dokumentert"),
    BENYTT_A_INNTEKT_I_BG("BENYTT_A_INNTEKT_I_BG", "Benytt i behandlingen. Inntekt fra A-inntekt benyttes i beregningsgrunnlaget"),
    HULL_MELLOM_STØNADSPERIODER("4005", "Hull mellom stønadsperioder"),
    IKKE_NY_I_ARBEIDSLIVET("IKKE_NY_I_ARBEIDSLIVET", "ikke ny i arbeidslivet"),
    AKTIVITETSKRAVET_ARBEID_IKKE_OPPFYLT("4050", "Aktivitetskravet arbeid ikke oppfylt"),
    AVSLAG_UTSETTELSE_PGA_ARBEID_TILBAKE_I_TID("4082", "Avslag utsettelse pga arbeid tilbake i tid"),
    TIDSBEGRENSET_ARBEIDSFORHOLD("TIDSBEGRENSET_ARBEIDSFORHOLD", "tidsbegrenset"),
    FAR_ALENEOMSORG_MOR_FYLLER_IKKE_AKTIVITETSKRAVET("4035", "Far aleneomsorg, mor fyller ikke aktivitetskravet"),
    AKTIVITETSKRAVET_OFFENTLIG_GODKJENT_UTDANNING_IKKE_OPPFYLT("4051", "Aktivitetskravet offentlig godkjent utdanning ikke oppfylt"),
    HAR_IKKE_GYLDIG_GRUNN("HAR_IKKE_GYLDIG_GRUNN", "Ingen gyldig grunn for sen fremsetting av søknaden"),
    EØS_BOSATT_NORGE("EØS_BOSATT_NORGE", "EØS bosatt Norge"),
    AKTIVITETSKRAV_ARBEID_IKKE_DOKUMENTERT("4066", "Aktivitetskrav- arbeid ikke dokumentert"),
    AKTIVITETSKRAV_UTDANNING_IKKE_DOKUMENTERT("467", "Aktivitetskrav – utdanning ikke dokumentert"),
    FASTSETT_RESULTAT_GRADERING_AVKLARES("FASTSETT_RESULTAT_GRADERING_AVKLARES", "Perioden er ok"),
    OVERFØRING_OPPFYLT_ANNEN_PART_HAR_IKKE_RETT_TIL_FP("2020", "Overføring oppfylt, annen part har ikke rett til foreldrepengene"),
    GYLDIG_UTSETTELSE("2024", "Gyldig utsettelse"),
    BARNETS_INNLEGGELSE_IKKE_OPPFYLT("4040", "Barnets innleggelse ikke oppfylt"),
    NY_I_ARBEIDSLIVET("NY_I_ARBEIDSLIVET", "ny i arbeidslivet"),
    GYLDIG_UTSETTELSE_PGA_BARN_INNLAGT("2013", "Gyldig utsettelse pga barn innlagt"),
    OVERFØRING_OPPFYLT_SØKER_HAR_ALENEOMSORG_FOR_BARNET("2023", "Overføring oppfylt, søker har aleneomsorg for barnet"),
    INNVILGET_FORELDREPENGER_KUN_MOR_HAR_RETT("2007", "Innvilget foreldrepenger, kun mor har rett"),
    BRUDD_PÅ_SØKNADSFRIST("4020", "Brudd på søknadsfrist"),
    GYLDIG_UTSETTELSE_PGA_SYKDOM("2014", "Gyldig utsettelse pga sykdom"),
    ARBEIDER_I_UTTAKSPERIODEN_MER_ENN_NULL_PROSENT("4023", "Arbeider i uttaksperioden mer enn 0%"),
    ANNEN_PART_HAR_OVERLAPPENDE_UTTAKSPERIODER_SOM_ER_INNVILGET_UTSETTELSE("4086", "Annen part har overlappende uttaksperioder som er innvilget utsettelse"),
    FASTSETT_RESULTAT_ENDRE_SOEKNADSPERIODEN("FASTSETT_RESULTAT_ENDRE_SOEKNADSPERIODEN", "Endre søknadsperioden"),
    UTFØR_TILBAKETREKK("UTFØR_TILBAKETREKK", "Tilbakekrev fra søker"),
    FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT_IKKE("FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT_IKKE", "Sykdommen/skaden er ikke dokumentert"),
    NASJONAL("NASJONAL", "NASJONAL"),
    AKTIVITETSKRAVET_MORS_SYKDOM_SKADE_IKKE_OPPFYLT("4053", "Aktivitetskravet mors sykdom/skade ikke oppfylt"),
    BARE_FAR_RETT_AKTIVITETSKRAVET_OPPPFYLT("2028", "Bare far rett, aktivitetskravet oppfylt"),
    FAR_HAR_IKKE_OMSORG("4012", "Far har ikke omsorg"),
    IKKE_GRADERING_PGA_FOR_SEN_SØKNAD("4501", "Ikke gradering pga. for sen søknad"),
    UTSETTELSE_PGA_FERIE_KUN_FAR_HAR_RETT("2015", "Utsettelse pga. ferie, kun far har rett"),
    IKKE_LOVLIG_OPPHOLD("IKKE_LOVLIG_OPPHOLD", "Søker har ikke lovlig opphold"),
    GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT("2034", "Gradering foreldrepenger, kun mor har rett"),
    ARBEIDSAVKLARINGSPENGER("ARBEIDSAVKLARINGSPENGER", "Arbeidsavklaringspenger"),
    INNVILGET_FORELDREPENGER_VED_ALENEOMSORG("2005", "Innvilget foreldrepenger ved aleneomsorg"),
    AVSLAG_GRADERING_IKKE_RETT_TIL_GRADERT_UTTAK("4503", "Avslag gradering - ikke rett til gradert uttak pga. redusert oppfylt aktivitetskrav på mor"),
    IKKE_HELTIDSARBEID("4037", "Ikke heltidsarbeid"),
    BARE_FAR_HAR_RETT_MANGLER_SØKNAD_UTTAK_AKTIVITETSKRAV("4102", "Bare far har rett, mangler søknad uttak/aktivitetskrav"),
    FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT_IKKE("FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT_IKKE", "Innleggelsen er ikke dokumentert"),
    AVSLAG_UTSETTELSE_FØR_TERMIN_FØDSEL("4030", "Avslag utsettelse før termin/fødsel"),
    UTSETTELSE_FERIE_IKKE_DOKUMENTERT("4061", "Utsettelse ferie ikke dokumentert"),
    IKKE_OPPRETT_BASERT_PÅ_INNTEKTSMELDING("IKKE_OPPRETT_BASERT_PÅ_INNTEKTSMELDING", "Ikke opprett arbeidsforhold"),
    AKTIVITETSKRAV_SYKDOM_SKADE_IKKE_DOKUMENTERT("4069", "Aktivitetskrav – sykdom/skade ikke dokumentert"),
    DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT("4007", "Den andre part syk/skadet ikke oppfylt"),
    GRADERING_FP_KUN_FAR_HAR_RETT_DAGER_UTEN_AKTIVITETSKRAV("2035", "Gradering foreldrepenger, kun far har rett - dager uten aktivitetskrav"),
    IKKE_ALENEOMSORG("IKKE_ALENEOMSORG", "Søker har ikke aleneomsorg for barnet"),
    INNVILGET_PREMATURUKER_MED_FRATREKK_PLEIEPENGER("4077", "Innvilget prematuruker, med fratrekk pleiepenger"),
    BARNET_ER_OVER_TRE_ÅR("4022", "Barnet er over 3 år"),
    FORTSETT_BEHANDLING("FORTSETT_BEHANDLING", "Fortsett behandling"),
    AVSLAG_OVERFØRING_AV_KVOTE_PGA_SYKDOM_SKADE_IKKE_DOKUMENTERT("4074", "Avslag overføring av kvote pga. sykdom/skade/innleggelse er ikke dokumentert"),
    UTSETTELSE_ARBEID_IKKE_DOKUMENTERT("4062", "Utsettelse arbeid ikke dokumentert"),
    JORDBRUKER("JORDBRUKER", "Selvstendig næringsdrivende - Jordbruker"),
    ALENEOMSORG("ALENEOMSORG", "Søker har aleneomsorg for barnet"),
    AVSLAG_GRADERING_ARBEID_HUNDRE_PROSENT_ELLER_MER("4025", "Avslag gradering - arbeid 100% eller mer"),
    IKKE_NYOPPSTARTET("IKKE_NYOPPSTARTET", "ikke nyoppstartet"),
    AVSLAG_OVERFØRING_HAR_IKKE_ALENEOMSORG_FOR_BARNET("4092", "Avslag overføring - har ikke aleneomsorg for barnet"),
    UTSETTELSE_SØKERS_SYKDOM_SKADE_IKKE_DOKUMENTERT("4063", "Utsettelse søkers sykdom/skade ikke dokumentert"),
    AKTIVITETSKRAVET_MORS_DELTAKELSE_PÅ_INTRODUKSJONSPROGRAM_IKKE_OPPFYLT("4055", "Aktivitetskravet mors deltakelse på introduksjonsprogram ikke oppfylt"),
    BARNET_ER_DØD("4072", "Barnet er dødt"),
    IKKE_RETT_TIL_KVOTE_FORDI_MOR_IKKE_RETT_TIL_FP("4073", "Ikke rett til kvote fordi mor ikke har rett til foreldrepenger"),
    SØKERS_SYKDOM_SKADE_IKKE_OPPFYLT("4038", "Søkers sykdom/skade ikke oppfylt"),
    BOSA("BOSA", "Bosatt"),
    IKKE_SAMTYKKE_MELLOM_PARTENE("4085", "Det er ikke samtykke mellom partene"),
    IKKE_LOVBESTEMT_FERIE("4033", "Ikke lovbestemt ferie"),
    GRADERING_FP_VED_ALENEOMSORG("2032", "Gradering foreldrepenger ved aleneomsorg"),
    MOR_TAR_IKKE_ALLE_TRE_UKENE_FØR_TERMIN("4095", "Mor tar ikke alle 3 ukene før termin"),
    NYOPPSTARTET("NYOPPSTARTET", "nyoppstartet"),
    OPPHØR_AV_MEDLEMSKAP("4087", "Opphør av medlemskap"),
    MEDLEM("MEDLEM", "Periode med medlemskap"),
    UTSETTELSE_PGA_SYKDOM_SKADE_KUN_FAR_HAR_RETT("2017", "Utsettelse pga. sykdom, skade, kun far har rett"),
    STØNADSPERIODE_FOR_NYTT_BARN("4104", "Stønadsperiode for nytt barn"),
    LAGT_TIL_AV_SAKSBEHANDLER("LAGT_TIL_AV_SAKSBEHANDLER", "Arbeidsforholdet er lagt til av saksbehandler"),
    ARBEIDSTAKER_UTEN_FERIEPENGER("ARBEIDSTAKER_UTEN_FERIEPENGER", "Arbeidstaker uten feriepenger"),
    AVSLAG_UTSETTELSE_PGA_FERIE_TILBAKE_I_TID("4081", "Avslag utsettelse pga ferie tilbake i tid"),
    OPPRETT_BASERT_PÅ_INNTEKTSMELDING("OPPRETT_BASERT_PÅ_INNTEKTSMELDING", "Opprettet basert på inntektsmeldingen"),
    GYLDIG_UTSETTELSE_FØRSTE_SEKS_UKERS_PGA_BARN_INNLAGT("2026", "Gyldig utsettelse første 6 uker pga. barn innlagt"),
    FERIE_INNENFOR_DE_FØRSTE_SEKS_UKENE("4031", "Ferie innenfor de første 6 ukene"),
    AKTIVITETSKRAVET_OFFENTLIG_GODKJENT_UTDANNING_I_MED_ARBEID_IKKE_OPPFYLT("4052", "Aktivitetskravet offentlig godkjent utdanning i kombinasjon med arbeid ikke oppfylt"),
    UTSETTELSE_PGA_BARNETS_INNLEGGELSE_PÅ_HELSEINSTITUSJON_KUN_FAR_HAR_RETT("2019", "Utsettelse pga. barnets innleggelse på helseinstitusjon, kun far har rett"),
    AKTIVITETSKRAVET_MORS_DELTAKELSE_PÅ_KVALIFISERINGSPROGRAM_IKKE_OPPFYLT("4056", "Aktivitetskravet mors deltakelse på kvalifiseringsprogram ikke oppfylt"),
    BARNETS_INNLEGGELSE_FØRSTE_SEKS_UKER_IKKE_OPPFYLT("4112", "Barnets innleggelse første 6 uker ikke oppfylt"),
    SJØMANN("SJØMANN", "Arbeidstaker - Sjømann"),
    INNVILGET_FØRSTE_SEKS_UKER_ETTER_FØDSEL("2039", "Innvilget første 6 uker etter fødsel"),
    AVSLAG_UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG("4041", "Avslag utsettelse ferie på bevegelig helligdag"),
    IKKE_OPPHOLDSRETT("IKKE_OPPHOLDSRETT", "Søker har ikke oppholdsrett (EØS)"),
    OVERFØRING_OPPFYLT_ANNEN_PART_ER_INNLAGT_I_HELSEINSTITUSJON("2022", "Overføring oppfylt, annen part er innlagt i helseinstitusjon"),
    FASTSETT_RESULTAT_PERIODEN_HV_DOKUMENTERT("FASTSETT_RESULTAT_PERIODEN_HV_DOKUMENTERT", "Øvelse eller tjeneste i heimevernet er dokumentert"),
    OPPHØR_AV_FORELDREANSVARVILKÅRET("4098", "Opphør av foreldreansvarvilkåret"),
    UTSETTELSE_BARNETS_INNLEGGELSE_BARNETS_INNLEGGELSE_IKKE_DOKUMENTERT("4065", "Utsettelse barnets innleggelse - barnets innleggelse ikke dokumentert"),
    GYLDIG_UTSETTELSE_FØRSTE_SEKS_UKER_PGA_SYKDOM("2027", "Gyldig utsettelse første 6 uker pga. sykdom"),
    UTVA("UTVA", "Utvandret"),
    AVSLAG_GRADERING_SØKER_ER_IKKE_I_ARBEID("4093", "Avslag gradering - søker er ikke i arbeid"),
    AKTIVITETSKRAV_ARBEID_I_KOMBINASJON_MED_UTDANNING_IKKE_DOKUMENTERT("4068", "Aktivitetskrav – arbeid i kombinasjon med utdanning ikke dokumentert"),
    MANUELT_OPPRETTET_AV_SAKSBEHANDLER("MANUELT_OPPRETTET_AV_SAKSBEHANDLER", "Opprettet av saksbehandler"),
    AVSLAG_GRADERINGSAVTALE_MANGLER_IKKE_DOKUMENTERT("4502", "Avslag graderingsavtale mangler - ikke dokumentert"),
    GYLDIG_UTSETTELSE_PGA_INNLEGGELSE("2012", "Gyldig utsettelse pga innleggelse"),
    MOR_HAR_IKKE_OMSORG("4003", "Mor har ikke omsorg"),
    UNNTAK_FOR_AKTIVITETSKRAVET_MORS_MOTTAK_AV_UFØRETRYGD_IKKE_OPPFYLT("4057", "Unntak for aktivitetskravet, mors mottak av uføretrygd ikke oppfylt"),
    FASTSETT_RESULTAT_PERIODEN_NAV_TILTAK_DOKUMENTERT("FASTSETT_RESULTAT_PERIODEN_NAV_TILTAK_DOKUMENTERT", "Tiltak i regi av NAV er dokumentert"),
    AKTIVITETSKRAV_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT("4088", "Aktivitetskrav – introduksjonsprogram ikke dokumentert"),
    FISKER("FISKER", "Selvstendig næringsdrivende - Fisker"),
    AVSLAG_GRADERING_ARBEID_HUNDRE_PROSENT_ELLER_MER_4523("4523", "Avslag gradering - arbeid 100% eller mer"),
    PRAKSIS_UTSETTELSE("PRAKSIS_UTSETTELSE", "Feil praksis utsettelse"),
    IKKE_NOK_DAGER_UTEN_AKTIVITETSKRAV("4107", "Ikke nok dager uten aktivitetskrav"),
    AVSLAG_GRADERING_GRADERING_FØR_UKE_SJU("4504", "Avslag gradering - gradering før uke 7"),
    GRADERING_PÅ_ANDEL_UTEN_BG_IKKE_SATT_PÅ_VENT("GRADERING_PÅ_ANDEL_UTEN_BG_IKKE_SATT_PÅ_VENT", "Riktig"),
    FASTSETT_RESULTAT_PERIODEN_NAV_TILTAK_DOKUMENTERT_IKKE("FASTSETT_RESULTAT_PERIODEN_NAV_TILTAK_DOKUMENTERT_IKKE", "Tiltak i regi av NAV er ikke dokumentert"),
    KONTAKT_ARBEIDSGIVER_VED_MANGLENDE_ARBEIDSFORHOLD("KONTAKT_ARBEIDSGIVER_VED_MANGLENDE_ARBEIDSFORHOLD", "Arbeidsgiver kontaktes"),
    UTTAK_FØR_OMSORGOVERTAKELSE("4100", "Uttak før omsorgsovertakelse"),
    IKKE_RETT_TIL_FELLESPERIODE_MOR_IKKE_RETT_TIL_FP("4075", "Ikke rett til fellesperiode fordi mor ikke har rett til foreldrepenger"),
    IKKE_RELEVANT("IKKE_RELEVANT", "Ikke relevant periode"),
    SØKERS_SYKDOM_SKADE_FØRSTE_SEKS_UKER_IKKE_DOKUMENTERT("4115", "Søkers sykdom/skade første 6 uker ikke dokumentert"),
    MANGLER_SØKNAD_FOR_FØRSTE_SEKS_UKER_ETTER_FØDSEL("4103", "Mangler søknad for første 6 uker etter fødsel"),
    OPPHØR_AV_OPPTJENINGSVILKÅRET("4099", "Opphør av opptjeningsvilkåret"),
    AKTIVITETSKRAV_KVALIFISERINGSPROGRAMMET_IKKE_DOKUMENTERT("4089", "Aktivitetskrav – kvalifiseringsprogrammet ikke dokumentert"),
    FAR_MEDMOR_SØKER_MER_ENN_TI_DAGER_IFM_FØDSEL("4016", "Far/medmor søker mer enn 10 dager ifm fødsel"),
    BARNETS_INNLEGGELSE_FØRSTE_SEKS_UKER_IKKE_DOKUMENTERT("4117", "Barnets innleggelse første 6 uker ikke dokumentert"),
    AKTIVITETSKRAV_INNLEGGELSE_IKKE_DOKUMENTERT("4070", "Aktivitetskrav – innleggelse ikke dokumentert"),
    FERIE_SELVSTENDIG_NÆRINGSDRIVENDE_FRILANSER("4032", "Ferie - selvstendig næringsdrivende/frilanser"),
    SØKERS_SYKDOM_SKADE_FØRSTE_SEKS_UKER_IKKE_OPPFYLT("4110", "Søkers sykdom/skade første 6 uker ikke oppfylt"),
    OPPFØR_AV_FØDSELSVILKÅRET("4096", "Opphør av fødselsvilkåret"),
    FORELDREANSVAR_4_TITTEL("FORELDREANSVAR_4_TITTEL", "Foreldreansvarsvilkåret § 14-17 fjerde ledd"),
    UTSETTELSE_PGA_EGEN_INNLEGGELSE_PÅ_HELSEINNSTITUSJON_KUN_FAR_HAR_RETT("2018", "Utsettelse pga. egen innleggelse på helseinstitusjon, kun far har rett"),
    DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT("4008", "Den andre part innleggelse ikke oppfylt"),
    FJERN_FRA_BEHANDLINGEN("FJERN_FRA_BEHANDLINGEN", "Fjernet fra behandlingen"),
    AKTIVITETSKRAVET_MORS_INNLEGGELSE_IKKE_OPPFYLT("4054", "Aktivitetskravet mors innleggelse ikke oppfylt"),
    OPPHØR_AV_ADOPSJONSVILKÅRET("4097", "Opphør av adopsjonsvilkåret"),
    UTSETTELSE_SØKERS_INNLEGGELSE_IKKE_DOKUMENTERT("4064", "Utsettelse søkers innleggelse ikke dokumentert"),
    DAGMAMMA("DAGMAMMA", "Selvstendig næringsdrivende - Dagmamma"),
    DOKUMENTERT("DOKUMENTERT", "dokumentert"),
    GYLDIG_UTSETTELSE_FØRSTE_SEKS_UKER_PGA_INNLEGGELSE("2025", "Gyldig utsettelse første 6 uker pga. innleggelse"),
    UNNTAKT_FOR_AKTIVITETSKRAVET_FLERBARNSDAGER_IKKE_NOK_DAGER("4059", "Unntak for aktivitetskravet, flerbarnsdager - ikke nok dager"),
    IKKE_EKTEFELLES_BARN("IKKE_EKTEFELLES_BARN", "ikke ektefelles barn"),
    FAR_MEDMOR_SØKER_UNNTAK_FØR_FØDSEL_OMSORG("4105", "Far/medmor søker uttak før fødsel/omsorg"),
    HENLEGG_BEHANDLING("HENLEGG_BEHANDLING", "Henlegg behandling"),
    SAMMENSATT_KONTROLL("SAMMENSATT_KONTROLL", "Sammensatt kontroll"),
    NYTT_ARBEIDSFORHOLD("NYTT_ARBEIDSFORHOLD", "Arbeidsforholdet er ansett som nytt"),
    MOR_SØKER_FELLESPERIODE_FØR_TOLV_UKERS_TERMIN_FØDSEL("4013", "Mor søker fellesperiode før 12 uker før termin/fødsel"),
    AKTIVITETSKRAV_INTRODUKSJONSPROGRAM_IKKE_DOKUMENTERT_4071("4071", "Aktivitetskrav – introduksjonsprogram ikke dokumentert"),
    ADOPTERER_IKKE_ALENE("ADOPTERER_IKKE_ALENE", "adopterer ikke alene"),
    IKKE_DOKUMENTERT("IKKE_DOKUMENTERT", "ikke dokumentert"),
    EKTEFELLES_BARN("EKTEFELLES_BARN", "ektefelles barn"),
    SØKERS_INNLEGGELSE_IKKE_OPPFYLT("4039", "Søkers innleggelse ikke oppfylt"),
    SØKERS_INNLEGGELSE_FØRSTE_SEKS_UKER_IKKE_OPPFYLT("4111", "Søkers innleggelse første 6 uker ikke oppfylt"),
    FORELDREANSVAR_2_TITTEL("FORELDREANSVAR_2_TITTEL", "Foreldreansvarsvilkåret § 14-17 andre ledd"),
    UNNTAK("UNNTAK", "Perioder uten medlemskap"),
    UNNTAK_FOR_AKTIVITETSKRAV_STEBARNSADOPSJON_IKKE_NOK_DAGER("4058", "Unntak for aktivitetskravet, stebarnsadopsjon - ikke nok dager"),
    SØKERS_INNLEGGELSE_FØRSTE_SEKS_UKER_IKKE_DOKUMENTERT("4116", "Søkers innleggelse første 6 uker ikke dokumentert"),
    OMSORGSVILKARET_TITTEL("OMSORGSVILKARET_TITTEL", "Omsorgsvilkår § 14-17 tredje ledd"),
    ADOPTERER_ALENE("ADOPTERER_ALENE", "adopterer alene"),
    FASTSETT_RESULTAT_PERIODEN_HV_DOKUMENTERT_IKKE("FASTSETT_RESULTAT_PERIODEN_HV_DOKUMENTERT_IKKE", "Øvelse eller tjeneste i heimevernet er ikke dokumentert"),
    VERGE("VERGE", "Verge/fullmektig"),
    MANUELL_BEHANDLING("MANUELL_BEHANDLING", "Manuell behandling"),
    FORELDREPENGER("FORELDREPENGER", "Foreldrepenger"),
    AKTIVITETSKRAVET_UTDANNING_IKKE_DOKUMENTERT("4067", "Aktivitetskrav – utdanning ikke dokumentert"),
    ADNR("ADNR", "Aktivt"),
    DØD("DØD", "Død"),
    FOSV("FOSV", "Forsvunnet/savnet"),
    FØDR("FØDR", "Fødselsregistrert"),
    UREG("UREG", "Uregistrert person"),
    UTPE("UTPE", "Utgått person"),
    UTAN("UTAN", "Utgått person annullert tilgang Fnr"),
    IKKE_VURDERT("IKKE_VURDERT", "Ikke vurdert"),
    INNTREKK("TILBAKEKR_INNTREKK", "Feilutbetaling hvor inntrekk dekker hele beløpet"),
    FAR_MER_ENN_TI_DAGER_FEDREKVOTE_IFM_FØDSEL("4106", "Far/medmor søker mer enn 10 dager ifm fødsel"),
    SELVSTENDIG_NÆRING("SELVSTENDIG_NÆRING", "Næringsdrivende"),
    DØD_DØDFØDSEL("DØD_DØDFØDSEL", "Død eller dødfødsel"),
    SOEKER("SOEKER", "Søker"),
    UDEFINERT("EN_UGYLDIG_VERDI_ØNSKER_Å_LOGGE_UDEFINERTE_VERDIER_OGSÅ", "UDEFINERT VERDI");

    private final String key;
    private final String text;

    FeltType(String key, String text) {
        this.key = key;
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }

    public static FeltType getByKey(String key) {
        for (FeltType feltType : values()) {
            if (feltType.getKey().equals(key)) {
                return feltType;
            }
        }

        throw new IllegalArgumentException("FeltType with key " + key + " not found");
    }
}
