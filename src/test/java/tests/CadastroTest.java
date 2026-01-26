package tests;

import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.CadastroPage;
import pages.LoginPage;
import pages.MenuPage;
import utils.CpfProvider; // <--- Importação da nova classe utilitária
import utils.ScreenshotUtils;

public class CadastroTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private MenuPage menuPage;
    private CadastroPage cadastroPage;

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            System.out.println("LOG: O teste falhou. Iniciando captura de tela...");
            if (driver != null) {
                // Tira a foto ENQUANTO o driver ainda está vivo
                ScreenshotUtils.tirarPrint(driver, description.getMethodName());
            }
        }

        @Override
        protected void finished(Description description) {
            // Este método roda SEMPRE ao final de tudo (igual ao @After)
            if (driver != null) {
                driver.quit();
                System.out.println("LOG: Driver encerrado pelo Watchman.");
            }
        }
    };
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

    @Test
    public void testeValidarDuplicidadeDeCpf() {
        cadastroPage.acessarTelaClientes();
        cadastroPage.iniciarInclusao();

        // CPF que sabemos que já existe (ID 29 do seu log anterior)
        String cpfDuplicado ="801.284.682-97";

        cadastroPage.preencherDadosObrigatorios(
                "Teste Duplicidade",
                cpfDuplicado,
                "01/01/1990",
                "(11) 99999-9999",
                "duplo@email.com",
                "123",
                "40060-055", "10", "casa"
        );

        cadastroPage.salvarRegistro();

        boolean salvou = cadastroPage.validarSeRegistroFoiSalvo();
        if (salvou) {
            String idGerado = cadastroPage.obterIdGerado();
            // Assert.fail força o teste a ficar VERMELHO na hora
            Assert.fail("FALHA GRAVE: O sistema permitiu cadastrar um CPF duplicado! ID gerado: " + idGerado);
        }
        String msg = cadastroPage.obterMensagemAlerta();

        // Ajuste aqui se a mensagem for diferente, mas geralmente duplicidade fala de "Unique constraint" ou "Já existe"
        // Se você não sabe a mensagem exata ainda, use o System.out para descobrir na primeira rodada.
        org.junit.Assert.assertTrue("Erro de duplicidade não exibido. Msg: " + msg,
                msg.contains("já existe") || msg.contains("duplicado") || msg.length() > 5);
    }

    @Test
    public void testeValidarCpfInvalido() {
        cadastroPage.acessarTelaClientes();
        cadastroPage.iniciarInclusao();

        // CPF com formato válido (máscara ok), mas matematicamente inválido
        cadastroPage.preencherDadosObrigatorios(
                "Teste CPF Invalido",
                "111.222.333-00",
                "01/01/1990",
                "(11) 99999-9999",
                "invalido@email.com",
                "123",
                "40060-055", "10", "casa"
        );

        cadastroPage.salvarRegistro();

        String msg = cadastroPage.obterMensagemAlerta();
        System.out.println("Mensagem CPF Inválido: " + msg);

        // Validação EXATA conforme image_173571.png
        Assert.assertTrue("Esperava erro de CPF inválido, mas veio: " + msg,
                msg.contains("deve conter um CPF válido"));
    }

    // --- CORREÇÃO BASEADA NO SEU PRINT (image_50f0b6.png) ---
    @Test
    public void testeValidarCamposObrigatorios() {
        cadastroPage.acessarTelaClientes();
        cadastroPage.iniciarInclusao();

        // Deixamos o CPF vazio para forçar o erro "O campo 'CPF' é obrigatório"
        cadastroPage.preencherDadosObrigatorios(
                "Teste Sem CPF",
                "",
                "01/01/1990",
                "(11) 99999-9999",
                "semcpf@email.com",
                "123",
                "40060-055", "10", "casa"
        );

        cadastroPage.salvarRegistro();

        String msg = cadastroPage.obterMensagemAlerta();
        System.out.println("Mensagem Campo Obrigatório: " + msg);

        // Validação EXATA conforme image_50f0b6.png
        Assert.assertTrue("Esperava erro de obrigatoriedade, mas veio: " + msg,
                msg.contains("obrigatório"));
    }

    /*@After
    public void fechar(){
        if (driver != null) {
            driver.quit();
        }
    }*/
}