/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.util.ArrayList;
import util.Classe;
import util.ErroSintatico;
import util.Token;

/**
 *
 * @author User-PC
 */
public class AnalisadorSintatico {

    private ArrayList<Token> listaDeTokens;
    private ArrayList<String> tipo = new ArrayList();
    private ArrayList<ErroSintatico> erros;
    private Token token;
   

    public AnalisadorSintatico(ArrayList<Token> listaDeTokens) {
        this.listaDeTokens = listaDeTokens;
        this.listaDeTokens.add(listaDeTokens.size(), new Token("$", Classe.FINALIZADOR, 0)); //add o '$' no final da lista
        this.setup();
        erros = new ArrayList();
    }

    public void setup() {
        //Tipos de variaveis
        tipo.add("inteiro");
        tipo.add("real");
        tipo.add("vazio");
        tipo.add("boleano");
        tipo.add("texto");

        //Identificadores
    }

    //fiz esse metodo sÃ³ pra testar se tem algo na lista mesmo.
    private void estaPresenteNaListaDeTokens(String s) {

        for (Token t : this.listaDeTokens) {
            if (t.getValor().equals(s)) {
                System.out.println("Sim");
            }
        }

        System.out.println("NÃ£o");
    }
    
    
    private void novoErro(int linha, String erro) {
    	this.erros.add(new ErroSintatico(linha, erro));
    }
    
    private void recuperacaoDeErro() {
    	
    	int linha = this.token.getLinha();
    	linha++;
    	
    	while(!token.getValor().equals("$") && token.getLinha() != linha) {
            this.token = proximo_token();
    	}
    	
    }

    public void executar() {
        //estaPresenteNaListaDeTokens(":");
        this.token = proximo_token();

        programa();

        if (this.token.getValor().equals("$")) {
            System.out.println("SUCESSO: cdigo encerrado com o caractere finalizador $");
        } else {
            System.out.println("ERRO: codigo encerrado sem atingir o $");
        }

    }

    public Token proximo_token() {
        Token t = listaDeTokens.remove(0);
        System.out.println("Token em analise: " + t.getValor());
        return t;
    }

    private void programa() {

        if (this.token.getValor().equals("programa")) {

            this.token = proximo_token();

            if (this.token.getValor().equals("{")) {

                this.token = proximo_token();
                blocoConstantes();
                escopoPrograma();

                if (this.token.getValor().equals("}")) {
                    this.token = proximo_token();
                    return;
                    
                } else {
                    
                	System.out.println("ERRO: esta faltando o simbolo }");
                	novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo }" );
                	this.recuperacaoDeErro();
                  
                }

            } else {
            	
                System.out.println("ERRO: esta faltando o simbolo {");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo {" );
            	this.recuperacaoDeErro();
            }

        } else {
        	
        	novoErro(this.token.getLinha(),"ERRO: esta faltando a palavra 'programa'" );
        	this.recuperacaoDeErro();
            System.out.println("ERRO: esta faltando a palavra 'programa'");
        }

    }
    
    private void escopoPrograma() {
        if (this.token.pertenceAoPrimeiroDe("metodo")) {
            metodo();//token = "metodo"
            escopoPrograma();
            
        } else {
        	return;
        }
    }

    private void blocoConstantes() {

        if (this.token.getValor().equals("constantes")) {
            this.token = proximo_token();

            if (this.token.getValor().equals("{")) {

                this.token = proximo_token();
                estruturaConstantes();

                if (this.token.getValor().equals("}")) {
                    this.token = proximo_token();
                    
                } else {
                    System.out.println("ERRO: esta faltando o simbolo }");
                    novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo }" );
                	this.recuperacaoDeErro();
                }

            } else {
                System.out.println("ERRO: esta faltando o simbolo {");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo {" );
            	this.recuperacaoDeErro();
            }

        } else {
        	return;
        }

    }


    private void estruturaConstantes() {
    	
        if (this.tipo.contains(this.token.getValor())) { //se token == tipo
            this.token = proximo_token();
            constantes();
            
            if (this.token.getValor().equals(";")) {
                this.token = proximo_token();
                estruturaConstantes();
                
           } else {
                System.out.println("ERRO: esta faltando o simbolo ;");
            	novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo ;" );
            	this.recuperacaoDeErro();
            }
            
        } else {//mesmo problema se checar vazio
            return;
        }
    }

    private void constantes() {
        if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) { //token == identificador
            this.token = proximo_token();
            
            if (this.token.getValor().equals("=")) {
                this.token = proximo_token();
                constante();
                multiConst();
                
            } else {
                System.out.println("ERRO: faltou o caractere =");
                novoErro(this.token.getLinha(),"ERRO: faltou o caractere =" );
            	this.recuperacaoDeErro();
            }
        } else {
            System.out.println("ERRO: declaracao de constante sem identificador");
            novoErro(this.token.getLinha(),"ERRO: declaracao de constante sem identificador" );
        	this.recuperacaoDeErro();
        }
    }

    private void constante() {
    	
        if (this.token.getClasse().equals(Classe.IDENTIFICADOR) | this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                | this.token.getClasse().equals(Classe.NUMERO)) {
            this.token = proximo_token();
        } else {
            System.out.println("ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador");
            novoErro(this.token.getLinha(),"ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador" );
        	this.recuperacaoDeErro();
        }
    }

    private void multiConst() {
        if (this.token.pertenceAoPrimeiroDe("multiplasConstantes")) {
            multiplasConstantes();
            
        }else {
            return;
        }
    }

    private void multiplasConstantes() {
        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            constantes();
            
        } else {
            System.out.println("ERRO: faltou virugula na declaracao de multiplas constantes");
            novoErro(this.token.getLinha(),"ERRO: faltou virugula na declaracao de multiplas constantes");
        	this.recuperacaoDeErro();
        }
    }

    private void leia() {
        if (this.token.getValor().equals("leia")) {
            this.token = proximo_token();

            if (this.token.getValor().equals("(")) {
                this.token = proximo_token();
                conteudoLeia();

                if (this.token.getValor().equals(")")) {
                    this.token = proximo_token();

                    if (this.token.getValor().equals(";")) {
                        this.token = proximo_token();

                    } else {
                        System.out.println("ERRO: faltou ; no fim do comando leia");
                        novoErro(this.token.getLinha(),"ERRO: faltou ; no fim do comando leia");
                    	this.recuperacaoDeErro();
                    }
                } else {
                    System.out.println("ERRO: faltou faltou o simbolo)");
                    novoErro(this.token.getLinha(),"ERRO: faltou faltou o simbolo)");
                	this.recuperacaoDeErro();
                }
            } else {
                System.out.println("ERRO: faltou o simbolo (");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo (");
            	this.recuperacaoDeErro();
            }
        } else {
            System.out.println("ERRO: faltou a palavra 'leia'");
            novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'leia'");
        	this.recuperacaoDeErro();

        }
    }

    private void conteudoLeia() {

        if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();
            vetor();
            lermais();
            
        } else {
        	System.out.println("ERRO: faltou paramentros no comando leia");
            novoErro(this.token.getLinha(),"ERRO: faltou paramentros no comando leia");
        	this.recuperacaoDeErro();
        }

    }

    private void lermais() {
    	
        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            conteudoLeia();
        } 
    }

    private void opIndice() {
        if (this.token.getClasse().equals(Classe.OPERADOR_ARITMETICO.getClasse())) {
            this.token = proximo_token();
            opI2();
            opIndice();
        }

    }

    private void opI2() {
        if (this.token.getClasse().equals(Classe.NUMERO)) {
            this.token = proximo_token();

        } else if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();

        } else {
        	System.out.println("ERRO: falta um numero ou um identificador");
            novoErro(this.token.getLinha(),"ERRO: falta um numero ou um identificador");
        	this.recuperacaoDeErro();
        }
    }

    private void vetor() {
        if (this.token.getValor().equals("[")) {
            this.token = proximo_token();
            opI2();
            opIndice();

            if (this.token.getValor().equals("]")) {
                this.token = proximo_token();
                matriz();
            } else {
            	System.out.println("ERRO: esta faltando ]");
                novoErro(this.token.getLinha(),"ERRO: esta faltando ]");
            	this.recuperacaoDeErro();

            }
        }
    }

    private void matriz() {
        if (this.token.getValor().equals("[")) {
            this.token = proximo_token();
            opI2();
            opIndice();

            if (this.token.getValor().equals("]")) {
                this.token = proximo_token();

            } else {
            	System.out.println("ERRO: esta faltando ]");
                novoErro(this.token.getLinha(),"ERRO: esta faltando ]");
            	this.recuperacaoDeErro();

            }
        }
    }

    private void metodo() {

        if (this.token.getValor().equals("metodo")) {
            this.token = proximo_token();

            if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
                this.token = proximo_token();

                if (this.token.getValor().equals("(")) {
                    this.token = proximo_token();
                    listaParametros();
                    
                    if (this.token.getValor().equals(")")) {
                    	this.token = proximo_token();
                    	
                        if (this.token.getValor().equals(":")) {
                            this.token = proximo_token();
                            
                            if (this.tipo.contains(this.token.getValor())) {
                                this.token = proximo_token();

                                if (this.token.getValor().equals("{")) {
                                    this.token = proximo_token();
                                    declaracaoVariaveis();
                                    escopoMetodo();

                                    if (this.token.getValor().equals("}")) {
                                        this.token = proximo_token();

                                    } else {
                                        System.out.println("ERRO: faltou o }");
                                        novoErro(this.token.getLinha(),"ERRO: faltou o }");
                                    	this.recuperacaoDeErro();

                                    }

                                } else {
                                    System.out.println("ERRO: faltou o {");
                                    novoErro(this.token.getLinha(),"ERRO: faltou o {");
                                	this.recuperacaoDeErro();

                                }

                            } else {
                                System.out.println("ERRO:faltou o tipo do retorno");
                                novoErro(this.token.getLinha(),"ERRO:faltou o tipo do retorno");
                            	this.recuperacaoDeErro();

                            }

                        } else {
                            System.out.println("ERRO: faltou o :");
                            novoErro(this.token.getLinha(),"ERRO: faltou o :");
                        	this.recuperacaoDeErro();


                        }
                    } else {
                        System.out.println("ERRO: faltou o )");
                        novoErro(this.token.getLinha(),"ERRO: faltou o )");
                    	this.recuperacaoDeErro();


                    }
                } else {
                    System.out.println("ERRO: faltou o (");
                    novoErro(this.token.getLinha(),"ERRO: faltou o (");
                	this.recuperacaoDeErro();


                }
            } else {
                System.out.println("ERRO:falta identificacao do metodo");
                novoErro(this.token.getLinha(),"ERRO:falta identificacao do metodo");
            	this.recuperacaoDeErro();


            }
        } else {
            System.out.println("ERRO:falta a palavra 'metodo'");
            novoErro(this.token.getLinha(),"ERRO:falta a palavra 'metodo'");
        	this.recuperacaoDeErro();


        }
    }

    private void listaParametros() {
        if (this.tipo.contains(this.token.getValor())) {
            this.token = proximo_token();

            if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
                this.token = proximo_token();
                maisParametros();

            } else {
            	System.out.println("ERRO:falta um identificador");
                novoErro(this.token.getLinha(),"ERRO:falta um identificador");
            	this.recuperacaoDeErro();
            }

        }
    }

    private void maisParametros() {

        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            listaParametros();
        }

    }

    private void escopoMetodo() {
        if(this.token.pertenceAoPrimeiroDe("comandos")){
            comandos();
            escopoMetodo();
        }
    }

    private void chamadaDeMetodo() {
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            
            if(this.token.getValor().equals("(")){
                this.token = proximo_token();
                var();
                
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                    
                }else{
                    System.out.println("ERRO: faltou )");
                    novoErro(this.token.getLinha(),"ERRO: faltou )");
                	this.recuperacaoDeErro();
                }
            }else{
                System.out.println("ERRO: faltou (");
                novoErro(this.token.getLinha(),"ERRO: faltou (");
            	this.recuperacaoDeErro();
            }
        }else{
            System.out.println("ERRO: falta identificador");
            novoErro(this.token.getLinha(),"ERRO: falta identificador");
        	this.recuperacaoDeErro();
        }
    }

    private void var() {
    	
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            maisVariavel();
            
        }else if(this.token.pertenceAoPrimeiroDe("metodoParametro")){
        	metodoParametro();
        	
        }
    }
    
    private void maisVariavel() {
        if(this.token.getValor().equals(",")){
            this.token = proximo_token();
            var();
        }
    }

   
    private void metodoParametro() {
    	
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            
            if(this.token.getValor().equals("(")){
                this.token = proximo_token();
                var();
                
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                    maisVariavel();
                    
                }else{
                	System.out.println("ERRO: esta faltando o simbolo ) ");
                    novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo ) ");
                	this.recuperacaoDeErro();
                }
            }else{
            	System.out.println("ERRO: esta faltando o simbolo ( ");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo ( ");
            	this.recuperacaoDeErro();
            }
        }else{
        	System.out.println("ERRO: esta faltando o identificador ");
            novoErro(this.token.getLinha(),"ERRO: esta faltando o identificador ) ");
        	this.recuperacaoDeErro();
        }
    }

    private void declaracaoVariaveis() {
    	
        if(this.token.getValor().equals("variaveis")){
            this.token = proximo_token();
            
            if(this.token.getValor().equals("{")){
                this.token = proximo_token();
                varV();
                
                if(this.token.getValor().equals("}")){
                    this.token = proximo_token();
                    
                }else{
                	System.out.println("ERRO: esta faltando o simbolo } ");
                    novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo } ");
                	this.recuperacaoDeErro();
                }
                
            }else{
            	System.out.println("ERRO: esta faltando o simbolo { ");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo { ");
            	this.recuperacaoDeErro();
            }
        }
    }

    private void varV() {
    	
        if(this.tipo.contains(this.token.getValor())){
            this.token = proximo_token();
            complementoV();
            maisVariaveis();
            
        }else{
        	System.out.println("ERRO: aguarda-se um tipo de variavel boleano/inteiro/real/texto");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um tipo de variável boleano/inteiro/real/texto");
        	this.recuperacaoDeErro();
        }
    }
    
	private void complementoV() {
		
       if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
           this.token = proximo_token();
           vetor();
           variavelMesmoTipo();
           
       }else {
    	   
    	   System.out.println("ERRO: faltou um identificador");
           novoErro(this.token.getLinha(),"ERRO: faltou um identificador");
       	   this.recuperacaoDeErro();
    	   
       }
       
    }

    private void variavelMesmoTipo() {
        if(this.token.getValor().equals(",")) {
            this.token = proximo_token();
            complementoV();
            
         } else if(this.token.getValor().equals(";")) {
             this.token = proximo_token();

         } else {
        	 
        	 System.out.println("ERRO: faltou , ou ;");
             novoErro(this.token.getLinha(),"ERRO: faltou , ou ;");
         	 this.recuperacaoDeErro();
         }
    }
    
    private void maisVariaveis() {
    	
        if(this.tipo.contains(this.token.getValor())){ //Primeiro("VarV") == Tipo
            varV();
        }
    }


    private void comandos() {
        if(this.token.pertenceAoPrimeiroDe("leia")){
            leia();
            
        }else if(this.token.pertenceAoPrimeiroDe("escreva")){
            escreva();
            
        }else if(this.token.pertenceAoPrimeiroDe("se")){
            se();
            
        }else if(this.token.pertenceAoPrimeiroDe("enquanto")){
            enquanto();
            
        }else if(this.token.pertenceAoPrimeiroDe("atribuicaoVariavel")){
            atribuicaoVariavel();
            
        }else if(this.token.pertenceAoPrimeiroDe("chamadaDeMetodo")){
            chamadaDeMetodo();
            
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
                
            }else{
            	 System.out.println("ERRO: faltou , ou ;");
                 novoErro(this.token.getLinha(),"ERRO: faltou , ou ;");
             	 this.recuperacaoDeErro();
            }
            
        }else if(this.token.pertenceAoPrimeiroDe("incrementador")){
            incrementador();
            
        }else if(this.token.getValor().equals("resultado")){
            this.token = proximo_token();
            retorno();
            
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
            }else{
            	 System.out.println("ERRO: faltou , ou ;");
                 novoErro(this.token.getLinha(),"ERRO: faltou , ou ;");
             	 this.recuperacaoDeErro();
            }
        }
            
    }

    private void escreva() {
    	
    	if(this.token.getValor().equals("escreva")) {
            this.token = proximo_token();
            
            if(this.token.getValor().equals("(")) {
                this.token = proximo_token();
                paramEscrita();
                
                if(this.token.getValor().equals(")")) {
                    this.token = proximo_token();
                    
                    if(this.token.getValor().equals(";")) {
                        this.token = proximo_token();

                    } else {
                    	 System.out.println("ERRO: faltou ;");
                         novoErro(this.token.getLinha(),"ERRO: faltou ;");
                     	 this.recuperacaoDeErro();
                    }
                    
                } else {
                	
                	 System.out.println("ERRO: faltou )");
                     novoErro(this.token.getLinha(),"ERRO: faltou )");
                 	 this.recuperacaoDeErro();
                }
                
            } else {
            	 System.out.println("ERRO: faltou (");
                 novoErro(this.token.getLinha(),"ERRO: faltou (");
             	 this.recuperacaoDeErro();
            }
            
    	} else {
    		 System.out.println("ERRO: faltou a palavra 'escreva'");
             novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'escreva'");
         	 this.recuperacaoDeErro();
    	}
        
    }

    private void paramEscrita() {
		if(this.token.pertenceAoPrimeiroDe("verificaCaso")) {
			verificaCaso();
			maisParametrosE();
			
		} else {
			System.out.println("ERRO: sintaxe de parametro incorreta no comando escrita");
            novoErro(this.token.getLinha(),"ERRO: sintaxe de parametro incorreta no comando escrita");
        	this.recuperacaoDeErro();
		}
    	
		
	}

	private void maisParametrosE() {
		
		if(this.token.getValor().equals(",")) {
            this.token = proximo_token();
            paramEscrita();

		}
		
	}

	private void se() {
        
		if(this.token.getValor().equals("se")) {
            this.token = proximo_token();
            condSe();
            
            if(this.token.getValor().equals("entao")) {
                this.token = proximo_token();
                
                if(this.token.getValor().equals("{")) {
                    this.token = proximo_token();
                    blocoSe();
                    
                    if(this.token.getValor().equals("}")) {
                        this.token = proximo_token();
                        senao();
                        
                    } else {
                    	System.out.println("ERRO: faltou o simbolo }");
                        novoErro(this.token.getLinha(),"ERRO: faltou o simbolo }");
                    	this.recuperacaoDeErro();
                    }
               } else {
            	   System.out.println("ERRO: faltou o simbolo {");
                   novoErro(this.token.getLinha(),"ERRO: faltou o simbolo {");
               		this.recuperacaoDeErro();
               }
                
	         } else {
	        	 System.out.println("ERRO: faltou a palavra 'entao'");
	             novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'entao'");
	         	 this.recuperacaoDeErro();
	         }

		}else {
			System.out.println("ERRO: faltou a palavra 'se'");
            novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'se'");
            this.recuperacaoDeErro();
		}
    }
	
	private void negar() {
		 
		if(this.token.getValor().equals("!")) {
            this.token = proximo_token();
		}
	}
	
	private void maisCond() {
		
		if(this.token.getClasse().equals(Classe.OPERADOR_LOGICO)){
            this.token = proximo_token();
            cond();
            maisCond();
		}
		
	}

	private void cond() {
		
		if(token.pertenceAoPrimeiroDe("termo")) {
			termo();
			
			if(token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
	            this.token = proximo_token();
				termo();
				
			} else {
				System.out.println("ERRO: faltou operador relacional");
	            novoErro(this.token.getLinha(),"ERRO: faltou operador relacional");
	            this.recuperacaoDeErro();
			}
			
		} else if(token.pertenceAoPrimeiroDe("negar")) {
			negar();
			
			if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
	            this.token = proximo_token();
	            vetor();
	            
			} else {
				System.out.println("ERRO: faltou identificador");
	            novoErro(this.token.getLinha(),"ERRO: faltou identificador");
	            this.recuperacaoDeErro();

			}
			
		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            this.recuperacaoDeErro();
		}
		
	}

	private void termo() {
		
		if(token.pertenceAoPrimeiroDe("tipoTermo")) {
			tipoTermo();
			op();
			
		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            this.recuperacaoDeErro();
		}
		
	}

	private void op() {
			
		if(this.token.getClasse().equals(Classe.OPERADOR_ARITMETICO)) {
            this.token = proximo_token();
            tipoTermo();
            op();
		}
		
	}

	private void tipoTermo() {
		
		if(token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            
		} else if(token.getClasse().equals(Classe.NUMERO)) {
            this.token = proximo_token();
            
		}else if(token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
            this.token = proximo_token();
			
		}else if(token.getValor().equals("verdadeiro")) {
            this.token = proximo_token();
			
		}else if(token.getValor().equals("false")) {
            this.token = proximo_token();
            
		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            this.recuperacaoDeErro();
		}
	}

	private void condSe() {
		
		if(token.getValor().equals("(")) {
            this.token = proximo_token();
            cond();
            maisCond();
            
    		if(token.getValor().equals(")")) {
                this.token = proximo_token();

    		} else {
    			System.out.println("ERRO: faltou o simbolo )");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo )");
                this.recuperacaoDeErro();
    		}


		} else {
			System.out.println("ERRO: faltou o simbolo (");
            novoErro(this.token.getLinha(),"ERRO: faltou o simbolo (");
            this.recuperacaoDeErro();
		} 
		
		
	}
	
    
	private void blocoSe() {
		
		if(token.pertenceAoPrimeiroDe("comandos")) {
			comandos();
			blocoSe();
		}
		
	}

	private void senao() {
		
		if(token.getValor().equals("senao")) {
            this.token = proximo_token();
            condSenao();
            
            if(token.getValor().equals("{")) {
                this.token = proximo_token();
                blocoSe();
                
                if(token.getValor().equals("}")) {
                    this.token = proximo_token();
                    
                } else {
                	System.out.println("ERRO: faltou o simbolo }");
                    novoErro(this.token.getLinha(),"ERRO: faltou o simbolo }");
                    this.recuperacaoDeErro();
                }
                
            } else {
            	System.out.println("ERRO: faltou o simbolo {");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo {");
                this.recuperacaoDeErro();
            }

            
		} 
		
	}

	
	private void condSenao() {
		
		if(token.pertenceAoPrimeiroDe("conseSe")) {
			condSe();
			
			if(token.getValor().equals("entao")) {
	            this.token = proximo_token();

			} else {
				System.out.println("ERRO: faltou a palavra 'entao");
                novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'entao");
                this.recuperacaoDeErro();
			}
			
		}
		
	}

	private void enquanto() {
		if(token.getValor().equals("enquanto")) {
            this.token = proximo_token();

    		if(token.getValor().equals("(")) {
                this.token = proximo_token();
                operacaoRelacional();
                
        		if(token.getValor().equals(")")) {
                    this.token = proximo_token();
                    
            		if(token.getValor().equals("{")) {
                        this.token = proximo_token();
                        conteudoLaco();
                        
                        if(token.getValor().equals("}")) {
                            this.token = proximo_token();
                            conteudoLeia();
                            
                		} else {
                			System.out.println("ERRO: faltou o simbolo }");
                            novoErro(this.token.getLinha(),"ERRO: faltou o simbolo }");
                            this.recuperacaoDeErro();
                		}
                        
            		} else {
            			System.out.println("ERRO: faltou o simbolo {");
                        novoErro(this.token.getLinha(),"ERRO: faltou o simbolo {");
                        this.recuperacaoDeErro();
            		}

        		}  else {
        			System.out.println("ERRO: faltou o simbolo )");
                    novoErro(this.token.getLinha(),"ERRO: faltou o simbolo )");
                    this.recuperacaoDeErro();
        		}
                   
    		}  else {
    			System.out.println("ERRO: faltou o simbolo (");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo (");
                this.recuperacaoDeErro();
    		}

		}  else {
			System.out.println("ERRO: faltou a palavra 'enquanto'");
            novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'enquanto'");
            this.recuperacaoDeErro();
		}
    }
	
	

    private void conteudoLaco() {
    	
		if(token.pertenceAoPrimeiroDe("comandos")) {
			comandos();
			conteudoLaco();
		}
		
	}

	private void operacaoRelacional() {
		
		if(token.pertenceAoPrimeiroDe("complementoOperador")) {
			complementoOperador();
			
			if(token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
	            this.token = proximo_token();
				complementoOperador();

			}
		} else if(token.pertenceAoPrimeiroDe("negar")) {
			negar();
			
			if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
	            this.token = proximo_token();
	            vetor();
	            
			} else {
				System.out.println("ERRO: faltou identificador");
	            novoErro(this.token.getLinha(),"ERRO: faltou identificador");
	            this.recuperacaoDeErro();
			}
			
		} else {
			
			System.out.println("ERRO: erro de sintaxe na condicao do enquanto");
            novoErro(this.token.getLinha(),"ERRO: erro de sintaxe na condicao do enquanto");
            this.recuperacaoDeErro();
		}
	}
		


	private void complementoOperador() {
		
		if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();
            vetor();
            
		} else if(token.getClasse().equals(Classe.NUMERO)) {
            this.token = proximo_token();

		} else if(token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
            this.token = proximo_token();

		} else if(token.getValor().equals("verdadeiro")) {
            this.token = proximo_token();

		} else if(token.getValor().equals("falso")) {
            this.token = proximo_token();

		} else {
			System.out.println("ERRO: erro de sintaxe na condicao do enquanto");
            novoErro(this.token.getLinha(),"ERRO: erro de sintaxe na condicao do enquanto");
            this.recuperacaoDeErro();
		}

		
		
	}

	private void atribuicaoVariavel() {
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            
            if(this.token.getValor().equals("=")){
                this.token = proximo_token();
                verificaCaso();
                
                if(this.token.getValor().equals(";")){
                    this.token = proximo_token();
                    
                }else{
                	System.out.println("ERRO: faltou o simbolo ;");
                    novoErro(this.token.getLinha(),"ERRO: faltou o simbolo ;");
                    this.recuperacaoDeErro();
                }
            }else{
            	System.out.println("ERRO: faltou o simbolo =");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo =");
                this.recuperacaoDeErro();
            }
            
        }else{
        	System.out.println("ERRO: faltou identificador");
            novoErro(this.token.getLinha(),"ERRO: faltou identificador");
            this.recuperacaoDeErro();
        }
    }

    private void incrementador() {
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            
            if(isIncrementador(this.token.getValor())){ //pra checar se Ã© ++ ou --
                this.token = proximo_token();
                
                if(this.token.getValor().equals(";")){
                    this.token = proximo_token();
                    
                }else{
                    String s = "Erro de Sintaxe: falta o ;";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
                }
            }else{
                String s = "Erro de Sintaxe: deveria ser ++ ou -- ;";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }
    }

    private void retorno() {
    	
        if(this.token.pertenceAoPrimeiroDe("verificaCaso")){
            verificaCaso();
        }
    }

    private void verificaCaso() {
    	
        if(this.token.pertenceAoPrimeiroDe("incremento")){
            incremento();
            
        }else if(this.token.pertenceAoPrimeiroDe("expressao")){
            expressao();
            
        }else if(this.token.pertenceAoPrimeiroDe("booleano")){
            booleano();
            
        }else{
        	System.out.println("ERRO: erro de sintaxe");
            novoErro(this.token.getLinha(),"ERRO: erro de sintaxe");
            this.recuperacaoDeErro();
        }
        
    }

    private boolean isIncrementador(String s){
        String[] partes = s.split("");
        if(partes.length == 2){
            if( (partes[0].equals("+") && partes[1].equals("+")) | ((partes[0].equals("-") && partes[1].equals("-"))) ){
                return true;
            }
        }
        
        return false;
    }
     
    private boolean isOperadorAritmetico(String s){
                return s.equals("+") | s.equals("-") | s.equals("*") | s.equals("/");
    }

   

    private void incremento(){
        if(this.token.getValor().equals("(")){
            this.token = proximo_token();
            auxiliarA();                
        
        }else if(isIncrementador(this.token.getValor())){
            this.token = proximo_token();
            if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
                this.token = proximo_token();
                vetor();
            }else{
                String s = "Erro de Sintaxe: falta o identificador";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }else if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            if(isIncrementador(this.token.getValor())){
                this.token = proximo_token();
            }else{
                String s = "Erro de Sintaxe: falta ++ ou --";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }else{
            String s = "Erro de Sintaxe";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
        }
    }
    
    
    private void auxiliarA() {
        if(isIncrementador(this.token.getValor())){
            this.token = proximo_token();
            if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
                this.token = proximo_token();
                vetor();
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                }else{
                    String s = "Erro Sintático: falta o )";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
                }
            }else{
                String s = "Erro Sintático: falta o identificador";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }else if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            if(isIncrementador(this.token.getValor())){
                this.token = proximo_token();
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                }else{
                    String s = "Erro Sintático: falta o )";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
                }
            }else{
                String s = "Erro Sintático: deveria ser ++ ou --";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }else{
            String s = "Erro de Sintaxe";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
        }
    }
    
    private void expressao() {
        if(this.token.getValor().equals("(")){
            this.token = proximo_token();
            expressao();
            if(this.token.getValor().equals(")")){
                this.token = proximo_token();
                auxiliarD();
            }else{
                String s = "Erro Sintático: falta o )";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }else if(this.token.pertenceAoPrimeiroDe("operador")){
            operador();
            maisOperacoes();
        }else{
            String s = "Erro Sintático";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
        }
    }

    private void auxiliarD() {
        if(this.token.getClasse().equals(Classe.OPERADOR_ARITMETICO)){
            this.token = proximo_token();
            expressao();
        }else{
            return;
        }
    }
    
    private void booleano() {
        if(this.token.getValor().equals("verdadeiro") | this.token.getValor().equals("falso")){
            this.token = proximo_token();
        }else if(this.token.getValor().equals("(")){
            this.token = proximo_token();
            auxiliarB();
        }else if(this.token.getValor().equals("!")){
            this.token = proximo_token();
            auxiliarC();
        }else{
            String s = "Erro de Sintaxe";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
        }
    }

    private void auxiliarB() {
        if(this.token.getValor().equals("verdadeiro") | this.token.getValor().equals("falso")){
            this.token = proximo_token();
            if(this.token.getValor().equals(")")){
                this.token = proximo_token();
            }else{
                    String s = "Erro Sintático: falta o )";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }else if(this.token.getValor().equals("!")){
            this.token=proximo_token();
            auxiliarC();
            if(this.token.getValor().equals(")")){
                this.token = proximo_token();
            }else{
                String s = "Erro Sintático: falta o )";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }
    }
        
    private void auxiliarC() {
        if(this.token.getValor().equals("verdadeiro") | this.token.getValor().equals("falso")){
            this.token = proximo_token();
        }else if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
        }else{
            String s = "Erro de Sintaxe";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
        }
    
    }
    
    private void operador() {
    	
        if( this.token.getClasse().equals(Classe.NUMERO) | this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)){
            this.token = proximo_token();    
            
        }else if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token =proximo_token();
            vetor();
            
        }else if(this.token.pertenceAoPrimeiroDe("chamadaDeMetodo")){
            chamadaDeMetodo();
        
        } else {
        	System.out.println("ERRO: operador invalido");
            novoErro(this.token.getLinha(),"ERRO: operador invalido");
            this.recuperacaoDeErro();
        }
    
    }

    private void maisOperacoes() {
        if(this.token.getClasse().equals(Classe.OPERADOR_ARITMETICO)){
            this.token = proximo_token();
            auxiliarE();
        }else{
            return;
        }
    }

    private void auxiliarE(){
        if(this.token.pertenceAoPrimeiroDe("maisOperacoes")){
                maisOperacoes();
        }else if(this.token.pertenceAoPrimeiroDe("expressao")){
                expressao();
        }else{
            String s = "Erro de Sintaxe";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
        }
    }
    
 
}
