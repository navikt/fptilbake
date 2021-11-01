alter table AKSJONSPUNKT modify (REAKTIVERING_STATUS NULL);
alter table AKSJONSPUNKT modify (REVURDERING NULL);
alter table AKSJONSPUNKT modify (MANUELT_OPPRETTET NULL);
alter table AKSJONSPUNKT drop constraint FK_AKSJONSPUNKT_1;
alter table AKSJONSPUNKT drop constraint FK_AKSJONSPUNKT_4;
drop index IDX_AKSJONSPUNKT_DEF_6;

alter table BEHANDLING_STEG_TILSTAND drop constraint FK_BEHANDLING_STEG_TILSTAND_2;
alter table BEHANDLING_TYPE_STEG_SEKV drop constraint FK_BEHANDLING_TYPE_STEG_SEK_1;
alter table TOTRINNSVURDERING drop constraint FK_TOTRINNSVURDERING_1;
alter table VURDERINGSPUNKT_DEF drop constraint FK_VURDERINGSPUNKT_DEF_1;

COMMENT ON COLUMN TOTRINNSVURDERING.AKSJONSPUNKT_DEF IS 'Kodeverdi for definisjon av aksjonspunkt';
