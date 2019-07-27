package view;

import java.io.IOException;

import util.ModoException;

public class Compilador {

	/**
     * @param args the command line arguments
	 * @throws ModoException 
     */
    public static void main(String[] args) throws IOException, ModoException {
    	
      AnalisadorLexico al = new AnalisadorLexico();
      al.executar("teste.txt");
    	
    }
	
}
