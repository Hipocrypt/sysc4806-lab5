# Complete GitHub and Azure setup

Student: **Daniel Esenwa (101199099)**.

The GitHub repository and Azure Web App have not been created. These steps need
your account sign-in. The included workflow implements CI and CD; the account
configuration below enables it. No live deployment is claimed in this project.

## 1. Create the repository and run CI

1. Sign in to GitHub and create an **empty** repository, for example
   `sysc4806-lab5`. Leave the automatic README, license, and gitignore unchecked.
2. In PowerShell, from the Lab5 directory, connect and push the prepared Git
   repository. Replace OWNER and REPOSITORY with your real values:

   ```powershell
   git remote add origin https://github.com/OWNER/REPOSITORY.git
   git push -u origin main
   ```

   If working from the extracted submission ZIP, first initialize its history:

   ```powershell
   git init -b main
   git add .
   git commit -m "Implement Lab5 AddressBook with HTTP tests and CI/CD"
   ```

3. Open **Actions > Java CI and Azure CD**. Confirm **Build and test (Java 21)**
   passes and download the `junit-reports` artifact if desired. Deployment is
   skipped until the Azure configuration is set. Update the README repository
   link and replace OWNER/REPOSITORY in its badge example with your real values.

## 2. Create the free Java Web App

1. Activate [Azure for Students](https://azure.microsoft.com/en-ca/free/students/)
   using your Carleton cmail account, as required by the PDF. Complete account
   verification before creating resources.
2. At [Azure Portal](https://portal.azure.com/), select **App Services > Create >
   Web App**. Choose your **Azure for Students** subscription and a new resource
   group, for example `sysc4806-lab5-rg`.
3. Choose a globally unique instance name, for example
   `sysc4806-lab5-101199099` if it is available. Select **Code**, **Java 21**,
   **Java SE (embedded web server)**, and Linux. Select **Free F1 (Shared
   infrastructure)** for the App Service plan. If F1 is unavailable, choose a
   region offering it. Do not silently accept a paid plan.
4. Do not add a managed database. The app uses H2 in memory. Keep app
   authentication disabled so the TAs can view it. Leave portal-generated
   deployment disabled during creation: the checked-in workflow will provide
   continuous deployment after the next section, including the required test
   gate. Do not generate a second workflow that deploys without running tests.
5. Review and create the Web App. On **Overview**, copy the actual **Default
   domain**. Use its full `https://` URL; Azure may include a unique suffix in
   the hostname. Keep Java 21 configured under the app's stack settings.

## 3. Connect GitHub Actions to Azure

This workflow uses OpenID Connect, which is compatible with leaving basic
publishing authentication disabled. Microsoft documents this method in
[its App Service Actions guide](https://learn.microsoft.com/en-us/azure/app-service/deploy-github-actions).

1. Open **Azure Cloud Shell**, choose **Bash**, and use its upload control to
   upload `scripts/configure-azure.sh` from this project.
2. Confirm `az account show` displays your Azure for Students subscription.
   If necessary, select it with `az account set --subscription YOUR_SUBSCRIPTION_ID`.
3. Run the uploaded script, replacing all three example arguments:

   ```bash
   bash configure-azure.sh sysc4806-lab5-rg YOUR_WEB_APP_NAME OWNER/REPOSITORY
   ```

   The script creates a managed identity and a federated credential limited to
   this repository's `main` branch, and grants Website Contributor on this Web
   App. It does not create a paid hosting plan. Your Azure account needs
   permission to create the identity and its role assignment. If your student
   subscription denies that assignment, use the portal's Deployment Center
   with an administrator-provided identity and copy its client/tenant/subscription
   values into the same secrets below.
4. In GitHub **Settings > Secrets and variables > Actions**, add these
   **repository secrets** using the script's output:

   | Secret | Value |
   | --- | --- |
   | `AZURE_CLIENT_ID` | Managed identity client ID |
   | `AZURE_TENANT_ID` | Azure tenant ID |
   | `AZURE_SUBSCRIPTION_ID` | Azure for Students subscription ID |

5. Under the **Variables** tab, add the **repository variable**
   `AZURE_WEBAPP_NAME` with the exact instance name. Do not put it in Secrets:
   the workflow's job condition reads the repository variable.
6. The workflow deploys directly from `main`. If you add a GitHub Environment
   to the deploy job later, its OIDC subject also needs to be changed to match.

## 4. Deploy, verify, and demonstrate an update

1. In GitHub Actions, select **Java CI and Azure CD > Run workflow > main**.
   Both build and deploy must pass. You can also trigger them by pushing a
   commit to `main`. Identity permissions may need a few minutes to propagate.
2. The build job runs all tests, then the deploy job downloads the same JAR and
   deploys it. The last step creates a book and buddy on the live app and checks
   the new email field in JSON and HTML. Its sample data remains until restart.
3. Open your copied Azure URL, create a book, and add a buddy with an email.
   Confirm the email appears in the table. The free tier may start slowly; the
   smoke script retries while startup completes. For startup failures, inspect
   Azure **Log stream** and confirm Java 21 / Java SE is configured.
4. Demonstrate redeployment with a visible change, such as changing the home
   page introduction in `src/main/resources/static/index.html`. Run
   `.\mvn.ps1 clean verify`, commit, and push. Confirm a new Actions run builds,
   deploys, passes the email smoke check, and serves the updated introduction.
   The email feature is already the tested functional addition relative to Lab4.
5. Replace the README's pending deployment text with the real clickable Azure
   link. Add the real repository link and CI badge. Commit and push those
   changes too. Do not mark deployment complete based only on a skipped job.

To repeat the smoke test manually with Python 3:

```powershell
python scripts/smoke_test.py https://YOUR_ACTUAL_DEFAULT_DOMAIN
```

## 5. Finish the required submission

- [ ] README identifies Daniel Esenwa and student ID 101199099.
- [ ] README contains the actual GitHub and working Azure links and build badge.
- [ ] GitHub Actions shows passing tests and an actual successful deployment.
- [ ] A pushed update has appeared on Azure.
- [ ] Invite the TA GitHub handles from **Brightspace > Course Details** using
  the repository's **Settings > Collaborators**. Those handles are not in the PDF.
- [ ] Regenerate the ZIP with `.\scripts\package-submission.ps1` after editing
  the README, and upload it to Brightspace.

The local project, tests, workflow, and ZIP can be prepared without these
accounts. Azure activation, hosted CI/CD evidence, TA access, and the Brightspace
upload require your authenticated accounts and remain outstanding until done.
