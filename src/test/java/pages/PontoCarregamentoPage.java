package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class PontoCarregamentoPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- SELETORES DE NAVEGAÇÃO ---
    private By menuCadastroBy = By.xpath("//span[contains(text(), 'Cadastro')]/..");
    private By subMenuPontosBy = By.xpath("//span[contains(text(), 'Pontos de Carregamento')]/..");

    private By btnIncluirBy = By.id("addRecordButton");
    private By btnGravarBy = By.cssSelector("a.webrun-form-nav-save, button.webrun-form-nav-save");

    // --- SELETORES DE CAMPOS ---
    private By inputDescricaoBy = By.xpath("//label[contains(., 'Descrição')]/..//input");
    private By inputGrupoBy = By.xpath("//label[contains(., 'Grupo')]/..//input[contains(@class, 'form-control')]");
    private By comboHabilitarBy = By.xpath("//label[contains(., 'Habilitar')]/..//select");
    private By comboAberto24hBy = By.xpath("//label[contains(., 'Aberto 24 horas')]/..//select");
    private By inputHrAberturaBy = By.xpath("//label[contains(., 'Horário de Abertura')]/..//input");
    private By inputHrFechamentoBy = By.xpath("//label[contains(., 'Horário de Fechamento')]/..//input");
    private By inputCepBy = By.xpath("//label[contains(., 'CEP')]/..//input");
    private By btnLupaCepBy = By.xpath("//label[contains(., 'CEP')]/../..//button");

    private By inputIdentificadorBy = By.xpath("//label[contains(., 'Identificador')]/..//input");

    public PontoCarregamentoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // --- NAVEGAÇÃO COM BONECA RUSSA (SOLICITADO) ---
    public void acessarTelaPontosDeCarregamento() {
        // 1. Aplica Boneca Russa para achar o Menu Cadastro
        // Isso garante que estamos no iframe certo antes de tentar clicar
        localizarFocoNoElemento(menuCadastroBy, "Menu Cadastro");

        // Elemento localizado, agora espera ser clicável e clica (com JS por segurança)
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(menuCadastroBy));
        clicarGarantido(menu);

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // 2. Aplica Boneca Russa para achar o Submenu
        localizarFocoNoElemento(subMenuPontosBy, "Submenu Pontos de Carregamento");

        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(subMenuPontosBy));
        clicarGarantido(link);
    }

    public void iniciarInclusao() {
        localizarFocoNoElemento(btnIncluirBy, "Botão Incluir");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnIncluirBy));
        clicarGarantido(btn);
    }

    // --- PREENCHIMENTO ---
    public void preencherCadastro(String descricao, String grupo, String habilitar, String aberto24h, String hrAbre, String hrFecha, String cep) {
        // Foca no frame usando Descrição
        localizarFocoNoElemento(inputDescricaoBy, "Campo Descrição");

        if (!descricao.isEmpty()) preencherComScroll(inputDescricaoBy, descricao);

        if (!grupo.isEmpty()) {
            try {
                WebElement input = wait.until(ExpectedConditions.elementToBeClickable(inputGrupoBy));
                input.click();
                input.clear();
                input.sendKeys(grupo);
                Thread.sleep(2000);
                By itemLista = By.xpath("//li[contains(@class, 'list-group-item') and contains(., '" + grupo + "')]");
                wait.until(ExpectedConditions.elementToBeClickable(itemLista)).click();
            } catch (Exception e) {
                System.out.println("AVISO: Erro ao selecionar Grupo.");
            }
        }

        selecionarCombo(comboHabilitarBy, habilitar);
        selecionarCombo(comboAberto24hBy, aberto24h);

        if (!hrAbre.isEmpty()) preencherComScroll(inputHrAberturaBy, hrAbre);
        if (!hrFecha.isEmpty()) preencherComScroll(inputHrFechamentoBy, hrFecha);

        if (!cep.isEmpty()) {
            preencherComScroll(inputCepBy, cep);
            try {
                WebElement btnLupa = wait.until(ExpectedConditions.elementToBeClickable(btnLupaCepBy));
                clicarGarantido(btnLupa);
                Thread.sleep(4000);
            } catch (Exception e) {}
        }
    }

    public void salvarRegistro() {
        localizarFocoNoElemento(btnGravarBy, "Botão Salvar");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(btnGravarBy));
        clicarGarantido(btn);

        try {
            Thread.sleep(1000);
            driver.switchTo().activeElement().sendKeys(Keys.chord(Keys.CONTROL, "s"));
        } catch (Exception e) {}
    }

    // --- VALIDAÇÃO ---
    public boolean validarSeRegistroFoiSalvo() {
        try {
            Thread.sleep(3000);
            localizarFocoNoElemento(inputIdentificadorBy, "Campo ID");
            String valorId = driver.findElement(inputIdentificadorBy).getAttribute("value");
            System.out.println("DEBUG: ID Gerado: " + valorId);
            return valorId != null && !valorId.isEmpty();
        } catch (Exception e) { return false; }
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

    // --- MÉTODOS AUXILIARES ---

    private void clicarGarantido(WebElement elemento) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", elemento);
            Thread.sleep(200);
            elemento.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
        }
    }

    private void preencherComScroll(By locator, String texto) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try { Thread.sleep(300); } catch (InterruptedException e) {}

        element.clear();
        element.sendKeys(texto);
    }

    private void selecionarCombo(By locator, String valorVisivel) {
        if (valorVisivel == null || valorVisivel.isEmpty()) return;
        try {
            WebElement select = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            new Select(select).selectByVisibleText(valorVisivel);
        } catch (Exception e) {}
    }

    // --- LÓGICA DE BONECA RUSSA (Melhorada para verificar Raiz e Mainsystem) ---
    private void localizarFocoNoElemento(By locator, String nomeElemento) {
        long endTime = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < endTime) {

            // 1. Tenta buscar na Raiz (Default Content) primeiro
            driver.switchTo().defaultContent();
            if (buscaRecursiva(0, locator)) return;

            // 2. Se não achou, entra no 'mainsystem' e busca lá dentro
            try {
                driver.switchTo().defaultContent();
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainsystem")));
                if (buscaRecursiva(0, locator)) return;
            } catch (Exception e) {}

            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        throw new RuntimeException("TIMEOUT: Elemento '" + nomeElemento + "' não encontrado.");
    }

    private boolean buscaRecursiva(int nivel, By locator) {
        // Verifica no nível atual
        if (!driver.findElements(locator).isEmpty()) return true;

        // Mergulha nos iframes filhos
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
            List<WebElement> inputs = driver.findElements(inputDescricaoBy);
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
    }