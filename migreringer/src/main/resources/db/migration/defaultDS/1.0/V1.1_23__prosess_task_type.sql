
Insert into PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE) values ('DEFAULT','Eksponentiell back-off med tak',null);
Insert into PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE) values ('ÅPNINGSTID','Åpningstidsbasert feilhåndtering','Åpningstidsbasert feilhåndtering. INPUT_VARIABEL1 = åpningstid og INPUT_VARIABEL2 = stengetid');


Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,BESKRIVELSE) values ('behandlingskontroll.fortsettBehandling','Forsetter automatisk behandling.','3','60','Task som melder om kjører automatisk behandling for behandlingskontroll.  Starter i det steget behandlingen står og forsetter til den stopper på et Aksjonspunkt.');
Insert into PROSESS_TASK_TYPE (KODE,NAVN,FEIL_MAKS_FORSOEK,FEIL_SEK_MELLOM_FORSOEK,BESKRIVELSE) values ('behandlingskontroll.gjenopptaBehandling','Gjenoppta behandling','3','30','Gjenoppta behandling som har åpent aksjonspunkt (auto) som har passert fristen');
