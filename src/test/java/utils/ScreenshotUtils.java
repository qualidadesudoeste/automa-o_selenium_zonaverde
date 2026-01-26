package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtils {

    public static void tirarPrint(WebDriver driver, String nomeDoTeste) {
        // Pega o caminho Raiz do projeto (C:\Users\sudoe\IdeaProjects\selenium-testes)
        String caminhoProjeto = System.getProperty("user.dir");

        String dataHora = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        // Monta o caminho completo explicitamente
        String nomeArquivo = caminhoProjeto + File.separator + "erros" + File.separator + nomeDoTeste + "_" + dataHora + ".png";

        File destino = new File(nomeArquivo);

        try {
            // Tira a foto
            File foto = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Cria a pasta se não existir
            File pasta = destino.getParentFile();
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            // Salva
            FileHandler.copy(foto, destino);

            System.out.println("-------------------------------------------------------");
            System.out.println("FOTO DE ERRO SALVA EM: " + destino.getAbsolutePath());
            System.out.println("-------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("ERRO AO TIRAR PRINT: " + e.getMessage());
        }
    }
}