package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys; // Importante
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    private By usernameBy = By.xpath("//input[@placeholder='Login']");
    private By passwordBy = By.xpath("//input[@placeholder='Senha']");
    private By loginButtonBy = By.xpath("//button[contains(.,'Entrar')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        identificarEEntrarNoFrame();
    }

    private void identificarEEntrarNoFrame() {
        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.tagName("iframe")));
        } catch (TimeoutException e) {
            System.out.println("DEBUG: Nenhum iframe de login detectado.");
        }
    }

    public void preencherUsuario(String usuario) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameBy));
        element.clear();
        element.sendKeys(usuario);
    }

    public void preencherSenha(String senha) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordBy));
        element.clear();
        // Digita a senha e aperta ENTER (funciona como clicar em Entrar)
        element.sendKeys(senha + Keys.ENTER);
    }

    public void clicarEntrar() {
        // Deixamos este método como backup. Se o ENTER falhar, o teste tenta clicar.
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(loginButtonBy));
            element.click();
        } catch (Exception e) {
            // Ignora erro aqui pois já tentamos o ENTER
        }
    }

    public void realizarLogin(String usuario, String senha) {
        preencherUsuario(usuario);
        // O ENTER já vai ser enviado aqui dentro
        preencherSenha(senha);

        // Opcional: Clicar no botão só por garantia (redundância)
        // clicarEntrar();
    }
}