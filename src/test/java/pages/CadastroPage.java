package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class CadastroPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- SELETORES ---
    private By menuCadastroBy = By.xpath("//span[contains(text(), 'Cadastro')]/..");
    private By subMenuClientesBy = By.xpath("//span[contains(text(), 'Clientes')]/..");
    private By btnIncluirBy = By.id("addRecordButton");

    // Campos
    private By inputNomeBy = By.xpath("//input[@placeholder='Ex: João da Silva']");
    private By inputCpfBy = By.xpath("//input[@placeholder='000.000.000-00']");
    private By inputNascBy = By.xpath("//input[@placeholder='DD/MM/AAAA']");
    private By inputTelefoneBy = By.xpath("//input[@placeholder='(00) 00000-0000']");
    private By inputEmailBy = By.xpath("//input[@placeholder='Ex: joao.silva@email.com']");
    private By inputSenhaBy = By.xpath("//input[@placeholder='Digite sua senha']");
    private By inputCepBy = By.xpath("//input[@placeholder='00000-000']");

    // LUPA DO CEP: Usando o ícone magnifying-glass que você enviou
    private By btnLupaCepBy = By.xpath("//i[contains(@class, 'fa-magnifying-glass')]/ancestor::button");
    private By inputComplementoBy = By.xpath("//input[@placeholder='Ex: Apto 101, Bloco B']");
    private By inputNumeroBy = By.xpath("//input[@placeholder='Ex: 123']");


    private By inputIdClienteBy = By.xpath("//label[contains(text(), 'Id Cliente')]/..//input");
    private By btnGravarBy = By.cssSelector("a.webrun-form-nav-save");

    public CadastroPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void acessarTelaClientes() {
        wait.until(ExpectedConditions.elementToBeClickable(menuCadastroBy)).click();
        try { Thread.sleep(800); } catch (InterruptedException e) {}
        WebElement linkClientes = wait.until(ExpectedConditions.elementToBeClickable(subMenuClientesBy));
        clicarGarantido(linkClientes);
    }

    public void iniciarInclusao() {
        localizarFocoNoElemento(btnIncluirBy, "Botão Incluir");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnIncluirBy));
        clicarGarantido(btn);
    }

    public void preencherDadosObrigatorios(String nome, String cpf, String nasc, String tel, String email, String senha, String cep, String numero, String complemento) {
        localizarFocoNoElemento(inputNomeBy, "Campo Nome");

        preencher(inputNomeBy, nome);
        preencher(inputCpfBy, cpf);
        preencher(inputNascBy, nasc);
        driver.findElement(inputNascBy).sendKeys(Keys.TAB);

        preencher(inputTelefoneBy, tel);
        preencher(inputEmailBy, email);
        preencher(inputSenhaBy, senha);

        // CEP e Clique na Lupa
        scrollToElement(inputCepBy);
        preencher(inputCepBy, cep);

        System.out.println("DEBUG: Clicando na Lupa do CEP...");
        WebElement lupa = wait.until(ExpectedConditions.elementToBeClickable(btnLupaCepBy));
        clicarGarantido(lupa);

        // IMPORTANTE: Espera o sistema Maker terminar de processar o CEP
        System.out.println("DEBUG: Aguardando retorno da busca do CEP...");
        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        preencher(inputComplementoBy, complemento);
        preencher(inputNumeroBy, numero);
    }

    public void salvarRegistro() {
        // 1. Busca recursiva para garantir que o driver achou o frame da barra de ferramentas
        localizarFocoNoElemento(btnGravarBy, "Botão Salvar");

        System.out.println("DEBUG: Executando clique no botão Salvar (webrun-form-nav-save)...");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(btnGravarBy));

        // No Maker, o clique via JS é o mais seguro para botões de barra
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        // Backup de Teclado: Algumas versões do Maker só salvam via atalho
        try {
            Thread.sleep(1000);
            driver.switchTo().activeElement().sendKeys(Keys.chord(Keys.CONTROL, "s"));
            System.out.println("DEBUG: Backup CTRL+S enviado.");
        } catch (Exception e) {}
    }

    public String obterIdGerado() {
        try {
            // Aguarda o campo ser preenchido pelo sistema após o CTRL+S
            Thread.sleep(2000);
            WebElement inputId = wait.until(ExpectedConditions.visibilityOfElementLocated(inputIdClienteBy));

            String idGerado = inputId.getAttribute("value");
            System.out.println("DEBUG: ID do Cliente após salvar: " + idGerado);

            return idGerado;
        } catch (Exception e) {
            return "";
        }
    }

    public boolean validarSeRegistroFoiSalvo() {
        try {
            System.out.println("DEBUG: Iniciando validação pós-salvamento...");
            // 1. Aguarda um pouco para o banco processar e o campo atualizar
            Thread.sleep(4000);

            // 2. O PULO DO GATO: Refazer a busca recursiva para garantir que o driver
            // reencontrou o iframe onde o ID 17 está sendo exibido
            localizarFocoNoElemento(inputIdClienteBy, "Campo Id Cliente");

            WebElement inputId = driver.findElement(inputIdClienteBy);
            String valorId = inputId.getAttribute("value").trim();

            System.out.println("DEBUG: Valor capturado no Id Cliente: " + valorId);

            // 3. Validação técnica: tenta converter o valor em número
            int idNumerico = Integer.parseInt(valorId);

            if (idNumerico > 0) {
                System.out.println("SUCESSO TOTAL: Registro confirmado com o ID: " + idNumerico);
                return true;
            }
        } catch (Exception e) {
            System.out.println("ERRO: Falha ao validar salvamento: " + e.getMessage());
        }
        return false;
    }

    // --- FUNÇÕES DE REFORÇO ---

    private void clicarGarantido(WebElement elemento) {
        try {
            elemento.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
        }
    }

    private void localizarFocoNoElemento(By locator, String nomeElemento) {
        long endTime = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < endTime) {
            driver.switchTo().defaultContent();
            try { wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainsystem"))); } catch (Exception e) { continue; }

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
}