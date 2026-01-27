package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Seus XPaths estavam ótimos, mantive eles.
    private final By usernameBy = By.xpath("//input[@placeholder='Login' or @placeholder='Usuário' or contains(@name,'user')]");
    private final By passwordBy = By.xpath("//input[@type='password' or @placeholder='Senha' or contains(@name,'password')]");
    private final By loginButtonBy = By.xpath("//button[contains(.,'Entrar') or contains(.,'Login')]");

    // Seletor genérico de iframe
    private final By iframeBy = By.tagName("iframe");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    // --- CORREÇÃO 1: Foco no Iframe mais agressivo ---
    private boolean tentarFocarNoIframe() {
        try {
            driver.switchTo().defaultContent();
            wait.until(ExpectedConditions.presenceOfElementLocated(iframeBy));
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframeBy));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --- CORREÇÃO 2: Preencher com JavaScript (Ignora loading masks) ---
    private void preencherComJS(By locator, String texto) {
        // Usa 'presence' em vez de 'visibility' (Resolve o erro de Timeout no Linux)
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        // Limpa e digita direto no HTML
        js.executeScript("arguments[0].value = '';", element);
        js.executeScript("arguments[0].value = arguments[1];", element, texto);
        // Dispara eventos para o sistema "acordar"
        js.executeScript("arguments[0].dispatchEvent(new Event('input'));", element);
        js.executeScript("arguments[0].dispatchEvent(new Event('change'));", element);
    }

    public void preencherUsuario(String usuario) {
        tentarFocarNoIframe();
        preencherComJS(usernameBy, usuario);

        // --- CORREÇÃO 3: TAB para forçar refresh do Maker ---
        try {
            driver.findElement(usernameBy).sendKeys(Keys.TAB);
        } catch (Exception e) {}
    }

    public void preencherSenha(String senha) {
        boolean sucesso = false;
        int tentativas = 0;

        // Loop de Insistência: Se o iframe piscar, ele tenta de novo
        while (!sucesso && tentativas < 4) {
            try {
                tentarFocarNoIframe();

                // Timeout curto para verificar se o campo já apareceu
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(passwordBy));

                preencherComJS(passwordBy, senha);

                // Dá o Enter final
                driver.findElement(passwordBy).sendKeys(Keys.ENTER);

                sucesso = true;
            } catch (Exception e) {
                tentativas++;
                System.out.println("LOG: Iframe recarregando... Tentativa " + tentativas + "/4");
                // Pausa para o Linux respirar
                try { Thread.sleep(2000); } catch (InterruptedException i) {}
            }
        }

        if (!sucesso) {
            throw new RuntimeException("ERRO: Não foi possível preencher a senha após 4 tentativas.");
        }
    }

    public void realizarLogin(String usuario, String senha) {
        // Pausa inicial para o ambiente CI carregar
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        preencherUsuario(usuario);

        // Pausa entre usuário e senha para o refresh do iframe
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        preencherSenha(senha);

        // --- CORREÇÃO 4: Voltar para a página principal ---
        // Isso impede que os próximos testes falhem procurando menu dentro do iframe de login
        System.out.println("LOG: Login enviado. Voltando ao contexto padrão.");
        driver.switchTo().defaultContent();
    }
}