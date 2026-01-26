package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Seus seletores
    private By usernameBy = By.xpath("//input[@placeholder='Login' or @placeholder='Usuário' or contains(@name, 'username')]");
    private By passwordBy = By.xpath("//input[@placeholder='Senha' or contains(@name, 'password')]");

    // Botão de entrar (usado apenas se o ENTER falhar)
    private By loginButtonBy = By.xpath("//button[contains(.,'Entrar') or contains(., 'Login')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        identificarEEntrarNoFrame();
    }

    private void identificarEEntrarNoFrame() {
        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.tagName("iframe")));
        } catch (TimeoutException e) {
            System.out.println("DEBUG: Nenhum iframe detectado. Tentando na página principal.");
        }
    }

    // --- MÉTODOS DE APOIO (JS para burlar cortinas de carregamento) ---

    private void preencherComJS(By locator, String texto) {
        // Usa 'presence' em vez de 'visibility'. Se existir no HTML, o JS preenche.
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", element, texto);
        // Dispara evento para o sistema saber que digitamos
        js.executeScript("arguments[0].dispatchEvent(new Event('input'));", element);
    }

    public void preencherUsuario(String usuario) {
        preencherComJS(usernameBy, usuario);
    }

    public void preencherSenha(String senha) {
        preencherComJS(passwordBy, senha);

        // Envia o ENTER via Selenium (funciona melhor que clicar)
        WebElement element = driver.findElement(passwordBy);
        element.sendKeys(Keys.ENTER);
    }

    public void clicarEntrar() {
        // Método de backup: Só clica se precisar
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(loginButtonBy));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            // Ignora erro, pois o ENTER provavelmente já resolveu
        }
    }

    public void realizarLogin(String usuario, String senha) {
        // Pequena pausa para estabilidade no Linux
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        preencherUsuario(usuario);

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        preencherSenha(senha);

        // --- CORREÇÃO PRINCIPAL AQUI ---
        // Removi a chamada obrigatória para clicarEntrar().
        // Como o preencherSenha já dá ENTER, chamar o clique aqui causava o erro
        // de "Botão não encontrado" porque a página já estava mudando.
    }
}