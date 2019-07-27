/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/** Automato que reconhece um número;
 *
 * @author sarah
 */
public class AutomatoNumeros extends Automato {
    
    
    /** Verifica se c é aceito pelo automato que reconhece números.
     * 
     * @param  c
     * @return 
     */
    public boolean isNumero(char c){
        
       int ascii = (int) c;
        
        switch(this.estado){
            
            case(0):{
                
                if(ascii> 47 && ascii< 58){
                    this.estado = 2;
                    return true;
                    
                }else if( c == '-' || c == ' '){
                    this.estado = 1;
                    return true;
   
                }
                
                break;
            }
            case(1):{
                
                if(c== ' '){
                    return true;
                    
                }else if(ascii> 47 && ascii< 58){
                    this.estado = 2;
                    return true;

                    
                }
                
                break;
            
            }
            
            case(2):{
                
                if( ascii> 47 && ascii< 58){
                    return true;
                    
                } else if(c == '.'){
                    this.estado = 3;
                    return true;
                
                }                
              
                break;
            }
            
            case (3):{
                
                if(ascii> 47 && ascii< 58){
                    this.estado = 4;
                    return true;
                } 
                
                break;
            }
            
            case (4):{
                
                if(ascii> 47 && ascii< 58){
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
        return this.estado == 2 || this.estado == 4;
    }
    
}
