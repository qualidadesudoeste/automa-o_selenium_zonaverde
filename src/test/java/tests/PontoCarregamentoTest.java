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
        // 1. Configurações do Navegador (Essencial para rodar no Servidor/GitHub)
        ChromeOptions options = new ChromeOptions();

        // -- CONFIGURAÇÃO HEADLESS --
        // O modo "headless=new" faz o teste rodar sem abrir a janela gráfica.
        // ISSO É OBRIGATÓRIO PARA O GITHUB ACTIONS.
        // Se quiser ver o navegador abrindo no seu PC para debugar,
        // comente a linha abaixo com //
        options.addArguments("--headless=new");

        // -- CONFIGURAÇÃO DE TAMANHO --
        // Em modo headless, o navegador pode abrir muito pequeno (tipo celular).
        // Forçamos Full HD para garantir que todos os botões e menus apareçam.
        options.addArguments("--window-size=1920,1080");

        // -- CONFIGURAÇÕES DE ESTABILIDADE (Para evitar crashes no Linux) --
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // 2. Inicializa o Driver COM as opções
        driver = new ChromeDriver(options);

        // Navegação
        driver.get("https://app.makernocode.dev/open.do?sys=OFK");

        // 3. Inicializa as Pages
        // (Certifique-se de inicializar todas que seu teste específico for usar)
        loginPage = new LoginPage(driver);
        menuPage = new MenuPage(driver);

        pontoPage = new PontoCarregamentoPage(driver);
        // Inicialize aqui a página específica que esse arquivo de teste vai usar
        // Exemplo: se for o teste de Conectores, descomente abaixo:
        // conectoresPage = new ConectoresPage(driver);
        // cadastroPage = new CadastroPage(driver);

        // --- PRÉ-CONDIÇÃO: ESTAR LOGADO ---
        loginPage.realizarLogin("qualidade", "1");

        // Garante que o menu carregou antes de começar qualquer teste
        if (!menuPage.validarLoginComSucesso()) {
            throw new RuntimeException("Falha crítica: Não foi possível logar para iniciar o teste.");
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