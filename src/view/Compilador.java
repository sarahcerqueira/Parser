package view;

import java.io.File;
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
    	File file = new File("teste\\");
    	File[] arquivos = file.listFiles();
    	
    	int i =0;
    	
    	while (i< arquivos.length) {
    		
    		if(arquivos[i].getName().endsWith(".txt"))
    		{       			
    			//executa o analisador lexico e obtem a lista de tokens
                      System.out.println("\nAnalisando arquivo: "+arquivos[i].getName()+"\n");
                      AnalisadorLexico al = new AnalisadorLexico();
    		      listaDeTokens = al.executar(arquivos[i].getPath());
    		      
    		      if(al.getErro().isEmpty()) {
    		      
	    		      //executa o analisador sintático
	    		      AnalisadorSintatico as = new AnalisadorSintatico(listaDeTokens);
	    		      as.executar(arquivos[i].getPath()); 
    		      } else {
    		    	  System.out.println("\nErro l�xico no arquivo: " + arquivos[i].getPath() + "\n");
    		      }
    		}
    		
    		i++;
    	}

      
   }
   
	
}
