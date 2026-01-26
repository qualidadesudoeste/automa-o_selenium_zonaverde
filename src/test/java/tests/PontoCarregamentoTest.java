package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.LoginPage;
import pages.MenuPage;
import pages.PontoCarregamentoPage; // Importe a nova página
import utils.ScreenshotUtils;

public class PontoCarregamentoTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private MenuPage menuPage;
    private PontoCarregamentoPage pontoPage; // Nova página

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
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://app.makernocode.dev/open.do?sys=OFK");

        loginPage = new LoginPage(driver);
        menuPage = new MenuPage(driver);
        pontoPage = new PontoCarregamentoPage(driver); // Instancia a nova página

        loginPage.realizarLogin("qualidade", "1");

        if (!menuPage.validarLoginComSucesso()) {
            throw new RuntimeException("Erro Crítico: Login falhou.");
        }
    }

    @Test
    public void testeIncluirPontoCarregamento() {
        pontoPage.acessarTelaPontosDeCarregamento();
        pontoPage.iniciarInclusao();

        pontoPage.preencherCadastro(
                "Ponto Auto",                 // Descrição mais curta
                "Estações do Setor Fiscal",
                "Sim",
                "Não",
                "07:00",
                "17:00",
                "40060-055"                   // CEP NOVO (Endereço mais curto para testar)
        );

        pontoPage.salvarRegistro();

        boolean salvou = pontoPage.validarSeRegistroFoiSalvo();

        // Se não salvou, a gente pega o erro e falha o teste mostrando o motivo
        if (!salvou) {
            String erroSistema = pontoPage.obterMensagemErro();
            Assert.fail("FALHA AO SALVAR: O sistema retornou o erro: " + erroSistema);
        }

        Assert.assertTrue("Sucesso! ID gerado.", salvou);
    }
}