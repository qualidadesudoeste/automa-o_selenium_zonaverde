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
    private By btnGravarBy = By.cssSelector("a.webrun-form-nav-save");

    // --- SELETORES DE CAMPOS (Estratégia do Label) ---
    private By inputDescricaoBy = By.xpath("//label[contains(., 'Descrição')]/..//input");

    // Novos Campos
    private By inputGrupoBy = By.xpath("//label[contains(., 'Grupo')]/..//input[contains(@class, 'form-control')]");
    private By comboHabilitarBy = By.xpath("//label[contains(., 'Habilitar')]/..//select");
    private By comboAberto24hBy = By.xpath("//label[contains(., 'Aberto 24 horas')]/..//select");

    private By inputHrAberturaBy = By.xpath("//label[contains(., 'Horário de Abertura')]/..//input");
    private By inputHrFechamentoBy = By.xpath("//label[contains(., 'Horário de Fechamento')]/..//input");

    // Endereço
    private By inputCepBy = By.xpath("//label[contains(., 'CEP')]/..//input");
    // Botão Lupa: Pega o botão que está 'vizinho' ao campo CEP
    private By btnLupaCepBy = By.xpath("//label[contains(., 'CEP')]/../..//button");

    // Identificador (Somente para validação, não escrita)
    private By inputIdentificadorBy = By.xpath("//label[contains(., 'Identificador')]/..//input");


    public PontoCarregamentoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void acessarTelaPontosDeCarregamento() {
        wait.until(ExpectedConditions.elementToBeClickable(menuCadastroBy)).click();
        try { Thread.sleep(800); } catch (InterruptedException e) {}
        WebElement linkPontos = wait.until(ExpectedConditions.elementToBeClickable(subMenuPontosBy));
        clicarGarantido(linkPontos);
    }

    public void iniciarInclusao() {
        localizarFocoNoElemento(btnIncluirBy, "Botão Incluir");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnIncluirBy));
        clicarGarantido(btn);
    }

    // Método Mestre atualizado com todos os campos novos
    public void preencherCadastro(String descricao, String grupo, String habilitar, String aberto24h, String hrAbre, String hrFecha, String cep) {
        // 1. Foca no frame usando Descrição como âncora
        localizarFocoNoElemento(inputDescricaoBy, "Campo Descrição");

        if (!descricao.isEmpty()) preencher(inputDescricaoBy, descricao);

        // --- PREENCHIMENTO DO GRUPO (LOOKUP) ---
        if (!grupo.isEmpty()) {
            try {
                System.out.println("DEBUG: Preenchendo Grupo...");
                WebElement input = wait.until(ExpectedConditions.elementToBeClickable(inputGrupoBy));
                input.click();
                input.clear();
                input.sendKeys(grupo);

                // Espera a lista carregar (baseado na image_61e092.png)
                Thread.sleep(2000);

                // Clica no primeiro item da lista que contem o texto digitado
                By itemLista = By.xpath("//li[contains(@class, 'list-group-item') and contains(., '" + grupo + "')]");
                wait.until(ExpectedConditions.elementToBeClickable(itemLista)).click();

            } catch (Exception e) {
                System.out.println("AVISO: Erro ao selecionar Grupo: " + e.getMessage());
            }
        }

        // --- COMBOS (SELECT) ---
        selecionarCombo(comboHabilitarBy, habilitar); // Sim/Não
        selecionarCombo(comboAberto24hBy, aberto24h); // Sim/Não

        // --- HORÁRIOS ---
        if (!hrAbre.isEmpty()) preencher(inputHrAberturaBy, hrAbre);
        if (!hrFecha.isEmpty()) preencher(inputHrFechamentoBy, hrFecha);

        // --- CEP E BUSCA AUTOMÁTICA ---
        if (!cep.isEmpty()) {
            scrollToElement(inputCepBy);
            preencher(inputCepBy, cep);

            System.out.println("DEBUG: Clicando na lupa do CEP...");
            try {
                WebElement btnLupa = wait.until(ExpectedConditions.elementToBeClickable(btnLupaCepBy));
                clicarGarantido(btnLupa);
                // Pausa para o sistema preencher o endereço
                Thread.sleep(4000);
            } catch (Exception e) {
                System.out.println("AVISO: Falha ao clicar na lupa do CEP.");
            }
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
            Thread.sleep(3000); // Tempo para o refresh da tela

            // 1. RE-LOCALIZA O IFRAME (BONECA RUSSA)
            // Se não fizermos isso, o driver estará perdido no 'Default Content' e não achará o ID
            localizarFocoNoElemento(inputIdentificadorBy, "Campo Identificador (Validação)");

            // 2. Agora sim, pega o valor
            WebElement inputId = driver.findElement(inputIdentificadorBy);
            String valorId = inputId.getAttribute("value");

            System.out.println("DEBUG: ID Gerado encontrado: " + valorId);
            return valorId != null && !valorId.isEmpty();
        } catch (Exception e) {
            System.out.println("DEBUG: Erro na validação: " + e.getMessage());
            return false;
        }
    }

    public String obterMensagemErro() {
        try {
            driver.switchTo().defaultContent();
            WebElement alerta = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("swal2-html-container")));
            String erro = alerta.getText();
            System.out.println("ERRO DO SISTEMA: " + erro);
            try { driver.findElement(By.cssSelector("button.swal2-confirm")).click(); } catch (Exception e) {}
            return erro;
        } catch (Exception e) {
            return "Nenhum erro visível capturado.";
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void selecionarCombo(By locator, String valorVisivel) {
        if (valorVisivel == null || valorVisivel.isEmpty()) return;
        try {
            WebElement selectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            Select select = new Select(selectElement);
            // Tenta selecionar pelo texto visível (Ex: "Sim", "Não")
            select.selectByVisibleText(valorVisivel);
        } catch (Exception e) {
            System.out.println("AVISO: Falha ao selecionar combo: " + valorVisivel);
        }
    }

    private void clicarGarantido(WebElement elemento) {
        try {
            elemento.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
        }
    }

    private void preencher(By locator, String texto) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.click();
        element.clear();
        element.sendKeys(texto);
    }

    private void scrollToElement(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        } catch (Exception e) {}
    }

    private void localizarFocoNoElemento(By locator, String nomeElemento) {
        long endTime = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < endTime) {
            driver.switchTo().defaultContent();
            try { wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainsystem"))); } catch (Exception e) {}

            if (buscaRecursiva(0, locator)) return;
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        throw new RuntimeException("TIMEOUT: Elemento '" + nomeElemento + "' não encontrado.");
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