package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By usernameBy = By.xpath(
            "//input[@placeholder='Login' or @placeholder='Usuário' or contains(@name,'user')]"
    );

    private final By passwordBy = By.xpath(
            "//input[@type='password' or @placeholder='Senha' or contains(@name,'password')]"
    );

    private final By loginButtonBy = By.xpath(
            "//button[contains(.,'Entrar') or contains(.,'Login')]"
    );

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    private void focarNoIframeSeExistir() {
        driver.switchTo().defaultContent();
        if (!driver.findElements(By.tagName("iframe")).isEmpty()) {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.tagName("iframe")));
        }
    }

    private WebElement aguardarCampo(By locator) {
        focarNoIframeSeExistir();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void preencherUsuario(String usuario) {
        WebElement campo = aguardarCampo(usernameBy);
        campo.clear();
        campo.sendKeys(usuario);
    }

    public void preencherSenha(String senha) {
        WebElement campo = aguardarCampo(passwordBy);
        campo.clear();
        campo.sendKeys(senha);
        campo.sendKeys(Keys.ENTER);
    }

    public void clicarEntrar() {
        focarNoIframeSeExistir();
        wait.until(ExpectedConditions.elementToBeClickable(loginButtonBy)).click();
    }

    public void realizarLogin(String usuario, String senha) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        preencherUsuario(usuario);
        preencherSenha(senha);
    }
}
