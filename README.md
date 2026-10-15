# SYSC 4806 Lab 5: Continuous Delivery of AddressBook

**Student:** Daniel Esenwa

**Student ID:** 101199099

**GitHub repository:** [Hipocrypt/sysc4806-lab5](https://github.com/Hipocrypt/sysc4806-lab5)

**Azure deployment:** [Open the AddressBook app](https://lab5-101199099-a9draqh6gjhcazf4.canadacentral-01.azurewebsites.net/)

The repository is connected to GitHub, and Azure's deployment workflow targets
`lab5-101199099`. The hosted build and deployment passed, and the live app's
POST/GET workflow and email feature passed verification on October 15, 2026.
Setup and submission steps are recorded in [DEPLOYMENT.md](DEPLOYMENT.md).

[![Java CI and Azure CD](https://github.com/Hipocrypt/sysc4806-lab5/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/Hipocrypt/sysc4806-lab5/actions/workflows/ci-cd.yml)
[![Azure deployment](https://github.com/Hipocrypt/sysc4806-lab5/actions/workflows/main_lab5-101199099.yml/badge.svg)](https://github.com/Hipocrypt/sysc4806-lab5/actions/workflows/main_lab5-101199099.yml)

This project extends the Lab4 Spring MVC address book with automated HTTP tests,
a GitHub Actions pipeline, and an optional **email address** for each buddy.
Email is saved by JPA, returned in JSON, and displayed in the Thymeleaf table.
Requests from Lab4 that omit email still work. A supplied email is trimmed and
checked for a basic address format and a maximum length of 254 characters.

## Run and test

Use **JDK 21**. The POM, GitHub Actions runner, and Azure runtime all use Java 21.
Apache Maven Wrapper 3.3.4 downloads Maven 3.9.11 on its first run. It needs
internet access to download Maven and dependencies; Maven need not be installed.

```powershell
# Windows, with JAVA_HOME pointing to JDK 21:
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
.\mvnw.cmd spring-boot:run
```

On this Windows workstation the convenience helper also locates JDK 21 and uses
the workspace's local Maven cache:

```powershell
.\mvn.ps1 --batch-mode --no-transfer-progress clean verify
.\mvn.ps1 spring-boot:run
```

```bash
# macOS / Linux, with JDK 21 configured:
chmod +x mvnw
./mvnw --batch-mode --no-transfer-progress clean verify
./mvnw spring-boot:run
```

Open **http://localhost:8080/**. Create a book, add buddies with optional emails,
and keep the book ID to reopen it. The packaged application can also be run with
`java -jar target/addressbook.jar`. The optional `PORT` environment variable
changes the port. H2 is in memory: data is discarded when the app restarts,
including on redeployment or Azure free-tier recycling. Address and phone retain
the integer types from the previous labs.

## Automated verification

`clean verify` runs **22 tests**, including five real HTTP integration tests
against embedded Tomcat on a random port and four MockMvc tests without starting
a server. Model and database tests cover the rest. The HTTP tests exercise the
actual POST/GET sequence, response codes, Location headers, persistence across
requests, email handling, HTML rendering, duplicate membership, missing records,
invalid input, and the home page. MockMvc additionally checks HTML escaping.

The project retains Spring Boot 3.5.6 from Lab4. It uses `TestRestTemplate` for
real HTTP tests and `MockMvc` for requests without a listening server; the newer
`RestTestClient` shown in the current tutorial is not part of this dependency
version. See [TEST-RESULTS.md](TEST-RESULTS.md) for the local verification record.

## Continuous integration and delivery

[.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml) runs tests and builds
on **every branch push**, pull request, and manual workflow run. It uploads JUnit
reports and the executable `addressbook.jar`.

Azure's connected workflow,
[.github/workflows/main_lab5-101199099.yml](.github/workflows/main_lab5-101199099.yml),
builds with Java 21 and runs `mvn clean install` on `main`. Its deployment job
depends on that build passing, downloads the resulting JAR, authenticates with
Azure's generated OIDC secrets, and deploys to `lab5-101199099`. Pull requests do
not deploy. This is the active deployment connection created in Azure.

The original `ci-cd.yml` also contains an optional deployment job that stays
skipped unless `AZURE_WEBAPP_NAME` is configured as a repository variable. Leave
that optional job disabled while using Azure's connected workflow to avoid
duplicate deployments. No dependency graph upload or additional GitHub
permission token is needed. `scripts/smoke_test.py` can verify the live app's
POST/GET workflow and email feature using its actual URL.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/` | Browser interface for creating or opening a book |
| POST | `/api/addressbooks` | Create a book with JSON `{}` |
| POST | `/api/buddies` | Create a buddy with firstName, lastName, address, phone, optional email |
| POST | `/api/addressbooks/{id}/buddies` | Attach an existing buddy with buddyId |
| GET | `/api/addressbooks/{id}` | Book and buddies as JSON |
| GET | `/api/buddies/{id}` | Buddy as JSON |
| GET | `/addressbooks/{id}` | Thymeleaf view and add-buddy form |

Creation returns 201 and a Location header. Missing records return 404, invalid
fields return 400, duplicate attachment is idempotent, and assigning the same
buddy to another book returns 409. See [HTTP-requests.txt](HTTP-requests.txt) for
PowerShell requests. The app does not seed or delete sample records at startup.

## Submission

Review [DEPLOYMENT.md](DEPLOYMENT.md), invite the TAs listed on Brightspace,
and regenerate the archive after any further changes:

```powershell
.\scripts\package-submission.ps1
```

Upload `lab5-101199099-submission.zip` to Brightspace. The archive includes the
source, tests, README, Maven wrappers, and hidden `.github` workflow directory.
It excludes `.git`, build output, local tool downloads, credentials, and the PDF.

## References

- [Spring: Testing the Web Layer](https://spring.io/guides/gs/testing-web/)
- [GitHub: Building and testing Java with Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)
- [Microsoft: Deploy to Azure App Service using GitHub Actions](https://learn.microsoft.com/en-us/azure/app-service/deploy-github-actions)
- [Apache Maven Wrapper](https://maven.apache.org/tools/wrapper/)
