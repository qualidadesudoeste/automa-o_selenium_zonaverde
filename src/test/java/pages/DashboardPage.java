package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- SELETORES ---
    private final By inputDataInicioBy = By.id("WFRInput1136638");
    private final By inputDataFimBy = By.id("WFRInput1136639");
    private final By btnFiltrarBy = By.xpath("//button[contains(., 'Filtrar')]");

    // ESTRATÉGIA DE ÍNDICE: Pega a segunda ocorrência da classe em toda a página
    // Colocamos o XPath entre parênteses para o índice [2] ser aplicado ao conjunto total
    private final By valKwhBy = By.xpath("(//span[contains(@class, 'item-value') and contains(@class, 'text-blue')])[2]");

    private final By valUsuariosBy = By.xpath("//div[contains(., 'Usuários')]//span[contains(@class, 'value')]");

    private final By mapaBy = By.id("map");
    private final By pinsMapaBy = By.cssSelector(".leaflet-marker-icon");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void filtrarDashboard(String dataInicio, String dataFim) {
        localizarFocoNoElemento(inputDataInicioBy, "Campo Data Início");
        preencherComJS(driver.findElement(inputDataInicioBy), dataInicio);

        localizarFocoNoElemento(inputDataFimBy, "Campo Data Fim");
        preencherComJS(driver.findElement(inputDataFimBy), dataFim);

        clicarGarantido(driver.findElement(btnFiltrarBy));

        // Pausa obrigatória para o Maker processar o recarregamento dos cards
        try { Thread.sleep(7000); } catch (InterruptedException e) {}
    }

    public String obterTotalUsuarios() {
        localizarFocoNoElemento(valUsuariosBy, "Indicador de Usuários");
        return driver.findElement(valUsuariosBy).getText().trim();
    }

    public String obterKwhConsumidos() {
        // A 'Boneca Russa' vai varrer os iframes até encontrar onde está o SEGUNDO span azul
        localizarFocoNoElemento(valKwhBy, "Indicador de kWh (2º azul)");

        WebElement element = driver.findElement(valKwhBy);

        // Espera o valor carregar (não ser vazio)
        wait.until(d -> !element.getText().trim().isEmpty());

        String valor = element.getText().trim();
        System.out.println("LOG: Valor de kWh (2º elemento) capturado: [" + valor + "]");
        return valor;
    }

    public boolean validarMapaComEstacoes() {
        try {
            localizarFocoNoElemento(mapaBy, "Mapa");

            // Força um resize para o Leaflet renderizar pins que podem estar ocultos
            ((JavascriptExecutor) driver).executeScript("window.dispatchEvent(new Event('resize'));");

            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(pinsMapaBy));
            return !driver.findElements(pinsMapaBy).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // --- MÉTODOS DE REFORÇO (BONECA RUSSA) ---

    private void preencherComJS(WebElement element, String texto) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = '';", element);
        js.executeScript("arguments[0].value = arguments[1];", element, texto);
        js.executeScript("arguments[0].dispatchEvent(new Event('change'));", element);
    }

    private void clicarGarantido(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        } catch (Exception e) {
            element.click();
        }
    }

    private void localizarFocoNoElemento(By locator, String nome) {
        long endTime = System.currentTimeMillis() + 25000;
        while (System.currentTimeMillis() < endTime) {
            driver.switchTo().defaultContent();
            if (buscaRecursiva(0, locator)) return;

            try {
                wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainsystem")));
                if (buscaRecursiva(0, locator)) return;
            } catch (Exception e) {}

            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        throw new RuntimeException("TIMEOUT: Dashboard - Elemento '" + nome + "' não encontrado.");
    }

    private boolean buscaRecursiva(int nivel, By locator) {
        // Verifica se o elemento existe no frame atual
        if (!driver.findElements(locator).isEmpty()) return true;

        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for (int i = 0; i < iframes.size(); i++) {
            try {
                driver.switchTo().frame(i);
                if (buscaRecursiva(nivel + 1, locator)) return true;
                driver.switchTo().parentFrame();
            } catch (Exception e) {
                try { driver.switchTo().parentFrame(); } catch (Exception ex) {}
            }
        }
        return false;
    }
}