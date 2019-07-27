/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/** Identifica se um token é um identificado.
 *
 * @author sarah
 */
public class AutomatoIdentificador extends Automato  {
    
    
    /** Verifica se um caractere é aceito pelo automato.
     * 
     * @param c Caractere a ser verificado. 
     */
    public boolean isIdentificador(char c){
        
        int ascii = (int) c;
        
        switch(this.estado){
            case(0):{
                
                // O intervalo de [97, 122] letras minúsculas em Ascii e [65,90] letras maiúsculas.
                if( (ascii > 96 && ascii < 123) || (ascii > 64 && ascii < 91) ){
                    this.estado = 1;
                    return true;
                } 
                
                break;
            }
            
            case(1):{
                
            // Letra minuscula em ascii [97,122]
            // Letra maiuscula em ascii [65,90]
            // Números em ascii [48, 57]
            if ((ascii > 96 && ascii < 123) || (ascii > 64 && ascii < 91) ||  (ascii > 47 && ascii < 58) || ascii == 95) {
            	return true;
            }
            
            break;
            }
        }
        
		this.estado = -1;
        return false;
        
    }
    
    /** Verifica se o automato está no estado final.
     * @return 
     */
    @Override
    public boolean isEstadoFinal(){
        
        return this.estado == 1;
    }
    
}
