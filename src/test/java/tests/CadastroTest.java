package tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.CadastroPage;
import pages.LoginPage;
import pages.MenuPage;
import utils.CpfProvider; // <--- Importação da nova classe utilitária

public class CadastroTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private MenuPage menuPage;
    private CadastroPage cadastroPage;

    @Before
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://app.makernocode.dev/open.do?sys=OFK");

        // Inicializa as páginas
        loginPage = new LoginPage(driver);
        menuPage = new MenuPage(driver);
        cadastroPage = new CadastroPage(driver);

        // --- PRÉ-CONDIÇÃO: ESTAR LOGADO ---
        loginPage.realizarLogin("qualidade", "1");

        // Garante que o menu carregou
        if (!menuPage.validarLoginComSucesso()) {
            throw new RuntimeException("Falha crítica: Não foi possível logar para iniciar o teste de cadastro.");
        }
    }

    @Test
    public void testeCadastrarClienteComSucesso() {
        // 1. Navegar até a tela
        cadastroPage.acessarTelaClientes();

        // 2. Clicar em Incluir
        cadastroPage.iniciarInclusao();

        // --- ALTERAÇÃO AQUI: Obtém um CPF aleatório da lista de 50 ---
        String cpfDinamico = CpfProvider.obterCpfAleatorio();
        System.out.println("DEBUG: Testando cadastro com CPF: " + cpfDinamico);

        // 3. Preencher o formulário
        cadastroPage.preencherDadosObrigatorios(
                "Juao Selenium Silva",
                cpfDinamico,           // <--- Usando a variável dinâmica em vez do fixo
                "01/01/1990",
                "(11) 99999-9999",
                "teste.selenium@email.com",
                "123456",
                "40060-055",
                "100",
                "casa"
        );

        // 4. Salvar
        cadastroPage.salvarRegistro();

        // VALIDAÇÃO REAL:
        // Asserção: Verifica se o campo ID agora contém um número inteiro válido
        org.junit.Assert.assertTrue("O registro não foi salvo ou o ID não foi localizado!",
                cadastroPage.validarSeRegistroFoiSalvo());
    }

    @After
    public void fechar(){
        if (driver != null) {
            driver.quit();
        }
    }
}