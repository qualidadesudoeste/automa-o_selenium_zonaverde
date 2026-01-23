package tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.LoginPage;

public class LoginTest {
    private WebDriver driver;
    private LoginPage loginPage;

    @Before
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://app.makernocode.dev/open.do?sys=OFK");
        loginPage = new LoginPage(driver);
    }

    @Test
    public void testeFazerLoginComSucesso() {
        // Agora o Java vai reconhecer esses símbolos
        loginPage.preencherUsuario("qualidade");
        loginPage.preencherSenha("1");
        loginPage.clicarEntrar();
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}