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
public class AutomatoDelimitador extends Automato {
    
    
    /** Verifica se um determinado caractere é um um delimitador.
     * 
     * @param c
     * @return 
     */
    public boolean isdelimitado(char c) {
        
        switch(this.estado){
            
            case(0):{
                
                if (c == ';' || c == ',' || c == '.' || c == '(' || c == ')'|| c == '[' || c == ']' || c == '{' || c == '}')
                    this.estado = 1;
                    return true;
            }
            case(1):{
            
                return false;
            }
        }

        return false;

    }

    
    public boolean isEstadoFinal() {
        return this.estado == 1;
    }
    

}
