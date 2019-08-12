package view;

import java.io.IOException;
import java.util.ArrayList;

import util.ModoException;
import util.Token;

public class Compilador {

	/**
     * @param args the command line arguments
	 * @throws ModoException 
     */
    public static void main(String[] args) throws IOException, ModoException {
    	
      ArrayList<Token> listaDeTokens;
      
      //executa o analisador lexico e obtem a lista de tokens
      AnalisadorLexico al = new AnalisadorLexico();
      listaDeTokens = al.executar("teste_pedro.txt");
      
      //executa o analisador sintático
      AnalisadorSintatico as = new AnalisadorSintatico(listaDeTokens);
      as.executar();
      
    	
    }
	
}
