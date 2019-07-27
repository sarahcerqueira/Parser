/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/** Reconhece uma cadeia de caractere.
 *
 * @author sarah
 */
public class AutomatoCadeiaCaractere extends Automato {

    
    /** Verifica e um determinado caractere é aceito pelo automato.
     * 
     * @param c
     * @return 
     */
    public boolean isCadeiaCaractere( char c){
        
        int ascii = (int) c;
        
        
        switch(this.estado){
        
            case(0):{
                
                if (c == '"'){
                    this.estado = 1;
                    return true;
                } else {
                    return false;
               }
            }
            
            case (1):{
                
                if (c == '"'){
                    this.estado = 3;
                    return true;
                    
                } else if( c == '\\'){
                    this.estado = 2;
                    return true;
                    
                } else if( ascii > 31 && ascii < 127 && ascii != 34){  // Simbolos ascii [32, 126]
                    return true;
                    
                } else{
                    return false;
                }           
            }
            
            case(2):{
                
                if(c == '"'){
                    this.estado = 1;
                    return true;
                    
                }else {
                    return false;
                }
                    
            }case(3):{
                return false;
            }
        }
        
        
    
    return false;
    }
  
    
   
    @Override
    public boolean isEstadoFinal(){
        
        return this.estado == 3;
    }
}
