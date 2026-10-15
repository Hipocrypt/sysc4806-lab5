# Verification record

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

## GitHub Actions and Azure verification

Verified on October 15, 2026 for commit `e89d3d9`:

- [GitHub CI run 38782193451](https://github.com/Hipocrypt/sysc4806-lab5/actions/runs/38782193451):
  **success**, including the Test and package step and JUnit report upload.
- [Azure workflow run 38782193476](https://github.com/Hipocrypt/sysc4806-lab5/actions/runs/38782193476):
  **success** for both the build and deployment jobs. The Maven build runs the
  tests before the dependent deployment job can start.
- [Live AddressBook](https://lab5-101199099-a9draqh6gjhcazf4.canadacentral-01.azurewebsites.net/):
  `scripts/smoke_test.py` **passed** against the Azure URL. It verified the
  student name and ID on the home page, book and buddy creation, attachment,
  the saved email in book/buddy JSON, and the email in the Thymeleaf HTML.

The Azure-generated workflow is the active deployment connection. The original
workflow's optional deployment job remains skipped, so only the Azure-generated
workflow deploys the app.

TA invitations and Brightspace submission remain unverified. See
`DEPLOYMENT.md` for the remaining submission steps.
