package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class PontoCarregamentoPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- SELETORES DE NAVEGAÇÃO ---
    private final By menuCadastroBy = By.xpath("//span[contains(text(), 'Cadastro')]/..");
    private final By subMenuPontosBy = By.xpath("//span[contains(text(), 'Pontos de Carregamento')]/..");

    private final By btnIncluirBy = By.id("addRecordButton");
    private final By btnGravarBy = By.cssSelector("a.webrun-form-nav-save, button.webrun-form-nav-save");

    // --- SELETORES DE CAMPOS ---
    private final By inputDescricaoBy = By.xpath("//label[contains(., 'Descrição')]/..//input");
    private final By inputGrupoBy = By.xpath("//label[contains(., 'Grupo')]/..//input[contains(@class, 'form-control')]");
    private final By comboHabilitarBy = By.xpath("//label[contains(., 'Habilitar')]/..//select");
    private final By comboAberto24hBy = By.xpath("//label[contains(., 'Aberto 24 horas')]/..//select");
    private final By inputHrAberturaBy = By.id("WFRInput1136604");
    private final By inputHrFechamentoBy = By.id("WFRInput1136603");
    private final By inputCepBy = By.xpath("//label[contains(., 'CEP')]/..//input");
    private final By btnLupaCepBy = By.xpath("//label[contains(., 'CEP')]/../..//button");
    private final By inputIdentificadorBy = By.xpath("//label[contains(., 'Identificador')]/..//input");

    public PontoCarregamentoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // --- NAVEGAÇÃO (Boneca Russa) ---
    public void acessarTelaPontosDeCarregamento() {
        localizarFocoNoElemento(menuCadastroBy, "Menu Cadastro");
        clicarGarantido(wait.until(ExpectedConditions.presenceOfElementLocated(menuCadastroBy)));

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        localizarFocoNoElemento(subMenuPontosBy, "Submenu Pontos");
        clicarGarantido(wait.until(ExpectedConditions.presenceOfElementLocated(subMenuPontosBy)));
    }

    public void iniciarInclusao() {
        localizarFocoNoElemento(btnIncluirBy, "Botão Incluir");
        clicarGarantido(wait.until(ExpectedConditions.elementToBeClickable(btnIncluirBy)));
    }

    // --- PREENCHIMENTO ---
    public void preencherCadastro(String descricao, String grupo, String habilitar, String aberto24h, String hrAbre, String hrFecha, String cep) {
        localizarFocoNoElemento(inputDescricaoBy, "Campo Descrição");

        if (!descricao.isEmpty()) preencherComJS(inputDescricaoBy, descricao);

        // Grupo (AJAX refresh)
        if (!grupo.isEmpty()) {
            try {
                WebElement input = wait.until(ExpectedConditions.elementToBeClickable(inputGrupoBy));
                input.click();
                input.clear();
                input.sendKeys(grupo);
                Thread.sleep(2000);
                input.sendKeys(Keys.ENTER);
                Thread.sleep(2000); // Aguarda refresh do Maker
            } catch (Exception e) {
                System.out.println("AVISO: Erro ao selecionar Grupo.");
            }
        }

        // Re-foca após refresh do grupo
        localizarFocoNoElemento(comboHabilitarBy, "Combo Habilitar (Re-foco)");
        selecionarCombo(comboHabilitarBy, habilitar);
        selecionarCombo(comboAberto24hBy, aberto24h);

        if (!hrAbre.isEmpty()) preencherComJS(inputHrAberturaBy, hrAbre);
        if (!hrFecha.isEmpty()) preencherComJS(inputHrFechamentoBy, hrFecha);

        if (!cep.isEmpty()) {
            preencherComJS(inputCepBy, cep);
            try {
                clicarGarantido(wait.until(ExpectedConditions.elementToBeClickable(btnLupaCepBy)));
                Thread.sleep(4500);
                localizarFocoNoElemento(inputCepBy, "CEP (Re-foco)");
            } catch (Exception e) {}
        }
    }

    public void salvarRegistro() {
        localizarFocoNoElemento(btnGravarBy, "Botão Salvar");
        clicarGarantido(driver.findElement(btnGravarBy));
        try {
            Thread.sleep(1000);
            driver.switchTo().activeElement().sendKeys(Keys.chord(Keys.CONTROL, "s"));
        } catch (Exception e) {}
    }

    // --- MÉTODOS TÉCNICOS (Onde estava o erro) ---

    private void preencherComJS(By locator, String texto) {
        // Localiza o elemento usando a estratégia de busca recursiva se necessário
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

        // Scroll para o elemento para evitar erro de coordenada -422
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", element, texto);
        js.executeScript("arguments[0].dispatchEvent(new Event('input'));", element);
        js.executeScript("arguments[0].dispatchEvent(new Event('change'));", element);
    }

    private void clicarGarantido(WebElement elemento) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", elemento);
            elemento.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
        }
    }

    // --- VALIDAÇÃO E BONECA RUSSA ---

    public boolean validarSeRegistroFoiSalvo() {
        try {
            Thread.sleep(3000);
            localizarFocoNoElemento(inputIdentificadorBy, "Campo ID");
            String valorId = driver.findElement(inputIdentificadorBy).getAttribute("value");
            return valorId != null && !valorId.isEmpty();
        } catch (Exception e) { return false; }
    }

    public String obterMensagemErro() {
        driver.switchTo().defaultContent();
        String erro = buscarErroRecursivamente(driver);
        return erro != null ? erro : "Nenhum erro visível capturado.";
    }

    private String buscarErroRecursivamente(WebDriver driver) {
        try {
            WebElement alerta = driver.findElement(By.id("swal2-html-container"));
            if (alerta.isDisplayed()) {
                String msg = alerta.getText();
                try { driver.findElement(By.cssSelector("button.swal2-confirm")).click(); } catch (Exception e) {}
                return msg;
            }
        } catch (Exception e) {}

        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for (WebElement iframe : iframes) {
            try {
                driver.switchTo().frame(iframe);
                String erro = buscarErroRecursivamente(driver);
                if (erro != null) return erro;
                driver.switchTo().parentFrame();
            } catch (Exception e) { try { driver.switchTo().parentFrame(); } catch (Exception ex) {} }
        }
        return null;
    }

    private void selecionarCombo(By locator, String valor) {
        if (valor == null || valor.isEmpty()) return;
        try {
            new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(locator))).selectByVisibleText(valor);
        } catch (Exception e) {}
    }

    private void localizarFocoNoElemento(By locator, String nome) {
        long endTime = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < endTime) {
            driver.switchTo().defaultContent();
            if (buscaRecursiva(0, locator)) return;
            try {
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainsystem")));
                if (buscaRecursiva(0, locator)) return;
            } catch (Exception e) {}
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        throw new RuntimeException("TIMEOUT: Elemento '" + nome + "' não encontrado.");
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