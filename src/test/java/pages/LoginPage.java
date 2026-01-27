package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Seletores
    private By usernameBy = By.xpath("//input[@placeholder='Login' or @placeholder='Usuário' or contains(@name, 'username')]");
    private By passwordBy = By.xpath("//input[@placeholder='Senha' or contains(@name, 'password')]");
    private By loginButtonBy = By.xpath("//button[contains(.,'Entrar') or contains(., 'Login')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // --- GERENCIAMENTO DE CONTEXTO (IFRAME) ---
    private boolean tentarFocarNoIframe() {
        try {
            driver.switchTo().defaultContent();
            // Tenta achar qualquer iframe. O Maker geralmente só tem um no login.
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.tagName("iframe")));
            return true;
        } catch (Exception e) {
            System.out.println("LOG: Tentativa de focar no iframe falhou. Pode ser que a página esteja carregando.");
            return false;
        }
    }

    // --- PREENCHIMENTO COM JAVASCRIPT ---
    private void preencherComJS(By locator, String texto) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", element, texto);
        js.executeScript("arguments[0].dispatchEvent(new Event('input'));", element);
        js.executeScript("arguments[0].dispatchEvent(new Event('change'));", element);
    }

    public void preencherUsuario(String usuario) {
        tentarFocarNoIframe();
        preencherComJS(usernameBy, usuario);
    }

    // --- A CORREÇÃO DE OURO: LOOP DE INSISTÊNCIA PARA A SENHA ---
    public void preencherSenha(String senha) {
        boolean sucesso = false;
        int tentativas = 0;

        // Tenta 3 vezes achar o campo senha, caso o iframe esteja recarregando
        while (!sucesso && tentativas < 3) {
            try {
                tentarFocarNoIframe(); // Garante que estamos dentro da boneca russa

                // Tenta achar o campo com um timeout curto (5s) para não travar muito
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(passwordBy));

                // Se achou, preenche!
                preencherComJS(passwordBy, senha);
                sucesso = true; // Sai do loop

            } catch (Exception e) {
                System.out.println("LOG: Falha ao achar campo senha (Tentativa " + (tentativas + 1) + "/3). O iframe pode estar recarregando...");
                tentativas++;
                try { Thread.sleep(2000); } catch (InterruptedException i) {} // Espera 2s antes de tentar de novo
            }
        }

        if (!sucesso) {
            throw new RuntimeException("ERRO CRÍTICO: Não foi possível encontrar o campo Senha após 3 tentativas e recarregamentos de Iframe.");
        }

        // Tenta dar Enter
        try { driver.findElement(passwordBy).sendKeys(Keys.ENTER); } catch (Exception e) {}
    }

    public void clicarEntrar() {
        try {
            tentarFocarNoIframe();
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(loginButtonBy));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {}
    }

    public void realizarLogin(String usuario, String senha) {
        // Aumentei o tempo inicial para garantir que o Linux carregou a página
        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        preencherUsuario(usuario);

        // Esse sleep é crucial: dá tempo do Maker iniciar o refresh do iframe
        // antes da gente começar a procurar a senha.
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        preencherSenha(senha);

        // clicarEntrar(); // Desnecessário se o Enter funcionar
    }
}