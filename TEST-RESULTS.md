# Local verification record

**Daniel Esenwa — 101199099**

Verified on **October 15, 2026** using Microsoft OpenJDK **21.0.10** and
Apache Maven **3.9.11**, with Spring Boot **3.5.6**.

## Build and automated tests

Maven `clean verify`: **BUILD SUCCESS**. The compiler used Java release 21.
The resulting Spring Boot executable is `target/addressbook.jar`.

| Test class | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| AddressBookHttpIntegrationTest | 5 | 0 | 0 | 0 |
| AddressBookMvcTest | 4 | 0 | 0 | 0 |
| RepositoryIntegrationTest | 3 | 0 | 0 | 0 |
| AddressBookTest | 4 | 0 | 0 | 0 |
| BuddyInfoTest | 6 | 0 | 0 | 0 |
| **Total** | **22** | **0** | **0** | **0** |

The real HTTP tests start embedded Tomcat on a random port. They verify the
JSON creation/linking/read workflow and HTML view, including the email field,
omitted and blank email, invalid email, empty books, missing records, invalid
requests, idempotent attachment, conflicting membership, and static assets.
The MockMvc tests include escaped rendering of script characters in names.
Repository tests read email from a separate persistence transaction and check
the inherited relationship and cascade behavior.

## Packaged application and browser checks

The packaged JAR was started with `PORT=8085`, confirming the configured port
override works. Running `scripts/smoke_test.py http://localhost:8085 --attempts 1`
passed: the home page, actual POST/GET requests, persisted email, and rendered
HTML all worked.

A hidden Chrome session exercised the browser's **Create address book** form,
then filled and submitted **Add a buddy** with `browser@example.com`. The
browser navigated to the new book and displayed the email in its table. The
home and populated-book screenshots were inspected locally.

## Build tooling and configuration

- The Windows convenience helper selected Java 21 and Maven 3.9.11.
- The standard Maven Wrapper downloaded Maven 3.9.11 and reported Java 21.
  Its distribution SHA-256 matches the archive verified against Maven Central.
- `actionlint` **1.7.12** accepted `.github/workflows/ci-cd.yml` without errors.
- `node --check` accepted the browser JavaScript.
- PowerShell parsed both helper/packaging scripts without errors.
- `bash -n` accepted the Azure setup script.

These are local results. GitHub-hosted Actions execution, Azure provisioning,
Azure deployment and redeployment, TA invitations, and Brightspace submission
have **not** been performed. The GitHub repository and Azure Web App still need
to be created and connected as described in `DEPLOYMENT.md`.
