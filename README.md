# SYSC 4806 Lab 5: Continuous Delivery of AddressBook

**Student:** Daniel Esenwa

**Student ID:** 101199099

**GitHub repository:** Not created yet.

**Azure deployment:** Not deployed yet.

The application and CI/CD workflow are implemented and tested locally. GitHub
and Azure setup remains to be completed using [DEPLOYMENT.md](DEPLOYMENT.md).
Replace the two lines above with the real repository and live deployment links
after setup. Do not submit this ZIP as proof of a live deployment until then.

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
reports and the executable `addressbook.jar`. Deployment depends on that build
passing and downloads its exact JAR. It runs only on `main`, after the Azure
repository variable and OIDC secrets have been configured. Pull requests do not
deploy. The final smoke check verifies the live email feature over HTTP.

Before Azure is configured the deployment job is deliberately skipped; a green
build alone does not establish that a deployment has occurred. No dependency
graph upload or additional GitHub permission token is needed.

After creating the repository, add a build badge using your actual owner/repo:

```markdown
[![Java CI and Azure CD](https://github.com/OWNER/REPOSITORY/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/OWNER/REPOSITORY/actions/workflows/ci-cd.yml)
```

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

Complete the account and deployment steps in [DEPLOYMENT.md](DEPLOYMENT.md),
replace the pending links above, add your badge, invite the TAs listed on
Brightspace, and regenerate the archive:

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
