package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class GrupoCarregamentoPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- SELETORES DE NAVEGAÇÃO ---
    private By menuCadastroBy = By.xpath("//span[contains(text(), 'Cadastro')]/..");
    // Texto exato do menu conforme sua imagem
    private By subMenuGrupoBy = By.xpath("//span[contains(text(), 'Grupo de Carregamento')]/..");

    private By btnIncluirBy = By.id("addRecordButton");
    private By btnGravarBy = By.cssSelector("a.webrun-form-nav-save");

    // --- SELETORES DE CAMPOS ---
    // Estratégia do Label para o campo "Nome do Grupo"
    private By inputNomeGrupoBy = By.xpath("//label[contains(., 'Nome do Grupo')]/..//input");

    // Campo para validação (Id ou Código, caso apareça)
    private By inputIdGeradoBy = By.xpath("//label[contains(., 'Id') or contains(., 'Código')]/..//input");

    public GrupoCarregamentoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void acessarTelaGrupoCarregamento() {
        wait.until(ExpectedConditions.elementToBeClickable(menuCadastroBy)).click();
        try { Thread.sleep(800); } catch (InterruptedException e) {}
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(subMenuGrupoBy));
        clicarGarantido(link);
    }

    public void iniciarInclusao() {
        localizarFocoNoElemento(btnIncluirBy, "Botão Incluir");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnIncluirBy));
        clicarGarantido(btn);
    }

    public void preencherCadastro(String nomeGrupo) {
        // Foca no frame usando o campo Nome como âncora
        localizarFocoNoElemento(inputNomeGrupoBy, "Campo Nome do Grupo");

        if (!nomeGrupo.isEmpty()) {
            preencher(inputNomeGrupoBy, nomeGrupo);
        }
    }

    public void salvarRegistro() {
        localizarFocoNoElemento(btnGravarBy, "Botão Salvar");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(btnGravarBy));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        try {
            Thread.sleep(1000);
            driver.switchTo().activeElement().sendKeys(Keys.chord(Keys.CONTROL, "s"));
            System.out.println("DEBUG: Backup CTRL+S enviado.");
        } catch (Exception e) {}
    }

    public boolean validarSeRegistroFoiSalvo() {
        try {
            System.out.println("DEBUG: Iniciando validação pós-salvamento...");
            Thread.sleep(3000); // Tempo para refresh da tela

            // Tenta achar um ID gerado
            try {
                localizarFocoNoElemento(inputIdGeradoBy, "Campo ID (Validação)");
                WebElement inputId = driver.findElement(inputIdGeradoBy);
                String valorId = inputId.getAttribute("value");
                System.out.println("DEBUG: ID Gerado: " + valorId);
                return valorId != null && !valorId.isEmpty();
            } catch (Exception e) {
                // Se não achar ID (tela não tem campo ID), verifica se NÃO há mensagens de erro
                System.out.println("AVISO: Campo ID não visível. Verificando ausência de erros...");
                String erro = obterMensagemErro();
                return erro.contains("Nenhum erro");
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String obterMensagemErro() {
        // 1. Reseta para o topo
        driver.switchTo().defaultContent();

        // 2. Chama a busca recursiva (Mergulha em todos os frames)
        String erroEncontrado = buscarErroRecursivamente(driver);

        if (erroEncontrado != null && !erroEncontrado.isEmpty()) {
            return erroEncontrado;
        }

        return "Nenhum erro visível capturado.";
    }

    private String buscarErroRecursivamente(WebDriver driver) {
        // A. Tenta achar o erro ONDE ESTAMOS AGORA
        String erroAqui = tentarCapturarErroNoContextoAtual();
        if (erroAqui != null) return erroAqui;

        // B. Se não achou, pega todos os iframes FILHOS e mergulha neles
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for (WebElement iframe : iframes) {
            try {
                driver.switchTo().frame(iframe);

                // Recursividade: Chama a si mesmo dentro do filho
                String erroNoFilho = buscarErroRecursivamente(driver);

                if (erroNoFilho != null) return erroNoFilho; // ACHOU!

                driver.switchTo().parentFrame(); // Volta para continuar procurando
            } catch (Exception e) {
                try { driver.switchTo().parentFrame(); } catch (Exception ex) {}
            }
        }
        return null; // Não achou em lugar nenhum
    }

    private String tentarCapturarErroNoContextoAtual() {
        // 1. Verifica SweetAlert (Popup bonito) - IGUAL SUA IMAGEM
        try {
            WebElement alerta = driver.findElement(By.id("swal2-html-container"));
            if (alerta.isDisplayed()) {
                String erro = alerta.getText();
                System.out.println("ERRO (SweetAlert) ENCONTRADO: " + erro);
                // Fecha o popup
                try { driver.findElement(By.cssSelector("button.swal2-confirm")).click(); } catch (Exception e) {}
                return erro;
            }
        } catch (Exception e) {}

        // 2. Verifica HTML5 (Validation Message no Input) via JS
        try {
            // Tenta achar o input neste frame
            List<WebElement> inputs = driver.findElements(inputNomeGrupoBy);
            if (!inputs.isEmpty()) {
                String erroHtml = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].validationMessage;", inputs.get(0));
                if (erroHtml != null && !erroHtml.isEmpty()) {
                    System.out.println("ERRO (HTML5) ENCONTRADO: " + erroHtml);
                    return erroHtml;
                }
            }
        } catch (Exception e) {}

        return null;
    }
    // --- MÉTODOS AUXILIARES ---
    private void clicarGarantido(WebElement elemento) {
        try { elemento.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
        }
    }

    private void preencher(By locator, String texto) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.click();
        element.clear();
        element.sendKeys(texto);
    }

    private void localizarFocoNoElemento(By locator, String nomeElemento) {
        long endTime = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < endTime) {
            driver.switchTo().defaultContent();
            try { wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainsystem"))); } catch (Exception e) {}

            if (buscaRecursiva(0, locator)) return;
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        throw new RuntimeException("TIMEOUT: Elemento '" + nomeElemento + "' não encontrado via Boneca Russa.");
    }

    private boolean buscaRecursiva(int nivel, By locator) {
        if (!driver.findElements(locator).isEmpty()) return true;
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for (int i = 0; i < iframes.size(); i++) {
            try {
                driver.switchTo().frame(i);
                if (buscaRecursiva(nivel + 1, locator)) return true;
                driver.switchTo().parentFrame();
            } catch (Exception e) { driver.switchTo().parentFrame(); }
        }
        return false;
    }
}