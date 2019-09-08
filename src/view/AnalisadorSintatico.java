/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import util.ChamadaMetodo;
import util.Classe;
import util.Erro;
import util.ManipuladorDeArquivo;
import util.Modo;
import util.ModoException;
import util.Token;
import util.Escopo;

/**
 *
 * @author User-PC
 */
public class AnalisadorSintatico {

    private ArrayList<Token> listaDeTokens;
    private ArrayList<String> tipo = new ArrayList<String>();
    private ArrayList<Erro> erros;
    private ArrayList<Erro> errosSemanticos;
    private Token token;
    private ArrayList<String[]> constantes;
    private ArrayList<Escopo> escopos;
    private String[] var;
    private String escopo;
	private ArrayList<ChamadaMetodo> callMedotos;

   

    public AnalisadorSintatico(ArrayList<Token> listaDeTokens) {
        this.listaDeTokens = listaDeTokens;
        this.listaDeTokens.add(listaDeTokens.size(), new Token("$", Classe.FINALIZADOR, 0)); //add o '$' no final da lista
        this.setup();
        erros = new ArrayList<Erro>();
        constantes = new ArrayList<String[]>();
        escopos = new ArrayList<Escopo>();
        errosSemanticos = new ArrayList<Erro>();
		callMedotos = new ArrayList<ChamadaMetodo>();

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

    private void novoErro(int linha, String erro) {
    	this.erros.add(new Erro(linha, erro));
    }
    
    private void recuperacaoDeErro() {
    	
    	int linha = this.token.getLinha();
    	linha++;
    	
    	while(!token.getValor().equals("$") && token.getLinha() != linha) {
            this.token = proximo_token();
    	}
    	
    }

    public void executar(String arquivo) {
        //estaPresenteNaListaDeTokens(":");
        this.token = proximo_token();

        programa();

        if (this.token.getValor().equals("$")) {
            System.out.println("SUCESSO: codigo encerrado com o caractere finalizador $");
        } else {
            System.out.println("ERRO: codigo encerrado sem atingir o $");
        }
        
        if (this.buscaEscopo("principal") == null) {
        	System.out.println("ERRO: metodo principal nï¿½o existe");
    		novoErroSemantico(-1,"ERRO: metodo principal nï¿½o existe");
        }
        
		verificarChamadasMetodos();

        escreveSaida(arquivo);

    }
    
    private void verificarChamadasMetodos() {
		ChamadaMetodo cm;
		Escopo e, eaux;
		ArrayList<String[]> parametros;
		ArrayList<String> paraChamada;

		while (!this.callMedotos.isEmpty()) {
			cm = this.callMedotos.remove(0);
			e = this.buscaEscopo(cm.getId());

			if (e == null) {
				System.out.println("ERRO: metodo " + cm.getId() + " não existe");
				novoErroSemantico(cm.getLinha(), "ERRO: metodo " + cm.getId() + " não existe");

			} else {

				parametros = e.getParametros();
				paraChamada = cm.getParametros();
				int tam, aux = 0;
				tam = parametros.size();

				if (parametros.size() > paraChamada.size()) {
					System.out.println("ERRO: falta parametros");
					novoErroSemantico(cm.getLinha(), "ERRO: falta parametros");

				} else if (parametros.size() < paraChamada.size()) {
					System.out.println("ERRO: há parametros a mais");
					novoErroSemantico(cm.getLinha(), "ERRO: há parametros a mais");
				} else {

					for (int i = 0; i < tam; i++) {
						String tipo = parametros.get(i)[0];
						String cadeia = paraChamada.get(i);
						String escopo = cm.getEscopo();

						eaux = this.buscaEscopo(escopo); // escopo de chamada

						if (this.isConstante(cadeia)) {
							if (!this.getConstante(cadeia)[3].equals(tipo)) {
								System.out.println("ERRO: tipo de paramentro errado");
								novoErroSemantico(cm.getLinha(), "ERRO: tipo de paramentro errado");
								break;
							}
						}
						if (eaux.hasParamentro(cadeia)) {
							if (!eaux.tipoParametro(cadeia).equals(tipo)) {
								System.out.println("ERRO: tipo de paramentro errado");
								novoErroSemantico(cm.getLinha(), "ERRO: tipo de paramentro errado");
								break;

							}
						}
						if (eaux.isVariavel(cadeia)) {
							if (!eaux.getTipo(cadeia).equals(tipo)) {
								System.out.println("ERRO: tipo de paramentro errado");
								novoErroSemantico(cm.getLinha(), "ERRO: tipo de paramentro errado");
								break;

							}
						}

					}

			}
			}	
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
        if (pertenceAoPrimeiroDe("metodo")) {
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
            
            String type = this.token.getValor();
            
            if(type.equals("vazio")){
                System.out.println("ERRO: o tipo vazio nao pode ser usado em declaracoes de constantes");
                novoErroSemantico(this.token.getLinha(),"ERRO: o tipo vazio nao pode ser usado em declaracoes de constantes" );	
            }
            
            this.token = proximo_token();
            constantes(type);
            
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

    private void constantes(String tipo) {
        if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) { //token == identificador
        	
        	//Erro Semantico
        	if(this.isConstante(token.getValor())) {
        		System.out.println("ERRO: identificador duplicado");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: identificador duplicado" );
        		
        	}else {
                    this.addConstantes(token.getValor(), token.getClasse().getClasse(), "constante", tipo);
        		
        	}
        		
        		
        	
            this.token = proximo_token();
            
            if (this.token.getValor().equals("=")) {
                this.token = proximo_token();
                constante(tipo);
                multiConst(tipo);
                
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

    private void constante(String tipo) {
    	
        if (this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                | this.token.getClasse().equals(Classe.NUMERO)) {
        	
        	//Erro semï¿½ntico
        	if(!token.getClasse().equals(Classe.CADEIA_DE_CARACTERES) && tipo.equals("texto")) {
        		
        		System.out.println("ERRO: constantes do tipo texto so podem receber cadeias de caracteres");
        		novoErroSemantico(this.token.getLinha(),"ERRO: constantes do tipo texto so podem receber cadeias de caracteres" );
                
        	} else if(tipo.equals("inteiro") && token.getClasse().equals(Classe.NUMERO) ) {
        		
        		if(token.getValor().contains(".")) {
        			System.out.println("ERRO: constantes do tipo inteiro nao pode receber numero real");
        			novoErroSemantico(this.token.getLinha(),"ERRO: constantes do tipo inteiro nï¿½o pode receber numero real");
        		}
        	} else if(tipo.equals("real") && token.getClasse().equals(Classe.NUMERO)) {
        		
        		if(!token.getValor().contains(".")) {
        			System.out.println("ERRO: constantes do tipo real nao pode receber numero inteiro");
        			novoErroSemantico(this.token.getLinha(),"ERRO: constantes do tipo real nao pode receber numero inteiro");
        		}
        	} else if((tipo.equals("real") || tipo.equals("inteiro")) && !token.getClasse().equals(Classe.NUMERO) ) {
        		
        		System.out.println("ERRO: a constante do tipo "+ tipo + " aguarda um nï¿½mero");
        		novoErroSemantico(this.token.getLinha(),"ERRO: a constante do tipo "+ tipo + " aguarda um nï¿½mero");
        	}
            
        	this.token = proximo_token();
        	
        } else {
            System.out.println("ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador");
            novoErro(this.token.getLinha(),"ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador" );
        	this.recuperacaoDeErro();
        }
    }

    private void multiConst(String tipo) {
        if (pertenceAoPrimeiroDe("multiplasConstantes")) {
            multiplasConstantes(tipo);
            
        }else {
            return;
        }
    }

    private void multiplasConstantes(String tipo) {
        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            constantes(tipo);
            
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
            
            if(!isNumeroInteiro(this.token.getValor())){
                System.out.println("ERRO: Indice(s) de vetores ou matrizes devem ser um numeros inteiros");
            	novoErroSemantico(this.token.getLinha(),"ERRO: Indice(s) de vetores ou matrizes devem ser um numeros inteiros");
            }
            
            this.token = proximo_token();

        } else if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();

        } else {
        	System.out.println("ERRO: falta um numero ou um identificador");
            novoErro(this.token.getLinha(),"ERRO: falta um numero ou um identificador");
        	this.recuperacaoDeErro();
        }
    }

    
    /* 
        checar se o indice Ã© inteiro
        checar se o indice estÃ¡ entre 0 e o numero declarado?
        checar se o vetor nÃ£o estÃ¡ sendo referenciado como matriz, ou o contrÃ¡rio
    */
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

            if (this.token.getClasse().equals(Classe.IDENTIFICADOR) || this.token.getValor().equals("principal") ) {
            	
            	escopo = token.getValor();
            	
            	//Erro Semantico
            	if(this.buscaEscopo(escopo)== null) {
            		this.addEscopo(escopo);
            		
            	} else {
            		System.out.println("ERRO: metodo ja existe");
            		novoErroSemantico(this.token.getLinha(),"ERRO: metodo ja existe");
            	}
            	
            	this.token = proximo_token();                

                if (this.token.getValor().equals("(")) {
                    this.token = proximo_token();
                    listaParametros();
                    
                    if (this.token.getValor().equals(")")) {
                    	this.token = proximo_token();
                    	
                        if (this.token.getValor().equals(":")) {
                            this.token = proximo_token();
                            
                            if (this.tipo.contains(this.token.getValor())) {
                            	Escopo e = this.buscaEscopo(escopo);
                            	e.setRetorno(this.token.getValor());
                            	
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
    	String tipo, cadeia;
    	
        if (this.tipo.contains(this.token.getValor())) {
        	tipo = token.getValor();
            this.token = proximo_token();

            if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
            	
            	if(this.hasParamentro( token.getValor())) {
            		System.out.println("ERRO: parametros com identificadores iguais");
            		novoErroSemantico(this.token.getLinha(),"ERRO: parametros com identificadores iguais");
            	
            	} else {
            		this.addParamentos(tipo, token.getValor());
            	}
            	
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
        if(pertenceAoPrimeiroDe("comandos")){
            comandos();
            escopoMetodo();
        }
    }

    private void chamadaDeMetodo() {
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
        	ChamadaMetodo cm = this.addChamadaMetodo(this.token.getValor(), escopo, token.getLinha());
			this.token = proximo_token();

        	            
            if(this.token.getValor().equals("(")){
                this.token = proximo_token();
                var(cm);
                
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

    private void var(ChamadaMetodo cm) {
    	
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
        	cm.addParametro(token.getValor());
            this.token = proximo_token();
            vetor();
            maisVariavel(cm);
            
        }else if(pertenceAoPrimeiroDe("metodoParametro")){
        	metodoParametro(cm);
        	
        }
    }
    
    private void maisVariavel(ChamadaMetodo cm) {
        if(this.token.getValor().equals(",")){
            this.token = proximo_token();
            var(cm);
        }
    }

   
    private void metodoParametro(ChamadaMetodo cm) {
    	
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
        	cm.addParametro(this.token.getValor());
            this.token = proximo_token();
            
            if(this.token.getValor().equals("(")){
                this.token = proximo_token();
                var(cm);
                
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                    maisVariavel(cm);
                    
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
            String type = this.token.getValor();
            
            if(type.equals("vazio")){
                System.out.println("ERRO: o tipo vazio nao pode ser usado em declaracoes de variaveis");
                novoErroSemantico(this.token.getLinha(),"ERRO: o tipo vazio nao pode ser usado em declaracoes de variaveis" );	
            }
            
            this.token = proximo_token();
            complementoV(type);
            maisVariaveis();
            
        }else{
        	System.out.println("ERRO: aguarda-se um tipo de variavel boleano/inteiro/real/texto");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um tipo de variÃ¡vel boleano/inteiro/real/texto");
        	this.recuperacaoDeErro();
        }
    }
    
private void complementoV(String tipo) {
        
       if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
    	  
           String caso = "nenhum";
           
    	   if(this.isConstante(token.getValor())) {
    		   System.out.println("ERRO: variavel com identificador igual ao identificador da constante");
    		   novoErroSemantico(this.token.getLinha(),"ERRO: variavel com identificador igual ao identificador da constante");
    	   }
    	   
    	   if(!this.hasVariarel(token.getValor())) {
    		   this.addVariaveis(token.getValor(), token.getClasse().getClasse(), tipo, caso);
    		   
    	   } else {
    		   System.out.println("ERRO: variaveis com identificadores iguais");
       			novoErroSemantico(this.token.getLinha(),"ERRO: variaveis com identificadores iguais");  
    	   }
    	   
           this.token = proximo_token();
           vetor();
           variavelMesmoTipo(tipo);
           
       }else {
    	   
    	   System.out.println("ERRO: faltou um identificador");
           novoErro(this.token.getLinha(),"ERRO: faltou um identificador");
       	   this.recuperacaoDeErro();
    	   
       }
       
    }

    private void variavelMesmoTipo(String tipo) {
        if(this.token.getValor().equals(",")) {
            this.token = proximo_token();
            complementoV(tipo);
            
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
        if(pertenceAoPrimeiroDe("leia")){
            leia();
            
        }else if(pertenceAoPrimeiroDe("escreva")){
            escreva();
            
        }else if(pertenceAoPrimeiroDe("se")){
            se();
            
        }else if(pertenceAoPrimeiroDe("enquanto")){
            enquanto();
            
        }else if(pertenceAoPrimeiroDe("atribuicaoVariavel") && !this.listaDeTokens.get(0).getValor().equals("(") && 
        		!this.listaDeTokens.get(0).getClasse().equals(Classe.OPERADOR_ARITMETICO)){
            atribuicaoVariavel();
            
        }else if(pertenceAoPrimeiroDe("chamadaDeMetodo") && this.listaDeTokens.get(0).getValor().equals("(")){
            chamadaDeMetodo();
            
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
                
            }else{
            	 System.out.println("ERRO: faltou , ou ;");
                 novoErro(this.token.getLinha(),"ERRO: faltou , ou ;");
             	 this.recuperacaoDeErro();
            }
            
        }else if(pertenceAoPrimeiroDe("incrementador")){
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
		if(pertenceAoPrimeiroDe("verificaCaso")) {
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
		
		if(pertenceAoPrimeiroDe("termo")) {
			termo();
			
			if(token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
	            this.token = proximo_token();
				termo();
				
			} else {
				System.out.println("ERRO: faltou operador relacional");
	            novoErro(this.token.getLinha(),"ERRO: faltou operador relacional");
	            this.recuperacaoDeErro();
			}
			
		} else if(pertenceAoPrimeiroDe("negar")) {
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
		
		if(pertenceAoPrimeiroDe("tipoTermo")) {
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
		
		if(pertenceAoPrimeiroDe("comandos")) {
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

            
		} 
		
	}

	
	private void condSenao() {
		
		if(this.token.getValor().equals("se")) {
            this.token = proximo_token();
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
    	
		if(pertenceAoPrimeiroDe("comandos")) {
			comandos();
			conteudoLaco();
		}
		
	}

	private void operacaoRelacional() {
		
		if(pertenceAoPrimeiroDe("complementoOperador")) {
			complementoOperador();
			
			if(token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
	            this.token = proximo_token();
				complementoOperador();

			}
		} else if(pertenceAoPrimeiroDe("negar")) {
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
        	
        	//Erro semantico
        	if(this.isConstante(token.getValor())) {
        		System.out.println("ERRO: atribuicao de constante");
        		novoErroSemantico(this.token.getLinha(),"ERRO: atribuicaoo de constante");    
        		
        	}else if(!this.hasVariarel( token.getValor())) {
        		System.out.println("ERRO: variavel nao declarada");
        		novoErroSemantico(this.token.getLinha(),"ERRO: variavel nao declarada");
        		
        	}
        		
        	Escopo e = this.buscaEscopo(escopo);
        	this.var = e.getVariavel(this.token.getValor());
        	        	
        	
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
            Escopo e = this.buscaEscopo(escopo);
            
            if(!e.isVariavel(this.token.getValor()) && !isConstante(this.token.getValor())) {
            	System.out.println("ERRO: variavel nao declarada");
            	novoErroSemantico(this.token.getLinha(),"ERRO: variavel nao declarada");		
            }
            
            if(e.isVariavel(this.token.getValor()) && !e.getTipo(this.token.getValor()).equals("inteiro")) {
            	System.out.println("ERRO: incrementadores so podem ser utilizados em variaveis do tipo inteiro");
            	novoErroSemantico(this.token.getLinha(),"ERRO: incrementadores so podem ser utilizados em variaveis do tipo inteiro");	
            }
            
            this.token = proximo_token();
            vetor();
            
            if(isIncrementador(this.token.getValor())){ //pra checar se eh ++ ou --
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
    	
        if(pertenceAoPrimeiroDe("verificaCaso")){
        	Escopo e = this.buscaEscopo(escopo);
        	this.var = new String[4];
        	this.var[2] =e.getRetorno();
        	
            verificaCaso();
        }
        
        
    }
    
    private void verificaCaso() {
    	
    	if (token.getValor().equals("(")) {
            this.token = proximo_token();
    		verificaCaso();
    		
    		//Tratando a expressao
    		if(token.getValor().equals(")")) {
                this.token = proximo_token();
                auxiliarW();
                
    		} else {
    			System.out.println("ERRO: faltou fechar )");
                novoErro(this.token.getLinha(),"ERRO: faltou fechar )");
                this.recuperacaoDeErro();
    		}
    		
    	} else if(token.getValor().equals("++") | token.getValor().equals("--")) {
    		
    		if(var != null && !var[2].equals("inteiro")) {
    			System.out.println("ERRO: incrementador só pode ser usando em tipo inteiro");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: incrementador só pode ser usando em tipo inteiro" );
    		}
    		
            this.token = proximo_token();
            
            if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
            	Escopo e = this.buscaEscopo(escopo);
            	            	
            	if(var != null && !var[2].equals(e.getTipo(token.getValor()))) {
        			System.out.println("ERRO: atribuicao com tipos incompatives");
            		this.novoErroSemantico(this.token.getLinha(),"ERRO: atribuicao com tipos incompatives" );
        		}            	
            	
                this.token = proximo_token();
                vetor();
                
            } else {
            	
            	System.out.println("ERRO: faltou identificador");
                novoErro(this.token.getLinha(),"ERRO: faltou identificador");
                this.recuperacaoDeErro();
            }

    	} else if(token.getValor().equals("verdadeiro") | token.getValor().equals("falso")) {
    		
    		if(this.var != null && !var[2].equals("boleano")) {
    			System.out.println("ERRO: atribuicao com tipos incompatives");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: atribuicao com tipos incompatives" );
    		}
    		
            this.token = proximo_token();

    	} else if(token.getValor().equals("!") ) {
    		
    		if(this.var != null && !var[2].equals("boleano")) {
    			System.out.println("ERRO: ! so pode ser usado em atribuicao de boleanos");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: ! so pode ser usado em atribuicao de boleanos" );
    		}
    		
            this.token = proximo_token();
    		auxiliarC();
    		
    	} else if (pertenceAoPrimeiroDe("expressao")) {
    		expressao();
    		
    	} else {
    		
    		System.out.println("ERRO: erro na atribuiï¿½ï¿½o de variavel");
            novoErro(this.token.getLinha(),"ERRO: erro na atribuiï¿½ï¿½o de variavel");
            this.recuperacaoDeErro();
    	}
    	
    }

    
    private void auxiliarW() {
		if(token.getClasse().equals(Classe.OPERADOR_ARITMETICO)) {
            this.token = proximo_token();
            expressao();
		}
		
	}

	private void auxiliarC() {
		// TODO Auto-generated method stub
    	if(token.getValor().equals("verdadeiro") | token.getValor().equals("falso")) {
            this.token = proximo_token();

    	} else if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();
            vetor();
            
        } else {

    		System.out.println("ERRO: erro na declaracao de variavel");
            novoErro(this.token.getLinha(),"ERRO: erro na declaracao de variavel");
            this.recuperacaoDeErro();
        }
	}

	
	
	private void expressao() {
		
		if(this.pertenceAoPrimeiroDe("multExp")) {
			multExp();
			auxiliarK();
			
		} else {
			System.out.println("ERRO: erro na expressao");
            novoErro(this.token.getLinha(),"ERRO: erro na expressao");
            this.recuperacaoDeErro();
		}
		
	}

	private void auxiliarK() {
		
		if(token.getValor().equals("-")| token.getValor().equals("+")){
			
			if(this.var != null && var[2].equals("boleano")) {
    			System.out.println("ERRO: nao se faz opercao com tipo boleano");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: nao se faz opercao com tipo boleano" );
    		
			} else if(this.var != null && var[2].equals("texto") && !token.getValor().equals("+")) {
    			System.out.println("ERRO: Em tipo texto so e possivel fazer operacoes de adicao");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: Em tipo texto so e possivel fazer operacoes de adicao" );
    		}
			
            this.token = proximo_token();
            multExp();
            auxiliarK();
		}
		
	}

	private void multExp() {
		
		if(this.pertenceAoPrimeiroDe("operador")) {
			operador();
			auxiliarY();
		} else {
			System.out.println("ERRO: aguardava-se um operador");
            novoErro(this.token.getLinha(),"ERRO: aguardava-se um operador");
            this.recuperacaoDeErro();
		}
		
	}

	private void auxiliarY() {
		
		if(token.getValor().equals('*')| token.getValor().equals('/')){
			
			if(this.var != null && var[2].equals("boleano")) {
    			System.out.println("ERRO: nao se faz operacao com tipo boleano");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: nao se faz opercao com tipo boleano" );
    		
			} else if(this.var != null && var[2].equals("texto")) {
    			System.out.println("ERRO: Em tipo texto so e possivel fazer operacoes de adicao");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: Em tipo texto so e possivel fazer operacoes de adicao" );
    		}
			
            this.token = proximo_token();
            operador();
            auxiliarY();
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
     
    
    
    private void operador() {
    	
        if( this.token.getClasse().equals(Classe.NUMERO) | this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)){
            
        	if(this.var != null && ((this.token.getClasse().equals(Classe.NUMERO) &&
        			!var[2].equals("inteiro") && !var[2].equals("real")) ||
        			(!var[2].equals("texto") && this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)))) {
        		
    			System.out.println("ERRO: atribuicao com tipo incompativel");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: atribuicao com tipo incompativel" );
    		
			}
        	
        	this.token = proximo_token();    
            
        }else if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
        	
        	Escopo e = this.buscaEscopo(escopo);
        	String t = e.getTipo(token.getValor());
        	
        	if( t == null) {
        		System.out.println("ERRO: variavel nao declarada");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: variavel nao declarada" );
        	
        	} else         	
        	if(var != null &&  !t.equals(var[2])) {
        		System.out.println("ERRO: atribuicao com tipo incompativel");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: atribuicao com tipo incompativel" );
        	}
        	
            this.token =proximo_token();
            vetor();
            auxiliarF();
            
        }else if(pertenceAoPrimeiroDe("chamadaDeMetodo")){
            chamadaDeMetodo();
        
        } else if(this.token.getValor().equals("(")) {
            this.token =proximo_token();
            expressao();
            
            if(this.token.getValor().equals(")")) {
                this.token =proximo_token();
                
            } else {
            	System.out.println("ERRO: faltou o simbolo )");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo )");
                this.recuperacaoDeErro();
            }
            
            
        	
        }else {
        	System.out.println("ERRO: operador invalido");
            novoErro(this.token.getLinha(),"ERRO: operador invalido");
            this.recuperacaoDeErro();
        }
    
    }
    
    private void auxiliarF() {
		
    	if(token.getValor().equals("++")|token.getValor().equals("--")) {
    		token = proximo_token();
    	}
		
	}

	private boolean pertenceAoPrimeiroDe(String naoTerminal){
        
        switch (naoTerminal) {
        
           case "metodo":
                return token.getValor().equals("metodo");
                
            case "multiplasConstantes":    
                return token.getValor().equals(",");
                
            case "comandos":    
                return token.getValor().equals("resultado") | token.getValor().equals("leia") | token.getValor().equals("escreva")
                		| token.getValor().equals("se") | token.getValor().equals("enquanto") | token.getClasse().equals(Classe.IDENTIFICADOR)  ;
            
            case "metodoParametro":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "leia":    
                return token.getValor().equals("leia");
                
            case "escreva":    
                return token.getValor().equals("escreva");
                
            case "se":    
                return token.getValor().equals("se");
                
            case "enquanto":    
                return token.getValor().equals("enquanto");
                
            case "atribuicaoVariavel":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "chamadaDeMetodo":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "incrementador":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "verificaCaso":    
                return  token.getValor().equals("(") | token.getValor().equals("!")| token.getValor().equals("verdadeiro")| token.getValor().equals("falso")
                		| token.getClasse().equals(Classe.IDENTIFICADOR)| token.getClasse().equals(Classe.NUMERO)
                		| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)| token.getClasse().equals(Classe.OPERADOR_ARITMETICO);
                
            case "termo":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
                		| token.getValor().equals("falso");
                
            case "negar":    
                return true; //aceita vazio
                
            case "tipoTermo":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
                		| token.getValor().equals("falso");
                
            case "conseSe":    
                return token.getValor().equals("(");
                
            case "complementoOperador":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
                		| token.getValor().equals("falso");
                
            case "expressao":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "multExp":    
            	return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR);
            	
            case "operador":    
            	return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) ;
                		
        }
    
        return false;
        
    }
    
    private void escreveSaida(String arquivo) {
		
		arquivo = arquivo.substring(0,  arquivo.indexOf("."));
		        
		ManipuladorDeArquivo escrita;
		try {
		            
		            escrita = new ManipuladorDeArquivo(arquivo + ".saida", Modo.ESCRITA);
		
		            if(!this.erros.isEmpty() && !this.listaDeTokens.isEmpty()){
		             
		                for (int i = 0; i < this.erros.size(); i++) {
		                        if (i == 0) {
		                                System.out.println("\nErros Sintï¿½ticos\n");
		                                escrita.escreverArquivo("\r\n Erros Sintï¿½ticos \r\n");
		                        }
		                        Erro e = this.erros.get(i);
		
		                        System.out.println(e.getLinha() + " - " + e.getErro());
		                        escrita.escreverArquivo(e.getLinha() + " - " + e.getErro()+ "\r\n");
		                }
		            }
		            
		           
                    
		            if (this.errosSemanticos.isEmpty()) {
		            	String s = "SUCESSO: NENHUM ERRO FOI ENCONTRADO!";
                		
                        System.out.println(s);
                        escrita.escreverArquivo(s);
                        
		            } else {
		            	
		            	 System.out.println("\nLista de Erros Semanticos\n");
		                    escrita.escreverArquivo("\nLista de Erros Semanticos\n");
		            	
		            	for (int i = 0; i < this.errosSemanticos.size(); i++) {
	                        	           Erro e = this.errosSemanticos.get(i);
	
	                        System.out.println(e.getLinha() + " - " + e.getErro());
	                        escrita.escreverArquivo(e.getLinha() + " - " + e.getErro()+ "\r\n");
	                }
		            	
		            }
		
		escrita.fechaArquivo();
		
		        
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}


    private void addConstantes(String cadeia, String token, String categoria, String tipo ) {
    	String [] s = new String[4];
    	s[0] = cadeia;
    	s[1] = token;
    	s[2] = categoria;
    	s[3] = tipo;
    	this.constantes.add(s);
    }
    
    private boolean isConstante(String id) {
    	int tam, aux =0;
    	tam = constantes.size();
    	
    	while(aux < tam) {
    		
    		if(constantes.get(aux)[0].equals(id)) {
    			return true;
    		}
    		aux = aux + 1;
    	}
    	
    	return false;
    }
    
    private void addEscopo(String nome) {
    	this.escopos.add(new Escopo(nome));
    }
    
    private boolean hasVariarel( String cadeia) {
    	Escopo e = buscaEscopo(escopo);

    	return e.isVariavel(cadeia);
    }
    
    private void addVariaveis(String cadeia, String token, String tipo, String caso) {
    	
        Escopo e = buscaEscopo(escopo);
    	
    	if(e != null) {
    		e.addVariaveis(cadeia, token, tipo, caso);
    	}
    	
    }
    
    private String getTipo( String cadeia) {
    	Escopo e = buscaEscopo(escopo);
    	
    	if(e != null) {
    		return e.getTipo(cadeia);
    	}
    	
    	return null;
    	
    }
    
    private Escopo buscaEscopo(String nome) {
    	int tam, aux=0;
    	
    	tam = escopos.size();
    	
    	while(aux < tam) {
    		Escopo e = escopos.get(aux);
    		
    		if(e.getNome().equals(nome)) {
    			return e;
    		}
    		
    		aux = aux +1;
    	}
    	
    	return null;
    }
    
    private void novoErroSemantico(int linha, String erro) {
    	this.errosSemanticos.add(new Erro(linha, erro));

    }
    
    private void addParamentos( String tipo, String cadeia) {
    	Escopo e = this.buscaEscopo(escopo);
    	
    	e.addParametros(tipo, cadeia);
    }
    
    private boolean hasParamentro( String cadeia) {
    	Escopo e = this.buscaEscopo(escopo);
    	
    	return e.hasParamentro(cadeia);
    	
    }
    
    private boolean isNumeroInteiro(String numero){
        return numero.matches("[0-9]*");
    }
    
    private ChamadaMetodo addChamadaMetodo(String id, String escopo, int linha) {

		ChamadaMetodo cm = new ChamadaMetodo(id, escopo, linha);
		this.callMedotos.add(cm);

		return cm;
	}
    
    public String[] getConstante(String id) {
		int tam, aux = 0;
		tam = constantes.size();

		while (aux < tam) {

			if (constantes.get(aux)[0].equals(id)) {
				return constantes.get(aux);
			}
			aux = aux + 1;
		}

		return null;
	}
 
}
