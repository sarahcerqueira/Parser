/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/** Automato que reconhece operadores aritméticos.
 *
 * @author sarah
 */
public class AutomatoOperAritmetico extends Automato {
    
    
    /** Verifica se c é aceito pelo automato que reconhece operadores aritméticos.
     * 
     * @param  c
     * @return 
     */
    public boolean isOperAritmetico(char c){
        
        switch(this.estado){
            
            case(0):{
                
                if(c == '*' || c == '/'){
                    this.estado = 3;
                    return true;
                    
                }else if( c == '+'){
                    this.estado = 1;
                    return true;
                    
                 }else if( c == '-'){
                    this.estado = 2;
                    return true;    
                    
                }else{
                    return false;
                }
            }
            case(1):{
                
                if(c== '+'){
                    this.estado = 3;
                    return true;
                    
                }else {
                    return false;
                }
            
            }
            
            case(2):{
                
                if( c == '-'){
                    this.estado = 3;
                    return true;
                    
                } else{
                    return false;
                }
            }
            
            case (3):{
            
                return false;
            }
            
        }
        
        
        return false;
    }
    

    @Override
    public boolean isEstadoFinal() {
        return this.estado == 3 || this.estado == 1 || this.estado == 2;
    }
    
}
