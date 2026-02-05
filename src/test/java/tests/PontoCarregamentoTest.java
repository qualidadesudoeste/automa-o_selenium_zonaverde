package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.LoginPage;
import pages.MenuPage;
import pages.PontoCarregamentoPage;
import utils.ScreenshotUtils;

public class PontoCarregamentoTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private MenuPage menuPage;
    private PontoCarregamentoPage pontoPage;

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            if (driver != null) ScreenshotUtils.tirarPrint(driver, description.getMethodName());
        }
        @Override
        protected void finished(Description description) {
            if (driver != null) driver.quit();
        }
    };

    @Before
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--headless=new");

        driver = new ChromeDriver(options);
        driver.get("https://app.makernocode.dev/open.do?sys=OFK");

        loginPage = new LoginPage(driver);
        menuPage = new MenuPage(driver);
        pontoPage = new PontoCarregamentoPage(driver);

        // Realiza login antes de cada teste
        loginPage.realizarLogin("qualidade", "1");
    }

    @Test
    public void testeIncluirPontoCarregamento() {
        pontoPage.acessarTelaPontosDeCarregamento();
        pontoPage.iniciarInclusao();

        // Preenche tudo corretamente
        pontoPage.preencherCadastro(
                "Ponto Teste Automatizado 2", // Descrição
                "Estações do Setor Fiscal", // Grupo (Lookup)
                "Sim",                      // Habilitar
                "Sim",                      // Aberto 24h
                "08:00",                    // Abertura
                "22:00",                    // Fechamento
                "41820-560"                 // CEP
        );

        pontoPage.salvarRegistro();

        Assert.assertTrue("FALHA: O registro não gerou um ID (não foi salvo).",
                pontoPage.validarSeRegistroFoiSalvo());
    }

    @Test
    public void testeValidarCamposObrigatorios() {
        pontoPage.acessarTelaPontosDeCarregamento();
        pontoPage.iniciarInclusao();


        pontoPage.preencherCadastro("", "", "", "", "", "", "");

        pontoPage.salvarRegistro();

        // Captura o erro do SweetAlert
        String erro = pontoPage.obterMensagemErro();
        System.out.println("DEBUG: Mensagem capturada: " + erro);

        // Valida se a mensagem reclama da Descrição
        boolean achouErro = erro.toLowerCase().contains("descrição") ||
                erro.toLowerCase().contains("obrigatório") ||
                erro.toLowerCase().contains("preencha");

        Assert.assertTrue("FALHA: Sistema permitiu salvar sem Descrição! Msg: " + erro, achouErro);
    }
}