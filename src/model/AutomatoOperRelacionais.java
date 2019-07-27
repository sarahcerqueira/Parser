/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/** Implementa um automato que reconhece operadores relacionais.
 *
 * @author sarah
 */
public class AutomatoOperRelacionais extends Automato {
    
    
    /** Verifica se c é aceito pelo automato que reconhece operadores relacionais.
     * 
     * @param  c
     * @return 
     */
    public boolean isOperRelacional(char c){
        
        switch(this.estado){
            
            case(0):{
                
                if(c == '!'){
                    this.estado = 2;
                    return true;
                    
                }else if( c == '<' || c == '>' | c == '='){
                    this.estado = 1;
                    return true;
                    
                }
                
                break;
            }
            case(1):{
                
                if(c== '='){
                    this.estado = 3;
                    return true;
                    
                }
                
                break;
            
            }
            
            case(2):{
                
                if( c == '='){
                    this.estado = 3;
                    return true;
                    
                } 
                
                break;
            }
            
            
        }
        
		this.estado = -1;
        return false;
    }
    

    @Override
    public boolean isEstadoFinal() {
        return this.estado == 3 || this.estado == 1;
    }
    
}
