# fptilbake

Tilbakekrevingsapplikasjon — handles cases where Nav has paid benefits to a
client and the client must repay an amount (tilbakekreving + tilbakebetaling).

Repository builds **two deployments** from the same codebase:
- **fptilbake** — for Foreldrepenger, Svangerskapspenger, Engangsstønad (Team Foreldrepenger)
- **k9-tilbake** — for k9-ytelser: pleiepenger, omsorgspenger, opplæringspenger, frisinn (Team Sykdom-i-familien)

When changing code, consider impact on **both** deployments.

## Domain

| Concept | Description |
|---------|-------------|
| Kravgrunnlag | Trigger from payment system (oppdragssystem) with claim basis — the main entry point |
| Varsel | Early notice letter sent when simulation indicates potential tilbakekreving |
| Tilbakekrevingsbehandling | Case processing of the repayment claim |
| Vurder vilkår | Evaluate cause and degree of client fault (forsett, grov uaktsomhet, simpel uaktsomhet) |
| Vurder foreldelse | Evaluate statute of limitations |
| Beregning | Calculate amount, tax (skattetrekk), and interest (rente) |
| Vedtak | Decision letter (vedtaksbrev) to client + structured data to payment system |

## Value chain position

OS (Oppdragssystemet, payment system) is external to the team and handles the payment balance for the clients.
The process is primarily triggered by a claim basis (kravgrunnlag) from OS over JMS/MQ, with a secondary entry point through varsel.
The final decision (vedtak) is sent back to OS for balance adjustment and client communication, via fp-ws-proxy.

```
OS → kravgrunnlag over MQ → fptilbake/k9-tilbake → vedtak → iverksetting (Vedtak til OS via fp-ws-proxy, vedtaksbrev) 
fpsak/k9-sak → simulering -> varsel → fptilbake/k9-tilbake (opprett tilbakekreving, evt sender varsel)
```

fpoppdrag is **not** the source of kravgrunnlag — that comes directly from OS.
fptilbake calls fpoppdrag only during the notice flow, to fetch repayment periods from the simulation as part of the notice letter.

## Tech stack

Standard Team Foreldrepenger backend stack. See
[fp-context/architecture/backend-stack.md](https://github.com/navikt/fp-context/blob/main/architecture/backend-stack.md).
Java 25, Jetty, Jersey, Weld, Hibernate, Jackson, fp-prosesstask, fp-felles.

## Module structure

| Module | Purpose |
|--------|---------|
| `behandlingslager` | JPA entities, repositories |
| `behandlingskontroll` | Behandlingssteg framework, aksjonspunkt orchestration |
| `behandlingsprosess` | Concrete steg implementations (vilkår, foreldelse, beregning) |
| `domenetjenester` | Domain services |
| `integrasjontjenester` | External integrations (fpsak, fpoppdrag, k9-sak, k9-oppdrag) |
| `kafka` | Kafka consumers/producers (kravgrunnlag, hendelser) |
| `kontrakter` | API contracts |
| `migreringer` | Flyway DB migrations |
| `web` | REST endpoints, app entry point |
| `testutilities` | Shared test fixtures |

## Build and test

```bash
mvn clean install                                  # full build + unit tests
mvn test -pl behandlingsprosess                    # tests for one module
```

## Integration tests

Run from [fp-autotest](https://github.com/navikt/fp-autotest) (Foreldrepenger side):
- Suite: `fptilbake` (also runs in `verdikjede`)
- See `fp-autotest/AGENTS.md` for catalog and run commands

k9-tilbake has its own integration test setup outside fp-autotest.

## Team context

| Hub                                                            | Purpose                                               |
|----------------------------------------------------------------|-------------------------------------------------------|
| [fp-context](https://github.com/navikt/fp-context)             | Team Foreldrepenger domain, architecture, conventions |
| TeamForeldrepenger Copilot Space (`navikt`)                    | Same content, attached for AI grounding               |
| [fp-gha-workflows](https://github.com/navikt/fp-gha-workflows) | Reusable CI/CD workflows                              |
| [fp-bom](https://github.com/navikt/fp-bom)                     | Parent POM, dependency versions                       |
| [fp-felles](https://github.com/navikt/fp-felles)               | Common components                                     |
| [fp-prosesstask](https://github.com/navikt/fp-prosesstask)     | Asynch execution framework                            |

Use these as authoritative for shared conventions. This file only covers
fptilbake-specific information.

## Cross-team note

Changes here affect both Team Foreldrepenger and Team Sykdom-i-familien
(`#sykdom-i-familien`, `#po-familie-tilbake`). Coordinate larger changes
across teams.
