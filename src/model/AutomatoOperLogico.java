/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/** Esta classe implementa um automato que reconhece um operador lógico.
 *
 * @author sarah
 */
public class AutomatoOperLogico  extends Automato {
    
    
    /** Verifica se c é um operador lógico.
     * 
     * @param  c
     */
    public boolean isOperLogico(char c){
        
        switch(this.estado){
            
            case(0):{
                
                if(c == '!'){
                    this.estado = 3;
                    return true;
                    
                }else if( c == '&'){
                    this.estado = 1;
                    return true;
                    
                } else if (c == '|'){
                    this.estado = 2;
                    return true;
                    
                }
                
                
                break;
                
                }
            
            case(1):{
                
                if(c== '&'){
                    this.estado = 3;
                    return true;
                    
                }
                
                break;
            
            }
            
            case(2):{
                
                if( c == '|'){
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
        return this.estado == 3;
    }
    
}
