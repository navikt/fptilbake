# fptilbake

Case processing application for repayments after benefits have been paid and then changed.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

Repository builds two deployments from the same codebase:
- `fptilbake` for foreldrepenger, svangerskapspenger, engangsstønad (Team Foreldrepenger) 
- `k9-tilbake` for pleiepenger, omsorgspenger, opplæringspenger (Team Sykdom i familien)

Consider impact on both deployments when changing shared code.

## Repo-specific context

| Topic             | Details                                                            |
|-------------------|--------------------------------------------------------------------|
| Role              | Case processing from identified repayment to vedtak/dismissal      |
| Consumers         | `fp-frontend`, `fp-los`, `fp-oversikt`, K9                         |
| Tech stack        | Standard fp Java backend using `fp-prosesstask`                    |
| Data              | Oracle; FSS deployment; long-term storage of behandling and vedtak |

Oppdragssystemet (OS) handles the payment balance for clients and is external to the team. 
- the main trigger is kravgrunnlag from OS over JMS/MQ
- the seconadry trigger is that simulation in `fp-sak` indicates feilutbetaling (possible repayment) and a saksbehandler selects to create tilbakekreving with a notice (varsel)

Direct relations/integrations:
- Upstream: `fp-sak` (create tilbakekreving), OS (kravgrunnlag)
- Frontend: `fp-frontend` (saksbehandler), `fp-swagger` (admin)
- Satellites: `fpoppdrag` (data for notice letter), `fp-sak` (text for notice letter)
- Downstream: `fp-oversikt`, `fp-ws-proxy` to OS, Joark
- Parallel: `fp-los`
- Main data sources: PDL, EREG, Joark
- Data warehouse: Producer for Kafka topics on behandling and vedtak

## Domain model

- `KravgrunnlagAggregate` = Java representation of a kravgrunnlag which can be active or suspended (pending update from OS)  
- `VarselInfo` = Text for early notice letter when simulation indicates possible tilbakekreving                              
- `Behandling` = a unit of case processing of repayment claim from identification to vedtak/dismissal
- `BehandlingModell` = pre-defined pipeline (behandlingsprosess) for a benefit and behandling type. See `ForeldrepengerModellProducer`and similar for ES/SVP
- `BehandlingSteg` = pipeline stage with implementation
- `Aksjonspunkt` = saksbehandler decision point
- `BehandlingVedtak` =  vedtak overall outcome, related to decision letter and structured vedtak sent to OS via `fp-ws-proxy` 

Steps included in a behandling:
- Feilutbetaling: Evaluate periods of incorrect payment and identify causes and legal references
- Vurder foreldelse: Evaluate statute of limitations for each periode
- Vurder vilkar: Evaluate fault and degree of client responsibility
- Beregning: Calculate amount, tax, and interest
- Vedtak: Finalize letter and approval (totrinnskontroll)
- The whole process may run automatically for small amounts

## Entry points

- `KravgrunnlagAsyncJmsConsumer`: receives kravgrunnlag from OS via MQ and creates or updates behandling
- `VedtakConsumer` receives vedtak from `fp-sak` and `k9-sak` creates `HåndterVedtakFattetTask`to handle event - create new behandling or suspend active behandling pending update from OS
- Most REST enpoints from `ApiConfig` method `getProduksjonsKlasser` support `fp-frontend`, except callbacks in `LosRestTjeneste` and `FpOversiktRestTjeneste` that serve `fp-los` and `fp-oversikt` respectively. 

## Repo structure

| Module                 | Purpose                                                                        |
|------------------------|--------------------------------------------------------------------------------|
| `behandlingslager`     | JPA entities and repositories                                                  |
| `behandlingskontroll`  | Behandlingssteg framework and aksjonspunkt orchestration                       |
| `behandlingsprosess`   | Concrete steg implementations for vilkar, foreldelse, and beregning            |
| `domenetjenester`      | Domain services                                                                |
| `integrasjontjenester` | External integrations such as `fpsak`, `fpoppdrag`, `k9-sak`, and `k9-oppdrag` |
| `kontrakter`           | Date warehouse Dtos                                                            |
| `migreringer`          | Flyway migrations                                                              |
| `web`                  | REST endpoints and app entry point                                             |
| `testutilities`        | Shared test fixtures                                                           |

## Verification

- Foreldrepenger-side integration tests run from `navikt/fp-autotest`.
- Relevant suites: `fptilbake`, `verdikjede`.
