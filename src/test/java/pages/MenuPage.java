package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MenuPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Elementos da tela
    private By avatarUsuarioBy = By.id("UserInfo");
    private By campoBuscaBy = By.xpath("//input[@placeholder='Buscar no menu']");

    public MenuPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public boolean validarLoginComSucesso() {
        // Pequena pausa para garantir estabilidade do carregamento visual
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        try {
            // 1. Volta para a raiz
            driver.switchTo().defaultContent();

            // 2. Entra no primeiro nível (Iframe do Sistema)
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainsystem")));

            // 3. Entra no segundo nível (Iframe do Dashboard/Conteúdo)
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.tagName("iframe")));

            // 4. Valida se encontrou o Avatar OU a Busca
            if (elementoExiste(avatarUsuarioBy) || elementoExiste(campoBuscaBy)) {
                return true;
            }

        } catch (Exception e) {
            // Se falhar a navegação entre frames, retorna falso
            return false;
        }

        return false;
    }

    private boolean elementoExiste(By locator) {
        try {
            // Timeout curto apenas para checar existência
            WebElement element = new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}