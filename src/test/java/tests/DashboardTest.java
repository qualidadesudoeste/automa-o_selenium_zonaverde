package tests;

import org.junit.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.DashboardPage;
import pages.LoginPage;
import java.time.Duration;

public class DashboardTest {
    private WebDriver driver;
    private DashboardPage dashboardPage;

    @Before
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        driver.get("https://app.makernocode.dev/open.do?sys=OFK");

        // Login inicial para cair no Dashboard
        new LoginPage(driver).realizarLogin("qualidade", "1");

        // Pausa necessária para o carregamento inicial dos componentes assíncronos
        try { Thread.sleep(5000); } catch (InterruptedException e) {}

        dashboardPage = new DashboardPage(driver);
    }

    @Test
    public void testeIndicadoresFixos() {
        // Então os indicadores de Usuários e Estações devem ser exibidos corretamente
        String usuarios = dashboardPage.obterTotalUsuarios();
        Assert.assertNotNull("Indicador de usuários não encontrado", usuarios);
        Assert.assertFalse("Indicador de usuários está vazio", usuarios.isEmpty());
        System.out.println("LOG: Usuários detectados: " + usuarios);
    }

    @Test
    public void testeMapaEstacoes() {
        // Então o mapa deve exibir todas as estações com ícone e legenda
        boolean temEstacoesNoMapa = dashboardPage.validarMapaComEstacoes();
        Assert.assertTrue("Mapa não carregou os pins das estações", temEstacoesNoMapa);
    }

    @Test
    public void testeFiltroComDados() {
        // Quando aplicar o filtro de um período válido
        dashboardPage.filtrarDashboard("01/01/2026 00:00:00", "31/01/2026 23:59:59");
        String kwh = dashboardPage.obterKwhConsumidos();
        // Então os valores devem refletir os dados do período
        boolean temConsumo = !kwh.equals("0") && !kwh.contains("0,00") && !kwh.contains("0.00");
        Assert.assertTrue("FALHA: O valor de kWh deveria ser diferente de 0. Obtido: " + kwh, temConsumo);
    }

    @Test
    public void testeFiltroSemConsumo() {
        // Data futura para garantir que venha zerado
        dashboardPage.filtrarDashboard("01/01/2099 00:00:00", "01/01/2099 23:59:59");
        String kwh = dashboardPage.obterKwhConsumidos();

        // Lógica: Se não houver dados, o conteúdo deve ser "0"
        boolean estaZerado = kwh.equals("0") || kwh.contains("0,00") || kwh.contains("0.00");

        Assert.assertTrue("FALHA: O valor de kWh deveria ser 0. Obtido: " + kwh, estaZerado);
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}