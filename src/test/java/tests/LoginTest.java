package tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.LoginPage;

public class LoginTest {
    private WebDriver driver;
    private LoginPage loginPage;

    @Before
    public void setUp() {
        // 1. Configura as opções (Blindagem para CI/CD)
        ChromeOptions options = new ChromeOptions();

        // OBRIGATÓRIO PARA GITHUB ACTIONS (Roda sem tela)
        // Se quiser ver a tela no seu PC, comente a linha abaixo com //
        options.addArguments("--headless=new");

        // Garante resolução Full HD para não quebrar layout responsivo
        options.addArguments("--window-size=1920,1080");

        // Estabilidade para Linux
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        // 2. Passa as opções para o Driver
        driver = new ChromeDriver(options);

        // driver.manage().window().maximize(); // Não precisa se usar window-size

        driver.get("https://app.makernocode.dev/open.do?sys=OFK");

        loginPage = new LoginPage(driver);
    }

    @Test
    public void testeFazerLoginComSucesso() {
        // Agora o Java vai reconhecer esses símbolos
        loginPage.preencherUsuario("qualidade");
        loginPage.preencherSenha("1");
        //loginPage.clicarEntrar();
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}