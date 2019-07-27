/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * Esse automato identifica delimitadores da linguagem.
 *
 * @author sara
 */
public class AutomatoDelimitador {
	
    
    /** Verifica se um determinado caractere é um um delimitador.
     * 
     * @param c
     * @return 
     */
    public boolean isdelimitado(char c) {
    	
    	if(c == ';' || c == ',' || c == '.' || c == '(' || c == ')'|| c == '[' || c == ']' || c == '{' || c == '}') { 
        return true;}
    	
    	return false;
                 

    }
    
    

    

}
