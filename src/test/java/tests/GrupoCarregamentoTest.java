package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.GrupoCarregamentoPage;
import pages.LoginPage;
import pages.MenuPage;
import utils.ScreenshotUtils;

public class GrupoCarregamentoTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private MenuPage menuPage;
    private GrupoCarregamentoPage grupoPage;

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
        grupoPage = new GrupoCarregamentoPage(driver);

        loginPage.realizarLogin("qualidade", "1");
        if (!menuPage.validarLoginComSucesso()) throw new RuntimeException("Erro Crítico: Login falhou.");
    }

    @Test
    public void testeIncluirGrupoCarregamento() {
        grupoPage.acessarTelaGrupoCarregamento();
        grupoPage.iniciarInclusao();
        grupoPage.preencherCadastro("Grupo Teste Auto");
        grupoPage.salvarRegistro();
        Assert.assertTrue("Falha ao salvar!", grupoPage.validarSeRegistroFoiSalvo());
    }

    @Test
    public void testeValidarCamposObrigatorios() {
        // 1. Navegação
        grupoPage.acessarTelaGrupoCarregamento();
        grupoPage.iniciarInclusao();

        // 2. Tenta salvar VAZIO ("")
        grupoPage.preencherCadastro("");
        grupoPage.salvarRegistro();

        // 3. Validação: Verifica mensagem de erro
        String erro = grupoPage.obterMensagemErro();

        boolean achouErro = erro.toLowerCase().contains("preencha") || erro.toLowerCase().contains("obrigatório");

        Assert.assertTrue("FALHA: Sistema permitiu salvar campo vazio! Msg: " + erro, achouErro);
    }
}