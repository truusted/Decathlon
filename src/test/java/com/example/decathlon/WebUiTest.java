package com.example.decathlon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebUiTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        System.out.println("Startar Chrome webbläsare...");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Öppnar applikationen på lokal server
        driver.get("http://localhost:8080/index.html");
        System.out.println("Webbsidan har laddats.");
    }

    // Hjälpmetod för att vänta på och hämta element
    private WebElement getElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // TF 1: 3.3.2 Web UI-testning - Verifiera unika och stabila ID-attribut för inmatningsfält
    @Test
    public void testInputFieldsHaveStableIds() throws InterruptedException {
        System.out.println("--- STARTAR TEST: TF 1 - Verifiering av inmatningsfält ---");

        // 1. Hittar inmatningsfältet för tävlandes namn via id="name"
        WebElement nameInput = getElement(By.id("name"));
        nameInput.sendKeys("Damir");
        System.out.println("Inmatningsfält för namn (id='name') hittat. Fyllde i: Damir");

        // 2. Hittar inmatningsfältet för namn vid resultatregistrering via id="name2"
        WebElement name2Input = getElement(By.id("name2"));
        name2Input.sendKeys("Damir");
        System.out.println("Bekräftelsefält för namn (id='name2') hittat.");

        // 3. Hittar rullgardinsmenyn för gren via id="event"
        WebElement eventSelect = getElement(By.id("event"));
        System.out.println("Rullgardinsmeny för gren (id='event') hittad.");

        // 4. Hittar inmatningsfältet för resultat via id="raw"
        WebElement rawInput = getElement(By.id("raw"));
        rawInput.sendKeys("10.50");
        System.out.println("Inmatningsfält för resultat (id='raw') hittat. Fyllde i: 10.50");

        // En paus på 4 sekunder för att se åtgärderna på skärmen
        Thread.sleep(4000);

        // 5. Laddar om sidan (refresh) och kontrollerar stabilitet
        driver.navigate().refresh();
        System.out.println("Sidan har laddats om. Kontrollerar att fälten fortfarande finns...");

        WebElement refreshedNameInput = getElement(By.id("name"));
        WebElement refreshedRawInput = getElement(By.id("raw"));

        // Kontrollerar att ID-attributen är oförändrade efter omladdning
        assertEquals("name", refreshedNameInput.getAttribute("id"));
        assertEquals("raw", refreshedRawInput.getAttribute("id"));

        System.out.println("TF 1 SLUTFÖRT MED LYCKAT RESULTAT: Alla ID-attribut för inmatningsfält är stabila.");
    }

    // TF 2: 3.3.2 Web UI-testning - Verifiera stabila knappar och åtgärdselement för beräkning
    @Test
    public void testButtonsHaveStableIds() throws InterruptedException {
        System.out.println("--- STARTAR TEST: TF 2 - Verifiering av knappar ---");

        // 1. Hittar knappen 'Add competitor' via id="add"
        WebElement addButton = getElement(By.id("add"));
        System.out.println("Knappen 'Add competitor' (id='add') hittad.");

        // 2. Hittar knappen 'Save score' via id="save"
        WebElement saveButton = getElement(By.id("save"));
        System.out.println("Knappen 'Save score' (id='save') hittad.");

        // 3. Hittar knappen 'Export CSV' via id="export"
        WebElement exportButton = getElement(By.id("export"));
        System.out.println("Knappen 'Export CSV' (id='export') hittad.");

        // 4. Testar att interagera med knapparna
        addButton.click();
        saveButton.click();
        System.out.println("Knapparna är klickbara.");

        // En paus på 4 sekunder för att se åtgärderna på skärmen
        Thread.sleep(4000);

        // 5. Laddar om sidan (refresh) och kontrollerar stabilitet
        driver.navigate().refresh();
        System.out.println("Sidan har laddats om. Kontrollerar att knapparna fortfarande finns...");

        WebElement refreshedAddButton = getElement(By.id("add"));
        WebElement refreshedSaveButton = getElement(By.id("save"));
        WebElement refreshedExportButton = getElement(By.id("export"));

        // Kontrollerar att ID-attributen är oförändrade efter omladdning
        assertEquals("add", refreshedAddButton.getAttribute("id"));
        assertEquals("save", refreshedSaveButton.getAttribute("id"));
        assertEquals("export", refreshedExportButton.getAttribute("id"));

        System.out.println("TF 2 SLUTFÖRT MED LYCKAT RESULTAT: Alla knappar har stabila ID-attribut.");
    }

    // TF 3: 3.3.2 Web UI-testning - Verifiera tydlig och identifierbar struktur för resultatvisning
    @Test
    public void testResultsStructureHasStableIds() throws InterruptedException {
        System.out.println("--- STARTAR TEST: TF 3 - Verifiering av resultatstruktur ---");

        // 1. Hittar rubriken för resultatsektionen via XPath
        WebElement standingsHeader = getElement(By.xpath("//h2[text()='Standings']"));
        System.out.println("Rubrik för resultatstruktur hittad: " + standingsHeader.getText());

        // 2. Hittar resultattabellen via XPath
        WebElement standingsTable = getElement(By.xpath("//table"));
        System.out.println("Resultattabell hittad på sidan.");

        // En paus på 4 sekunder för att se elementen på skärmen
        Thread.sleep(4000);

        // 3. Laddar om sidan (refresh) och kontrollerar stabilitet
        driver.navigate().refresh();
        System.out.println("Sidan har laddats om. Kontrollerar att strukturen fortfarande finns...");

        WebElement refreshedHeader = getElement(By.xpath("//h2[text()='Standings']"));
        WebElement refreshedTable = getElement(By.xpath("//table"));

        // Kontrollerar att elementen finns kvar med korrekt text och tagg efter omladdning
        assertEquals("Standings", refreshedHeader.getText());
        assertEquals("table", refreshedTable.getTagName());

        System.out.println("TF 3 SLUTFÖRT MED LYCKAT RESULTAT: Resultatstrukturen är stabil och identifierbar.");
    }

    // TF 4: Verifiera valideringsspärr för max 40 deltagare och resultatregistrering
    @Test
    public void testMaxCompetitorsLimit() throws InterruptedException {
        System.out.println("--- STARTAR TEST: TF 4 - Validering av max 40 deltagare med resultat ---");

        WebElement nameInput = getElement(By.id("name"));
        WebElement addButton = getElement(By.id("add"));
        WebElement name2Input = getElement(By.id("name2"));
        WebElement eventSelectElement = getElement(By.id("event"));
        WebElement rawInput = getElement(By.id("raw"));
        WebElement saveButton = getElement(By.id("save"));

        org.openqa.selenium.support.ui.Select eventDropdown =
                new org.openqa.selenium.support.ui.Select(eventSelectElement);

        // Petlja: Registrujemo takmičara, pa mu odmah upisujemo rezultat i čuvamo
        for (int i = 1; i <= 41; i++) {
            String competitorName = "Deltagare " + i;

            // 1. Dodavanje takmičara
            nameInput.clear();
            nameInput.sendKeys(competitorName);
            addButton.click();

            // 2. Unos rezultata za istog takmičara (npr. 100m)
            name2Input.clear();
            name2Input.sendKeys(competitorName);
            eventDropdown.selectByIndex(0); // Bira prvu disciplinu (100m)

            rawInput.clear();
            rawInput.sendKeys("10.50");

            // 3. Klik na Save score
            saveButton.click();
        }

        Thread.sleep(20000);

        // Prebrojavanje redova u tabeli Standings
        java.util.List<WebElement> rows = driver.findElements(By.xpath("//table//tbody/tr"));
        int totalCompetitorsInTable = rows.size();
        System.out.println("Antal deltagare i tabellen efter inmatning: " + totalCompetitorsInTable);

        // Provjera prikaza greške na interfejsu (id="error" iz HTML-a)
        WebElement errorBanner = driver.findElement(By.id("error"));
        String errorText = errorBanner.getText();
        System.out.println("Felmeddelande på sidan: " + (errorText.isEmpty() ? "INGET FELMEDDELANDE" : errorText));

        if (totalCompetitorsInTable > 40) {
            System.out.println("AVVIKELSE BEKRÄFTAD: Systemet tillåter " + totalCompetitorsInTable + " deltagare utan felmeddelande. Bugg existerar!");
        } else {
            System.out.println("Valideringsspärr fungerar som förväntat.");
        }

        // Test pada ako tabela sadrži više od 40 takmičara
        assertEquals(40, totalCompetitorsInTable,
                "Bugg bekräftad: Systemet saknar valideringsspärr och tillåter fler än 40 deltagare!");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            System.out.println("Stänger webbläsaren...");
            driver.quit();
        }
    }
}