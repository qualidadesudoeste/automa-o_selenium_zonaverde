package tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.ConectoresPage;
import pages.LoginPage;
import pages.MenuPage;
import utils.ScreenshotUtils;

public class ConectoresTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private MenuPage menuPage;
    private ConectoresPage conectoresPage;

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
        conectoresPage = new ConectoresPage(driver);

        loginPage.realizarLogin("qualidade", "1");
        if (!menuPage.validarLoginComSucesso()) throw new RuntimeException("Erro Crítico: Login falhou.");
    }

    @Test
    public void testeIncluirConectorComSucesso() {
        conectoresPage.acessarTelaConectores();
        conectoresPage.iniciarInclusao();
        conectoresPage.preencherCadastro("Conector Tipo 3");
        conectoresPage.salvarRegistro();
        Assert.assertTrue("Falha ao salvar!", conectoresPage.validarSeRegistroFoiSalvo());
    }

    @Test
    public void testeValidarCamposObrigatorios() {
        // 1. Navegação
        conectoresPage.acessarTelaConectores();
        conectoresPage.iniciarInclusao();

        // 2. Deixa o campo Nome VAZIO ("")
        conectoresPage.preencherCadastro("");

        // 3. Tenta Salvar
        conectoresPage.salvarRegistro();

        // 4. Validação: Esperamos uma mensagem de erro
        String erro = conectoresPage.obterMensagemErro();

        // Verifica se a mensagem indica obrigatoriedade (pode ser "Preencha..." ou "Obrigatório")
        boolean achouErro = erro.toLowerCase().contains("preencha") || erro.toLowerCase().contains("obrigatório");

        Assert.assertTrue("FALHA: O sistema permitiu salvar ou não mostrou erro! Mensagem: " + erro, achouErro);
    }

    @Test
    public void testeValidarDuplicidadeDeConector() {
        // 1. Navegação
        conectoresPage.acessarTelaConectores();
        conectoresPage.iniciarInclusao();

        // 2. Preenchimento com nome JÁ EXISTENTE
        // Usamos o nome que você confirmou que existe no banco
        String nomeExistente = "Conector Tipo Light";
        System.out.println("DEBUG: Tentando cadastrar duplicado: " + nomeExistente);

        conectoresPage.preencherCadastro(nomeExistente);

        // 3. Tenta Salvar
        conectoresPage.salvarRegistro();

        // 4. Validação: O sistema DEVE exibir o Popup de erro
        String erroCapturado = conectoresPage.obterMensagemErro();

        // Verifica se a mensagem contém palavras-chave de duplicidade
        boolean erroDuplicidade = erroCapturado.toLowerCase().contains("duplicado") ||
                erroCapturado.toLowerCase().contains("já está cadastrado");

        // Se NÃO apareceu erro de duplicidade, verificamos se ele salvou (o que seria uma falha grave)
        if (!erroDuplicidade) {
            boolean salvou = conectoresPage.validarSeRegistroFoiSalvo();
            if (salvou) {
                Assert.fail("FALHA GRAVE: O sistema permitiu cadastrar duplicado! ID gerado para: " + nomeExistente);
            } else {
                Assert.fail("FALHA: O sistema não salvou, mas exibiu um erro inesperado: " + erroCapturado);
            }
        }

        // Se chegou aqui, o erro foi capturado corretamente
        System.out.println("SUCESSO: Bloqueio de duplicidade funcionou. Mensagem: " + erroCapturado);
    }
}