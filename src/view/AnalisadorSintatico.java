/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

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
	private String tipoVariavelAnterior;
	private int ordem;
	private Token[] elementos;
	private ArrayList<String> retorno;
	private Escopo escopo_atual;

	public AnalisadorSintatico(ArrayList<Token> listaDeTokens) {
		this.listaDeTokens = listaDeTokens;
		this.listaDeTokens.add(listaDeTokens.size(), new Token("$", Classe.FINALIZADOR, 0)); // add o '$' no final da
																								// lista
		this.setup();
		erros = new ArrayList<Erro>();
		constantes = new ArrayList<String[]>();
		escopos = new ArrayList<Escopo>();
		errosSemanticos = new ArrayList<Erro>();
		callMedotos = new ArrayList<ChamadaMetodo>();

	}

	public void setup() {
		// Tipos de variaveis
		tipo.add("inteiro");
		tipo.add("real");
		tipo.add("vazio");
		tipo.add("boleano");
		tipo.add("texto");

		this.elementos = new Token[3];
	}

	private void novoErro(int linha, String erro) {
		this.erros.add(new Erro(linha, erro));
	}

	private void recuperacaoDeErro() {

		int linha = this.token.getLinha();
		linha++;

		while (!token.getValor().equals("$") && token.getLinha() != linha) {
			this.token = proximo_token();
		}

	}

	public void executar(String arquivo) {
		// estaPresenteNaListaDeTokens(":");
		this.token = proximo_token();

		programa();

		if (this.token.getValor().equals("$")) {
			System.out.println("SUCESSO: codigo encerrado com o caractere finalizador $");
		} else {
			System.out.println("ERRO: codigo encerrado sem atingir o $");
		}

		if (this.buscaEscopo("principal") == null) {
			System.out.println("ERRO: metodo principal n�o existe");
			novoErroSemantico(-1, "ERRO: metodo principal n�o existe");
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
				System.out.println("ERRO: metodo " + cm.getId() + " n�o existe");
				novoErroSemantico(cm.getLinha(), "ERRO: metodo " + cm.getId() + " n�o existe");

			} else {

				parametros = e.getParametros();
				paraChamada = cm.getParametros();
				int tam, aux = 0;
				tam = parametros.size();

				if (parametros.size() > paraChamada.size()) {
					System.out.println("ERRO: falta parametros");
					novoErroSemantico(cm.getLinha(), "ERRO: falta parametros");

				} else if (parametros.size() < paraChamada.size()) {
					System.out.println("ERRO: h� parametros a mais");
					novoErroSemantico(cm.getLinha(), "ERRO: h� parametros a mais");
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
						
						if(cadeia.length() > 2) {
						
						String m = cadeia.substring(0, cadeia.length()-2);
						Escopo chamada = this.buscaEscopo(m);
						
						if(chamada != null && !chamada.getRetorno().equals(tipo)) {
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
					novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo }");
					this.recuperacaoDeErro();

				}

			} else {

				System.out.println("ERRO: esta faltando o simbolo {");
				novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo {");
				this.recuperacaoDeErro();
			}

		} else {

			novoErro(this.token.getLinha(), "ERRO: esta faltando a palavra 'programa'");
			this.recuperacaoDeErro();
			System.out.println("ERRO: esta faltando a palavra 'programa'");
		}

	}

	private void escopoPrograma() {
		if (pertenceAoPrimeiroDe("metodo")) {
			metodo();// token = "metodo"
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
					novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo }");
					this.recuperacaoDeErro();
				}

			} else {
				System.out.println("ERRO: esta faltando o simbolo {");
				novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo {");
				this.recuperacaoDeErro();
			}

		} else {
			return;
		}

	}

	private void estruturaConstantes() {

		if (this.tipo.contains(this.token.getValor())) { // se token == tipo

			String type = this.token.getValor();

			if (type.equals("vazio")) {
				System.out.println("ERRO: o tipo vazio nao pode ser usado em declaracoes de constantes");
				novoErroSemantico(this.token.getLinha(),
						"ERRO: o tipo vazio nao pode ser usado em declaracoes de constantes");
			}

			this.token = proximo_token();
			constantes(type);

			if (this.token.getValor().equals(";")) {
				this.token = proximo_token();
				estruturaConstantes();

			} else {
				System.out.println("ERRO: esta faltando o simbolo ;");
				novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo ;");
				this.recuperacaoDeErro();
			}

		} else {// mesmo problema se checar vazio
			return;
		}
	}

	private void constantes(String tipo) {
		if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) { // token == identificador

			// Erro Semantico
			if (this.isConstante(token.getValor())) {
				System.out.println("ERRO: identificador duplicado");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: identificador duplicado");

			} else {
				this.addConstantes(token.getValor(), token.getClasse().getClasse(), "constante", tipo);

			}

			this.token = proximo_token();

			if (this.token.getValor().equals("=")) {
				this.token = proximo_token();
				constante(tipo);
				multiConst(tipo);

			} else {
				System.out.println("ERRO: faltou o caractere =");
				novoErro(this.token.getLinha(), "ERRO: faltou o caractere =");
				this.recuperacaoDeErro();
			}
		} else {
			System.out.println("ERRO: declaracao de constante sem identificador");
			novoErro(this.token.getLinha(), "ERRO: declaracao de constante sem identificador");
			this.recuperacaoDeErro();
		}
	}

	private void constante(String tipo) {

		if (this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES) | this.token.getClasse().equals(Classe.NUMERO)) {

			// Erro sem�ntico
			if (!token.getClasse().equals(Classe.CADEIA_DE_CARACTERES) && tipo.equals("texto")) {

				System.out.println("ERRO: constantes do tipo texto so podem receber cadeias de caracteres");
				novoErroSemantico(this.token.getLinha(),
						"ERRO: constantes do tipo texto so podem receber cadeias de caracteres");

			} else if (tipo.equals("inteiro") && token.getClasse().equals(Classe.NUMERO)) {

				if (token.getValor().contains(".")) {
					System.out.println("ERRO: constantes do tipo inteiro nao pode receber numero real");
					novoErroSemantico(this.token.getLinha(),
							"ERRO: constantes do tipo inteiro n�o pode receber numero real");
				}
			} else if (tipo.equals("real") && token.getClasse().equals(Classe.NUMERO)) {

				if (!token.getValor().contains(".")) {
					System.out.println("ERRO: constantes do tipo real nao pode receber numero inteiro");
					novoErroSemantico(this.token.getLinha(),
							"ERRO: constantes do tipo real nao pode receber numero inteiro");
				}
			} else if ((tipo.equals("real") || tipo.equals("inteiro")) && !token.getClasse().equals(Classe.NUMERO)) {

				System.out.println("ERRO: a constante do tipo " + tipo + " aguarda um n�mero");
				novoErroSemantico(this.token.getLinha(), "ERRO: a constante do tipo " + tipo + " aguarda um n�mero");
			}

			this.token = proximo_token();

		} else {
			System.out.println("ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador");
			novoErro(this.token.getLinha(), "ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador");
			this.recuperacaoDeErro();
		}
	}

	private void multiConst(String tipo) {
		if (pertenceAoPrimeiroDe("multiplasConstantes")) {
			multiplasConstantes(tipo);

		} else {
			return;
		}
	}

	private void multiplasConstantes(String tipo) {
		if (this.token.getValor().equals(",")) {
			this.token = proximo_token();
			constantes(tipo);

		} else {
			System.out.println("ERRO: faltou virugula na declaracao de multiplas constantes");
			novoErro(this.token.getLinha(), "ERRO: faltou virugula na declaracao de multiplas constantes");
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
						novoErro(this.token.getLinha(), "ERRO: faltou ; no fim do comando leia");
						this.recuperacaoDeErro();
					}
				} else {
					System.out.println("ERRO: faltou faltou o simbolo)");
					novoErro(this.token.getLinha(), "ERRO: faltou faltou o simbolo)");
					this.recuperacaoDeErro();
				}
			} else {
				System.out.println("ERRO: faltou o simbolo (");
				novoErro(this.token.getLinha(), "ERRO: faltou o simbolo (");
				this.recuperacaoDeErro();
			}
		} else {
			System.out.println("ERRO: faltou a palavra 'leia'");
			novoErro(this.token.getLinha(), "ERRO: faltou a palavra 'leia'");
			this.recuperacaoDeErro();

		}
	}

	private void conteudoLeia() {

		if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
			String v = token.getValor();
			this.token = proximo_token();
			vetor(v);
			lermais();

		} else {
			System.out.println("ERRO: faltou paramentros no comando leia");
			novoErro(this.token.getLinha(), "ERRO: faltou paramentros no comando leia");
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

			if (!isNumeroInteiro(this.token.getValor())) {
				System.out.println("ERRO: Indice(s) de vetores ou matrizes devem ser um numeros inteiros");
				novoErroSemantico(this.token.getLinha(),
						"ERRO: Indice(s) de vetores ou matrizes devem ser um numeros inteiros");
			}

			this.token = proximo_token();

		} else if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
			this.token = proximo_token();

		} else {
			System.out.println("ERRO: falta um numero ou um identificador");
			novoErro(this.token.getLinha(), "ERRO: falta um numero ou um identificador");
			this.recuperacaoDeErro();
		}
	}

	/*
	 * checar se o indice é inteiro checar se o indice está entre 0 e o numero
	 * declarado? checar se o vetor não está sendo referenciado como matriz, ou o
	 * contrário
	 */
	private void vetor(String v) {
		if (this.token.getValor().equals("[")) {

			if (this.hasVariarel(v) && !this.isVetor(v) && !this.isMatriz(v)) {
				System.out.println("ERRO: variavel nao e vetor nem matriz");
				novoErroSemantico(this.token.getLinha(), "ERRO: variavel nao e vetor nem matriz");

			}

			this.token = proximo_token();
			opI2();
			opIndice();

			if (this.token.getValor().equals("]")) {
				this.token = proximo_token();
				matriz(v);
			} else {
				System.out.println("ERRO: esta faltando ]");
				novoErro(this.token.getLinha(), "ERRO: esta faltando ]");
				this.recuperacaoDeErro();

			}
		} else {
			if (this.isVetor(v) && this.isMatriz(v)) {
				System.out.println("ERRO: variavel referenciada errado ");
				novoErroSemantico(this.token.getLinha(), "ERRO: variavel referenciada errado ");

			}
		}
	}

	private void vetorDeclaracao(String v) {
		if (this.token.getValor().equals("[")) {

			this.setCasoVariavel(v, "vetor");
			this.token = proximo_token();
			opI2();
			opIndice();

			if (this.token.getValor().equals("]")) {
				this.token = proximo_token();
				matrizDeclaracao(v);
			} else {
				System.out.println("ERRO: esta faltando ]");
				novoErro(this.token.getLinha(), "ERRO: esta faltando ]");
				this.recuperacaoDeErro();

			}
		}
	}

	private void matrizDeclaracao(String v) {
		if (this.token.getValor().equals("[")) {
			this.setCasoVariavel(v, "matriz");

			this.token = proximo_token();
			opI2();
			opIndice();

			if (this.token.getValor().equals("]")) {
				this.token = proximo_token();

			} else {
				System.out.println("ERRO: esta faltando ]");
				novoErro(this.token.getLinha(), "ERRO: esta faltando ]");
				this.recuperacaoDeErro();

			}
		}
	}

	private void matriz(String v) {

		if (this.token.getValor().equals("[")) {
			this.token = proximo_token();

			if (this.isVetor(v)) {
				System.out.println("ERRO: vetor referenciado como matriz");
				novoErroSemantico(this.token.getLinha(), "ERRO: vetor referenciado como matriz");

			}

			opI2();
			opIndice();

			if (this.token.getValor().equals("]")) {
				this.token = proximo_token();

			} else {
				System.out.println("ERRO: esta faltando ]");
				novoErro(this.token.getLinha(), "ERRO: esta faltando ]");
				this.recuperacaoDeErro();

			}
		} else {
			if (this.isMatriz(v)) {
				System.out.println("ERRO: matriz referenciado como vetor");
				novoErroSemantico(this.token.getLinha(), "ERRO: matriz referenciado como vetor");

			}
		}
	}

	private void metodo() {

		if (this.token.getValor().equals("metodo")) {
			this.token = proximo_token();

			if (this.token.getClasse().equals(Classe.IDENTIFICADOR) || this.token.getValor().equals("principal")) {

				escopo = token.getValor();
				this.escopo_atual = new Escopo(escopo);

				this.token = proximo_token();

				if (this.token.getValor().equals("(")) {
					this.token = proximo_token();
					listaParametros();

					if (this.token.getValor().equals(")")) {
						this.token = proximo_token();

						if (this.token.getValor().equals(":")) {
							this.token = proximo_token();

							if (this.tipo.contains(this.token.getValor())) {
								escopo_atual.setRetorno(this.token.getValor());

								if (escopo.equals("principal") && !this.token.getValor().equals("vazio")) {
									System.out.println("ERRO: o retorno do metodo principal so pode ser vazio");
									novoErroSemantico(this.token.getLinha(),
											"ERRO: o retorno do metodo principal so pode ser vazio");
								}
								
								Escopo e = this.buscaEscopo(escopo);
								
								if(e != null) {
									verificaSobreescrita(e);
								} else {
									escopos.add(this.escopo_atual);
								}
																	
								

								this.token = proximo_token();

								if (this.token.getValor().equals("{")) {
									this.token = proximo_token();
									declaracaoVariaveis();
									this.retorno = new ArrayList<String>();
									escopoMetodo();

									if (this.token.getValor().equals("}")) {

										if (!this.escopo_atual.getRetorno().equals("vazio") && !retorno.contains("resultado")) {
											System.out.println("ERRO: faltou retorno no metodo");
											novoErroSemantico(this.token.getLinha(), "ERRO: faltou retorno no metodo");
										}


										this.token = proximo_token();

									} else {
										System.out.println("ERRO: faltou o }");
										novoErro(this.token.getLinha(), "ERRO: faltou o }");
										this.recuperacaoDeErro();

									}

								} else {
									System.out.println("ERRO: faltou o {");
									novoErro(this.token.getLinha(), "ERRO: faltou o {");
									this.recuperacaoDeErro();

								}

							} else {
								System.out.println("ERRO:faltou o tipo do retorno");
								novoErro(this.token.getLinha(), "ERRO:faltou o tipo do retorno");
								this.recuperacaoDeErro();

							}

						} else {
							System.out.println("ERRO: faltou o :");
							novoErro(this.token.getLinha(), "ERRO: faltou o :");
							this.recuperacaoDeErro();

						}
					} else {
						System.out.println("ERRO: faltou o )");
						novoErro(this.token.getLinha(), "ERRO: faltou o )");
						this.recuperacaoDeErro();

					}
				} else {
					System.out.println("ERRO: faltou o (");
					novoErro(this.token.getLinha(), "ERRO: faltou o (");
					this.recuperacaoDeErro();

				}
			} else {
				System.out.println("ERRO:falta identificacao do metodo");
				novoErro(this.token.getLinha(), "ERRO:falta identificacao do metodo");
				this.recuperacaoDeErro();

			}
		} else {
			System.out.println("ERRO:falta a palavra 'metodo'");
			novoErro(this.token.getLinha(), "ERRO:falta a palavra 'metodo'");
			this.recuperacaoDeErro();

		}
	}

	private void verificaSobreescrita(Escopo e) {
		ArrayList<String[]> atual, aux;
		boolean falhou = false;
		
		atual = this.escopo_atual.getParametros();
		aux = e.getParametros();
		
		if(atual.size() == aux.size()) {

			for(int i=0; i<atual.size(); i++) {
				
				if(!atual.get(i)[0].equals(aux.get(i)[0])) {
					falhou = true;
					break;
				}
			}
						
		} else {
			falhou = true;
		}
		
		
		if(!falhou && !e.getRetorno().equals(escopo_atual.getRetorno())) {
			falhou = true;
		}
		
		
		if(falhou) {
			escopos.add(escopo_atual);
			
		} else {
			System.out.println("ERRO: metodo sobreescrito");
			novoErroSemantico(this.token.getLinha(), "ERRO: metodo sobreescrito");

		}
	}

	private void listaParametros() {
		String tipo;

		if (this.tipo.contains(this.token.getValor())) {
			tipo = token.getValor();

			if (escopo.equals("principal")) {
				System.out.println("ERRO: metodo principal nao pode ter parametros");
				novoErroSemantico(this.token.getLinha(), "ERRO: metodo principal nao pode ter parametros");

			}

			this.token = proximo_token();

			if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {

				if (this.hasParamentro(token.getValor())) {
					System.out.println("ERRO: parametros com identificadores iguais");
					novoErroSemantico(this.token.getLinha(), "ERRO: parametros com identificadores iguais");

				} else {
					this.addParamentos(tipo, token.getValor());
				}

				this.token = proximo_token();
				maisParametros();

			} else {
				System.out.println("ERRO:falta um identificador");
				novoErro(this.token.getLinha(), "ERRO:falta um identificador");
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
		if (pertenceAoPrimeiroDe("comandos")) {
			comandos();
			escopoMetodo();
		}
	}

	private void chamadaDeMetodo() {
		if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
			ChamadaMetodo cm = this.addChamadaMetodo(this.token.getValor(), escopo, token.getLinha());
			this.token = proximo_token();

			if (this.token.getValor().equals("(")) {
				this.token = proximo_token();
				var(cm);

				if (this.token.getValor().equals(")")) {
					this.token = proximo_token();

				} else {
					System.out.println("ERRO: faltou )");
					novoErro(this.token.getLinha(), "ERRO: faltou )");
					this.recuperacaoDeErro();
				}
			} else {
				System.out.println("ERRO: faltou (");
				novoErro(this.token.getLinha(), "ERRO: faltou (");
				this.recuperacaoDeErro();
			}
		} else {
			System.out.println("ERRO: falta identificador");
			novoErro(this.token.getLinha(), "ERRO: falta identificador");
			this.recuperacaoDeErro();
		}
	}

	private void var(ChamadaMetodo cm) {

		if (this.token.getClasse().equals(Classe.IDENTIFICADOR) && !this.listaDeTokens.get(0).getValor().equals("(")) {
			cm.addParametro(token.getValor());
			String v = token.getValor();
			this.token = proximo_token();
			vetor(v);
			maisVariavel(cm);

		} else if (pertenceAoPrimeiroDe("metodoParametro")) {
			metodoParametro(cm);

		}
	}

	private void maisVariavel(ChamadaMetodo cm) {
		if (this.token.getValor().equals(",")) {
			this.token = proximo_token();
			var(cm);
		}
	}

	private void metodoParametro(ChamadaMetodo cm) {

		if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
			cm.addParametro(this.token.getValor()+"()");
			ChamadaMetodo cmp = this.addChamadaMetodo(this.token.getValor(), escopo, token.getLinha());
			this.token = proximo_token();

			if (this.token.getValor().equals("(")) {
				this.token = proximo_token();
				var(cmp);

				if (this.token.getValor().equals(")")) {
					this.token = proximo_token();
					maisVariavel(cmp);

				} else {
					System.out.println("ERRO: esta faltando o simbolo ) ");
					novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo ) ");
					this.recuperacaoDeErro();
				}
			} else {
				System.out.println("ERRO: esta faltando o simbolo ( ");
				novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo ( ");
				this.recuperacaoDeErro();
			}
		} else {
			System.out.println("ERRO: esta faltando o identificador ");
			novoErro(this.token.getLinha(), "ERRO: esta faltando o identificador ) ");
			this.recuperacaoDeErro();
		}
	}

	private void declaracaoVariaveis() {

		if (this.token.getValor().equals("variaveis")) {
			this.token = proximo_token();

			if (this.token.getValor().equals("{")) {
				this.token = proximo_token();
				varV();

				if (this.token.getValor().equals("}")) {
					this.token = proximo_token();

				} else {
					System.out.println("ERRO: esta faltando o simbolo } ");
					novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo } ");
					this.recuperacaoDeErro();
				}

			} else {
				System.out.println("ERRO: esta faltando o simbolo { ");
				novoErro(this.token.getLinha(), "ERRO: esta faltando o simbolo { ");
				this.recuperacaoDeErro();
			}
		}
	}

	private void varV() {

		if (this.tipo.contains(this.token.getValor())) {
			String type = this.token.getValor();

			if (type.equals("vazio")) {
				System.out.println("ERRO: o tipo vazio nao pode ser usado em declaracoes de variaveis");
				novoErroSemantico(this.token.getLinha(),
						"ERRO: o tipo vazio nao pode ser usado em declaracoes de variaveis");
			}

			this.token = proximo_token();
			complementoV(type);
			maisVariaveis();

		} else {
			System.out.println("ERRO: aguarda-se um tipo de variavel boleano/inteiro/real/texto");
			novoErro(this.token.getLinha(), "ERRO: aguarda-se um tipo de variável boleano/inteiro/real/texto");
			this.recuperacaoDeErro();
		}
	}

	private void complementoV(String tipo) {

		if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {

			String caso = "nenhum";
			if (this.isConstante(token.getValor())) {
				System.out.println("ERRO: variavel com identificador igual ao identificador da constante");
				novoErroSemantico(this.token.getLinha(),
						"ERRO: variavel com identificador igual ao identificador da constante");
			}

			if (!this.hasVariarel(token.getValor())) {
				this.addVariaveis(token.getValor(), token.getClasse().getClasse(), tipo, caso);

			} else {
				System.out.println("ERRO: variaveis com identificadores iguais");
				novoErroSemantico(this.token.getLinha(), "ERRO: variaveis com identificadores iguais");
			}

			String v = token.getValor();
			this.token = proximo_token();
			vetorDeclaracao(v);
			variavelMesmoTipo(tipo);

		} else {

			System.out.println("ERRO: faltou um identificador");
			novoErro(this.token.getLinha(), "ERRO: faltou um identificador");
			this.recuperacaoDeErro();

		}

	}

	private void variavelMesmoTipo(String tipo) {
		if (this.token.getValor().equals(",")) {
			this.token = proximo_token();
			complementoV(tipo);

		} else if (this.token.getValor().equals(";")) {
			this.token = proximo_token();

		} else {

			System.out.println("ERRO: faltou , ou ;");
			novoErro(this.token.getLinha(), "ERRO: faltou , ou ;");
			this.recuperacaoDeErro();
		}
	}

	private void maisVariaveis() {

		if (this.tipo.contains(this.token.getValor())) { // Primeiro("VarV") == Tipo
			varV();
		}
	}

	private void comandos() {
		if (pertenceAoPrimeiroDe("leia")) {
			leia();

		} else if (pertenceAoPrimeiroDe("escreva")) {
			escreva();

		} else if (pertenceAoPrimeiroDe("se")) {

			if (!retorno.isEmpty()) {

				if (!retorno.get(retorno.size() - 1).equals("se")
						&& !retorno.get(retorno.size() - 1).equals("enquanto")) {
					this.retorno.add("se");
				}

			} else {
				this.retorno.add("se");

			}
			se();
			this.retorno.add("fse");

		} else if (pertenceAoPrimeiroDe("enquanto")) {
			
			if (!retorno.isEmpty()) {

				if (!retorno.get(retorno.size() - 1).equals("se")
						&& !retorno.get(retorno.size() - 1).equals("enquanto")) {
					this.retorno.add("enquanto");
				}

			} else {
				this.retorno.add("enquanto");

			}

			
		enquanto();

		this.retorno.add("fenquanto");

	}else if(pertenceAoPrimeiroDe("atribuicaoVariavel") && !this.listaDeTokens.get(0).getValor().equals("(")
                && !this.listaDeTokens.get(0).getClasse().equals(Classe.OPERADOR_ARITMETICO)) {
            atribuicaoVariavel();

        } else if (pertenceAoPrimeiroDe("chamadaDeMetodo") && this.listaDeTokens.get(0).getValor().equals("(")) {
            chamadaDeMetodo();

            if (this.token.getValor().equals(";")) {
                this.token = proximo_token();

            } else {
                System.out.println("ERRO: faltou , ou ;");
                novoErro(this.token.getLinha(), "ERRO: faltou , ou ;");
                this.recuperacaoDeErro();
            }

        } else if (pertenceAoPrimeiroDe("incrementador")) {
            incrementador();

        } else if (this.token.getValor().equals("resultado")) {
        	
        	if (!retorno.isEmpty()) {

				if (!retorno.get(retorno.size() - 1).equals("se")
						&& !retorno.get(retorno.size() - 1).equals("enquanto")) {
		        	this.retorno.add("resultado");
				}

			} else {
	        	this.retorno.add("resultado");

			}

        	
        	
            this.token = proximo_token();
            retorno();

            if (this.token.getValor().equals(";")) {
                this.token = proximo_token();
            } else {
                System.out.println("ERRO: faltou , ou ;");
                novoErro(this.token.getLinha(), "ERRO: faltou , ou ;");
                this.recuperacaoDeErro();
            }
        }

    }

	private void escreva() {

		if (this.token.getValor().equals("escreva")) {
			this.token = proximo_token();

			if (this.token.getValor().equals("(")) {
				this.token = proximo_token();
				paramEscrita();

				if (this.token.getValor().equals(")")) {
					this.token = proximo_token();

					if (this.token.getValor().equals(";")) {
						this.token = proximo_token();

					} else {
						System.out.println("ERRO: faltou ;");
						novoErro(this.token.getLinha(), "ERRO: faltou ;");
						this.recuperacaoDeErro();
					}

				} else {

					System.out.println("ERRO: faltou )");
					novoErro(this.token.getLinha(), "ERRO: faltou )");
					this.recuperacaoDeErro();
				}

			} else {
				System.out.println("ERRO: faltou (");
				novoErro(this.token.getLinha(), "ERRO: faltou (");
				this.recuperacaoDeErro();
			}

		} else {
			System.out.println("ERRO: faltou a palavra 'escreva'");
			novoErro(this.token.getLinha(), "ERRO: faltou a palavra 'escreva'");
			this.recuperacaoDeErro();
		}

	}

	private void paramEscrita() {
		if (pertenceAoPrimeiroDe("verificaCaso")) {
			verificaCaso();
			maisParametrosE();

		} else {
			System.out.println("ERRO: sintaxe de parametro incorreta no comando escrita");
			novoErro(this.token.getLinha(), "ERRO: sintaxe de parametro incorreta no comando escrita");
			this.recuperacaoDeErro();
		}

	}

	private void maisParametrosE() {

		if (this.token.getValor().equals(",")) {
			this.token = proximo_token();
			paramEscrita();

		}

	}

	private void se() {

		if (this.token.getValor().equals("se")) {
			this.token = proximo_token();
			condSe();

			if (this.token.getValor().equals("entao")) {
				this.token = proximo_token();

				if (this.token.getValor().equals("{")) {
					this.token = proximo_token();
					blocoSe();

					if (this.token.getValor().equals("}")) {
						this.token = proximo_token();
						senao();

					} else {
						System.out.println("ERRO: faltou o simbolo }");
						novoErro(this.token.getLinha(), "ERRO: faltou o simbolo }");
						this.recuperacaoDeErro();
					}
				} else {
					System.out.println("ERRO: faltou o simbolo {");
					novoErro(this.token.getLinha(), "ERRO: faltou o simbolo {");
					this.recuperacaoDeErro();
				}

			} else {
				System.out.println("ERRO: faltou a palavra 'entao'");
				novoErro(this.token.getLinha(), "ERRO: faltou a palavra 'entao'");
				this.recuperacaoDeErro();
			}

		} else {
			System.out.println("ERRO: faltou a palavra 'se'");
			novoErro(this.token.getLinha(), "ERRO: faltou a palavra 'se'");
			this.recuperacaoDeErro();
		}
	}

	private void negar() {

		if (this.token.getValor().equals("!")) {
			this.token = proximo_token();
		}
	}

	private void maisCond() {

		if (this.token.getClasse().equals(Classe.OPERADOR_LOGICO)) {
			this.token = proximo_token();
			cond();
			maisCond();
		}

	}

	private void cond() {

		if (pertenceAoPrimeiroDe("termo")) {
			termo();

			if (token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
				this.elementos[2] = this.token;
				this.token = proximo_token();
				termo();

			} else {
				System.out.println("ERRO: faltou operador relacional");
				novoErro(this.token.getLinha(), "ERRO: faltou operador relacional");
				this.recuperacaoDeErro();
			}

		} else if (pertenceAoPrimeiroDe("negar")) {
			negar();

			if (token.getClasse().equals(Classe.IDENTIFICADOR)) {

				// checa se ja existe a variavel no escopo do metodo
				if (!this.isConstante(token.getValor()) && !this.hasVariarel(token.getValor())
						&& !this.hasParamentro(token.getValor())) {
					System.out.println("ERRO: uso de variável inexistente em condicional");
					novoErroSemantico(this.token.getLinha(), "ERRO: uso de variável inexistente em condicional");

				}

				String v = token.getValor();
				this.token = proximo_token();
				vetor(v);

			} else {
				System.out.println("ERRO: faltou identificador");
				novoErro(this.token.getLinha(), "ERRO: faltou identificador");
				this.recuperacaoDeErro();

			}

		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
			novoErro(this.token.getLinha(), "ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
			this.recuperacaoDeErro();
		}

	}

	private void termo() {

		if (pertenceAoPrimeiroDe("tipoTermo")) {
			tipoTermo();
			op();

		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
			novoErro(this.token.getLinha(), "ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
			this.recuperacaoDeErro();
		}

	}

	private void op() {

		if (this.token.getClasse().equals(Classe.OPERADOR_ARITMETICO)) {
			this.token = proximo_token();
			tipoTermo();
			op();
		}

	}

	private void tipoTermo() {

		if (token.getClasse().equals(Classe.IDENTIFICADOR)) {

			// checa se ja existe a variavel no escopo do metodo, ou se é constante ou
			// parametro.
			if (!this.isConstante(token.getValor()) && !this.hasVariarel(token.getValor())
					&& !this.hasParamentro(token.getValor())) {
				System.out.println("ERRO: uso de variável inexistente em condicional");
				novoErroSemantico(this.token.getLinha(), "ERRO: uso de variável inexistente em condicional");
				this.verificaOrdem();
			} else {
				this.verificaOrdem();

			}

			String v = token.getValor();
			this.token = proximo_token();
			vetor(v);

		} else if (token.getClasse().equals(Classe.NUMERO)) {
			this.verificaOrdem();
			this.token = proximo_token();

		} else if (token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
			this.verificaOrdem();
			this.token = proximo_token();

		} else if (token.getValor().equals("verdadeiro")) {
			this.token = proximo_token();

		} else if (token.getValor().equals("false")) {
			this.token = proximo_token();

		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
			novoErro(this.token.getLinha(), "ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
			this.recuperacaoDeErro();
		}
	}

	private void condSe() {

		if (token.getValor().equals("(")) {
			this.token = proximo_token();
			cond();
			maisCond();

			if (token.getValor().equals(")")) {
				this.token = proximo_token();

			} else {
				System.out.println("ERRO: faltou o simbolo )");
				novoErro(this.token.getLinha(), "ERRO: faltou o simbolo )");
				this.recuperacaoDeErro();
			}

		} else {
			System.out.println("ERRO: faltou o simbolo (");
			novoErro(this.token.getLinha(), "ERRO: faltou o simbolo (");
			this.recuperacaoDeErro();
		}

	}

	private void blocoSe() {

		if (pertenceAoPrimeiroDe("comandos")) {
			comandos();
			blocoSe();
		}

	}

	private void senao() {

		if (token.getValor().equals("senao")) {
			this.token = proximo_token();
			condSenao();

			if (token.getValor().equals("{")) {
				this.token = proximo_token();
				blocoSe();

				if (token.getValor().equals("}")) {
					this.token = proximo_token();
					senao();

				} else {
					System.out.println("ERRO: faltou o simbolo }");
					novoErro(this.token.getLinha(), "ERRO: faltou o simbolo }");
					this.recuperacaoDeErro();
				}

			} else {
				System.out.println("ERRO: faltou o simbolo {");
				novoErro(this.token.getLinha(), "ERRO: faltou o simbolo {");
				this.recuperacaoDeErro();
			}

		}

	}

	private void condSenao() {

		if (this.token.getValor().equals("se")) {
			this.token = proximo_token();
			condSe();

			if (token.getValor().equals("entao")) {
				this.token = proximo_token();

			} else {
				System.out.println("ERRO: faltou a palavra 'entao");
				novoErro(this.token.getLinha(), "ERRO: faltou a palavra 'entao");
				this.recuperacaoDeErro();
			}

		}

	}

	private void enquanto() {
		if (token.getValor().equals("enquanto")) {
			this.token = proximo_token();

			if (token.getValor().equals("(")) {
				this.token = proximo_token();
				operacaoRelacional();

				if (token.getValor().equals(")")) {
					this.token = proximo_token();

					if (token.getValor().equals("{")) {
						this.token = proximo_token();
						conteudoLaco();

						if (token.getValor().equals("}")) {
							this.token = proximo_token();

						} else {
							System.out.println("ERRO: faltou o simbolo }");
							novoErro(this.token.getLinha(), "ERRO: faltou o simbolo }");
							this.recuperacaoDeErro();
						}

					} else {
						System.out.println("ERRO: faltou o simbolo {");
						novoErro(this.token.getLinha(), "ERRO: faltou o simbolo {");
						this.recuperacaoDeErro();
					}

				} else {
					System.out.println("ERRO: faltou o simbolo )");
					novoErro(this.token.getLinha(), "ERRO: faltou o simbolo )");
					this.recuperacaoDeErro();
				}

			} else {
				System.out.println("ERRO: faltou o simbolo (");
				novoErro(this.token.getLinha(), "ERRO: faltou o simbolo (");
				this.recuperacaoDeErro();
			}

		} else {
			System.out.println("ERRO: faltou a palavra 'enquanto'");
			novoErro(this.token.getLinha(), "ERRO: faltou a palavra 'enquanto'");
			this.recuperacaoDeErro();
		}
	}

	private void conteudoLaco() {

		if (pertenceAoPrimeiroDe("comandos")) {
			comandos();
			conteudoLaco();
		}

	}

	private void operacaoRelacional() {

		if (pertenceAoPrimeiroDe("complementoOperador")) {
			complementoOperador();

			if (token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
				this.token = proximo_token();
				complementoOperador();

			}
		} else if (pertenceAoPrimeiroDe("negar")) {
			negar();

			if (token.getClasse().equals(Classe.IDENTIFICADOR)) {
				String v = token.getValor();
				this.token = proximo_token();
				vetor(v);

			} else {
				System.out.println("ERRO: faltou identificador");
				novoErro(this.token.getLinha(), "ERRO: faltou identificador");
				this.recuperacaoDeErro();
			}

		} else {

			System.out.println("ERRO: erro de sintaxe na condicao do enquanto");
			novoErro(this.token.getLinha(), "ERRO: erro de sintaxe na condicao do enquanto");
			this.recuperacaoDeErro();
		}
	}

	private void complementoOperador() {

		if (token.getClasse().equals(Classe.IDENTIFICADOR)) {
			String v = token.getValor();
			this.token = proximo_token();
			vetor(v);

		} else if (token.getClasse().equals(Classe.NUMERO)) {
			this.token = proximo_token();

		} else if (token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
			this.token = proximo_token();

		} else if (token.getValor().equals("verdadeiro")) {
			this.token = proximo_token();

		} else if (token.getValor().equals("falso")) {
			this.token = proximo_token();

		} else {
			System.out.println("ERRO: erro de sintaxe na condicao do enquanto");
			novoErro(this.token.getLinha(), "ERRO: erro de sintaxe na condicao do enquanto");
			this.recuperacaoDeErro();
		}

	}

	private void atribuicaoVariavel() {
		if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {

			// Erro semantico
			if (this.isConstante(token.getValor())) {
				System.out.println("ERRO: atribuicao de constante");
				novoErroSemantico(this.token.getLinha(), "ERRO: atribuicaoo de constante");

			} else if (!this.hasVariarel(token.getValor()) && !this.hasParamentro(token.getValor())) {
				System.out.println("ERRO: variavel nao declarada");
				novoErroSemantico(this.token.getLinha(), "ERRO: variavel nao declarada");

			} else {

			}

			Escopo e = this.buscaEscopo(escopo);
			this.var = e.getVariavel(this.token.getValor());

			this.token = proximo_token();

			if (var != null) {
				vetor(var[0]);
			} else {
				vetor(null);
			}

			if (this.token.getValor().equals("=")) {
				this.token = proximo_token();
				verificaCaso();

				if (this.token.getValor().equals(";")) {
					this.token = proximo_token();

				} else {
					System.out.println("ERRO: faltou o simbolo ;");
					novoErro(this.token.getLinha(), "ERRO: faltou o simbolo ;");
					this.recuperacaoDeErro();
				}
			} else {
				System.out.println("ERRO: faltou o simbolo =");
				novoErro(this.token.getLinha(), "ERRO: faltou o simbolo =");
				this.recuperacaoDeErro();
			}

		} else {
			System.out.println("ERRO: faltou identificador");
			novoErro(this.token.getLinha(), "ERRO: faltou identificador");
			this.recuperacaoDeErro();
		}
	}

	private void incrementador() {

		if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {

			String t = getTipo(this.token.getValor());

			if (t == null) {
				System.out.println("ERRO: variavel nao declarada");
				novoErroSemantico(this.token.getLinha(), "ERRO: variavel nao declarada");

			} else if (!getTipo(this.token.getValor()).equals("inteiro")) {
				System.out.println("ERRO: incrementadores so podem ser utilizados em variaveis do tipo inteiro");
				novoErroSemantico(this.token.getLinha(),
						"ERRO: incrementadores so podem ser utilizados em variaveis do tipo inteiro");
			}

			String v = token.getValor();
			this.token = proximo_token();
			vetor(v);

			if (isIncrementador(this.token.getValor())) { // pra checar se eh ++ ou --
				this.token = proximo_token();

				if (this.token.getValor().equals(";")) {
					this.token = proximo_token();

				} else {
					String s = "Erro de Sintaxe: falta o ;";
					System.out.println(s);
					novoErro(this.token.getLinha(), s);
					this.recuperacaoDeErro();
				}
			} else {
				String s = "Erro de Sintaxe: deveria ser ++ ou -- ;";
				System.out.println(s);
				novoErro(this.token.getLinha(), s);
				this.recuperacaoDeErro();
			}
		}
	}

	private void retorno() {

		if (pertenceAoPrimeiroDe("verificaCaso")) {
			Escopo e = this.buscaEscopo(escopo);
			this.var = new String[4];
			this.var[2] = e.getRetorno();

			verificaCaso();
		}

	}

	private void verificaCaso() {

		if (token.getValor().equals("(")) {
			this.token = proximo_token();
			verificaCaso();

			// Tratando a expressao
			if (token.getValor().equals(")")) {
				this.token = proximo_token();
				auxiliarW();

			} else {
				System.out.println("ERRO: faltou fechar )");
				novoErro(this.token.getLinha(), "ERRO: faltou fechar )");
				this.recuperacaoDeErro();
			}

		} else if (token.getValor().equals("++") | token.getValor().equals("--")) {

			if (var != null && !var[2].equals("inteiro")) {
				System.out.println("ERRO: incrementador s� pode ser usando em tipo inteiro");
				this.novoErroSemantico(this.token.getLinha(),
						"ERRO: incrementador s� pode ser usando em tipo inteiro");
			}

			this.token = proximo_token();

			if (token.getClasse().equals(Classe.IDENTIFICADOR)) {

				if (var != null && !var[2].equals(getTipo(token.getValor()))) {
					System.out.println("ERRO: atribuicao com tipos incompatives");
					this.novoErroSemantico(this.token.getLinha(), "ERRO: atribuicao com tipos incompatives");

				}
				String v = token.getValor();
				this.token = proximo_token();
				vetor(v);

			} else {

				System.out.println("ERRO: faltou identificador");
				novoErro(this.token.getLinha(), "ERRO: faltou identificador");
				this.recuperacaoDeErro();
			}

		} else if (token.getValor().equals("verdadeiro") | token.getValor().equals("falso")) {

			if (this.var != null && !var[2].equals("boleano")) {
				System.out.println("ERRO: atribuicao com tipos incompatives");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: atribuicao com tipos incompatives");
			}

			this.token = proximo_token();

		} else if (token.getValor().equals("!")) {

			if (this.var != null && !var[2].equals("boleano")) {
				System.out.println("ERRO: ! so pode ser usado em atribuicao de boleanos");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: ! so pode ser usado em atribuicao de boleanos");
			}

			this.token = proximo_token();
			auxiliarC();

		} else if (pertenceAoPrimeiroDe("expressao")) {
			expressao();

		} else {

			System.out.println("ERRO: erro na atribui��o de variavel");
			novoErro(this.token.getLinha(), "ERRO: erro na atribui��o de variavel");
			this.recuperacaoDeErro();
		}

	}

	private void auxiliarW() {
		if (token.getClasse().equals(Classe.OPERADOR_ARITMETICO)) {
			this.token = proximo_token();
			expressao();
		}

	}

	private void auxiliarC() {
		// TODO Auto-generated method stub
		if (token.getValor().equals("verdadeiro") | token.getValor().equals("falso")) {
			this.token = proximo_token();

		} else if (token.getClasse().equals(Classe.IDENTIFICADOR)) {
			String v = token.getValor();
			this.token = proximo_token();
			vetor(v);

		} else {

			System.out.println("ERRO: erro na declaracao de variavel");
			novoErro(this.token.getLinha(), "ERRO: erro na declaracao de variavel");
			this.recuperacaoDeErro();
		}
	}

	private void expressao() {

		if (this.pertenceAoPrimeiroDe("multExp")) {
			multExp();
			auxiliarK();

		} else {
			System.out.println("ERRO: erro na expressao");
			novoErro(this.token.getLinha(), "ERRO: erro na expressao");
			this.recuperacaoDeErro();
		}

	}

	private void auxiliarK() {

		if (token.getValor().equals("-") | token.getValor().equals("+")) {

			if (this.var != null && var[2].equals("boleano")) {
				System.out.println("ERRO: nao se faz opercao com tipo boleano");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: nao se faz opercao com tipo boleano");

			} else if (this.var != null && var[2].equals("texto") && !token.getValor().equals("+")) {
				System.out.println("ERRO: Em tipo texto so e possivel fazer operacoes de adicao");
				this.novoErroSemantico(this.token.getLinha(),
						"ERRO: Em tipo texto so e possivel fazer operacoes de adicao");
			}

			this.token = proximo_token();
			multExp();
			auxiliarK();
		}

	}

	private void multExp() {

		if (this.pertenceAoPrimeiroDe("operador")) {
			operador();
			auxiliarY();
		} else {
			System.out.println("ERRO: aguardava-se um operador");
			novoErro(this.token.getLinha(), "ERRO: aguardava-se um operador");
			this.recuperacaoDeErro();
		}

	}

	private void auxiliarY() {

		if (token.getValor().equals("*") || token.getValor().equals("/")) {

			if (this.var != null && var[2].equals("boleano")) {
				System.out.println("ERRO: nao se faz operacao com tipo boleano");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: nao se faz opercao com tipo boleano");

			} else if (this.var != null && var[2].equals("texto")) {
				System.out.println("ERRO: Em tipo texto so e possivel fazer operacoes de adicao");
				this.novoErroSemantico(this.token.getLinha(),
						"ERRO: Em tipo texto so e possivel fazer operacoes de adicao");
			}

			this.token = proximo_token();
			operador();
			auxiliarY();
		}

	}

	private boolean isIncrementador(String s) {
		String[] partes = s.split("");
		if (partes.length == 2) {
			if ((partes[0].equals("+") && partes[1].equals("+")) | ((partes[0].equals("-") && partes[1].equals("-")))) {
				return true;
			}
		}

		return false;
	}

	private void operador() {

		if (this.token.getClasse().equals(Classe.NUMERO) | this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {

			if (this.var != null && ((this.token.getClasse().equals(Classe.NUMERO) && !var[2].equals("inteiro")
					&& !var[2].equals("real"))
					|| (!var[2].equals("texto") && this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)))) {

				System.out.println("ERRO: operacao com tipo incompativel");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: operacao com tipo incompativel");

			}

			this.token = proximo_token();

		} else if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {

			String t = getTipo(token.getValor());

			if (t == null) {
				System.out.println("ERRO: variavel nao declarada");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: variavel nao declarada");

			} else if (var != null && !t.equals(var[2])) {
				System.out.println("ERRO: operacao com tipo incompativel");
				this.novoErroSemantico(this.token.getLinha(), "ERRO: operacao com tipo incompativel");
			}

			String v = token.getValor();
			this.token = proximo_token();
			vetor(v);
			auxiliarF();

		} else if (pertenceAoPrimeiroDe("chamadaDeMetodo")) {
			chamadaDeMetodo();

		} else if (this.token.getValor().equals("(")) {
			this.token = proximo_token();
			expressao();

			if (this.token.getValor().equals(")")) {
				this.token = proximo_token();

			} else {
				System.out.println("ERRO: faltou o simbolo )");
				novoErro(this.token.getLinha(), "ERRO: faltou o simbolo )");
				this.recuperacaoDeErro();
			}

		} else {
			System.out.println("ERRO: operador invalido");
			novoErro(this.token.getLinha(), "ERRO: operador invalido");
			this.recuperacaoDeErro();
		}

	}

	private void auxiliarF() {

		if (token.getValor().equals("++") | token.getValor().equals("--")) {
			token = proximo_token();
		}

	}

	private boolean pertenceAoPrimeiroDe(String naoTerminal) {

		switch (naoTerminal) {

		case "metodo":
			return token.getValor().equals("metodo");

		case "multiplasConstantes":
			return token.getValor().equals(",");

		case "comandos":
			return token.getValor().equals("resultado") | token.getValor().equals("leia")
					| token.getValor().equals("escreva") | token.getValor().equals("se")
					| token.getValor().equals("enquanto") | token.getClasse().equals(Classe.IDENTIFICADOR);

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
			return token.getValor().equals("(") | token.getValor().equals("!") | token.getValor().equals("verdadeiro")
					| token.getValor().equals("falso") | token.getClasse().equals(Classe.IDENTIFICADOR)
					| token.getClasse().equals(Classe.NUMERO) | token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
					| token.getClasse().equals(Classe.OPERADOR_ARITMETICO);

		case "termo":
			return token.getClasse().equals(Classe.NUMERO) | token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
					| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
					| token.getValor().equals("falso");

		case "negar":
			return true; // aceita vazio

		case "tipoTermo":
			return token.getClasse().equals(Classe.NUMERO) | token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
					| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
					| token.getValor().equals("falso");

		case "conseSe":
			return token.getValor().equals("(");

		case "complementoOperador":
			return token.getClasse().equals(Classe.NUMERO) | token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
					| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
					| token.getValor().equals("falso");

		case "expressao":
			return token.getClasse().equals(Classe.NUMERO) | token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
					| token.getClasse().equals(Classe.IDENTIFICADOR);

		case "multExp":
			return token.getClasse().equals(Classe.NUMERO) | token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
					| token.getClasse().equals(Classe.IDENTIFICADOR);

		case "operador":
			return token.getClasse().equals(Classe.NUMERO) | token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
					| token.getClasse().equals(Classe.IDENTIFICADOR);

		}

		return false;

	}

	private void escreveSaida(String arquivo) {

		arquivo = arquivo.substring(0, arquivo.indexOf("."));

		ManipuladorDeArquivo escrita;
		try {

			escrita = new ManipuladorDeArquivo(arquivo + ".saida", Modo.ESCRITA);

			if (!this.erros.isEmpty() && !this.listaDeTokens.isEmpty()) {

				for (int i = 0; i < this.erros.size(); i++) {
					if (i == 0) {
						System.out.println("\nErros Sint�ticos\n");
						escrita.escreverArquivo("\r\n Erros Sint�ticos \r\n");
					}
					Erro e = this.erros.get(i);

					System.out.println(e.getLinha() + " - " + e.getErro());
					escrita.escreverArquivo(e.getLinha() + " - " + e.getErro() + "\r\n");
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
					escrita.escreverArquivo(e.getLinha() + " - " + e.getErro() + "\r\n");
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

	private void addConstantes(String cadeia, String token, String categoria, String tipo) {
		String[] s = new String[4];
		s[0] = cadeia;
		s[1] = token;
		s[2] = categoria;
		s[3] = tipo;
		this.constantes.add(s);
	}

	private boolean isConstante(String id) {
		int tam, aux = 0;
		tam = constantes.size();

		while (aux < tam) {

			if (constantes.get(aux)[0].equals(id)) {
				return true;
			}
			aux = aux + 1;
		}

		return false;
	}

	

	private boolean hasVariarel(String cadeia) {
		Escopo e = buscaEscopo(escopo);

		return e.isVariavel(cadeia);
	}

	private void addVariaveis(String cadeia, String token, String tipo, String caso) {

		Escopo e = buscaEscopo(escopo);

		if (e != null) {
			e.addVariaveis(cadeia, token, tipo, caso);
		}

	}

	private String getTipo(String cadeia) {
		Escopo e = buscaEscopo(escopo);

		if (e != null) {
			return e.getTipo(cadeia);
		}

		if (this.isConstante(cadeia)) {
			String[] c = this.getConstante(cadeia);
			return c[3];
		}

		return null;

	}

	private Escopo buscaEscopo(String nome) {
		int tam, aux = 0;

		tam = escopos.size();

		while (aux < tam) {
			Escopo e = escopos.get(aux);

			if (e.getNome().equals(nome)) {
				return e;
			}

			aux = aux + 1;
		}

		return null;
	}

	private void novoErroSemantico(int linha, String erro) {
		this.errosSemanticos.add(new Erro(linha, erro));

	}

	private void addParamentos(String tipo, String cadeia) {
		
		this.escopo_atual.addParametros(tipo, cadeia);
	}

	private boolean hasParamentro(String cadeia) {

		return this.escopo_atual.hasParamentro(cadeia);

	}

	private boolean isNumeroInteiro(String numero) {
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

	public void setCasoVariavel(String v, String caso) {
		Escopo e = this.buscaEscopo(escopo);

		e.setCasoVariavel(v, caso);

	}

	public boolean isVetor(String v) {
		Escopo e = this.buscaEscopo(escopo);

		return e.isVetor(v);

	}

	public boolean isMatriz(String v) {
		Escopo e = this.buscaEscopo(escopo);

		return e.isMatriz(v);

	}

	private void verificaOrdem() {
		if (this.ordem == 0) {
			this.elementos[0] = this.token;
			this.ordem++;
		} else if (this.ordem == 1) {
			this.elementos[1] = this.token;
			boolean compativeis = this.verificaCompatibilidade();

			if (!compativeis) {
				System.out.println("ERRO: tipos incompativeis na estutura condicional");
				novoErroSemantico(this.token.getLinha(), "ERRO: tipos incompativeis na estutura condicional");
			}

			this.ordem--;
		} else {
			System.out.println("ordem = " + ordem);
		}
	}

	private boolean verificaCompatibilidade() {

		Token t1 = this.elementos[0];
		Token t2 = this.elementos[1];
		Token t3 = this.elementos[2];
		String tipo1, tipo2;

		tipo1 = this.getTipo(t1.getValor());
		tipo2 = this.getTipo(t2.getValor());

		// se t1 ou t2 forem IDENTIFICADORES, o tipo não será null.
		// caso contrário, é necessário dar um tipo, pois t1 e t2 não são
		// variaveis. Ex: t1 = 5, t2 = "oi"

		if (tipo1 == null) {
			if (t1.getClasse().equals(Classe.NUMERO)) {
				if (this.isNumeroInteiro(t1.getValor())) {
					tipo1 = "inteiro";
				} else {
					tipo1 = "real";
				}
			} else if (t1.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
				if (t1.getValor().equals("verdadeiro") || t1.getValor().equals("falso")) {
					tipo1 = "boleano";
				} else {
					tipo1 = "texto";
				}
			} else {
				System.out.println("ERRO: o token t1 =" + (t1.toString()) + " nao possui tipo");
				tipo1 = "inexistente1";
			}
		}

		if (tipo2 == null) {
			if (t2.getClasse().equals(Classe.NUMERO)) {
				if (this.isNumeroInteiro(t2.getValor())) {
					tipo2 = "inteiro";
				} else {
					tipo2 = "real";
				}
			} else if (t2.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
				if (t2.getValor().equals("verdadeiro") || t2.getValor().equals("falso")) {
					tipo2 = "boleano";
				} else {
					tipo2 = "texto";
				}
			} else {
				System.out.println("ERRO: o token t2 =" + (t2.toString()) + " nao possui tipo");
				tipo2 = "inexistente2";
			}
		}

		if (t3.getValor().equals("<") || t3.getValor().equals("<=") || t3.getValor().equals(">")
				|| t3.getValor().equals(">=")) {

			if (tipo1.equals("texto") || tipo1.equals("boleano") || tipo2.equals("texto") || tipo2.equals("boleano")) {
				System.out.println("ERRO: operacao relacional incompativel com os tipos da estutura condicional");
				novoErroSemantico(this.token.getLinha(),
						"ERRO: operacao relacional incompativel com os tipos da estutura condicional");
			}
		} else if (t3.getValor().equals("=")) {
			System.out.println("ERRO: operacao relacional de atribuicao dentro de estutura condicional");
			novoErroSemantico(this.token.getLinha(),
					"ERRO: operacao relacional de atribuicao dentro de estutura condicional");
		}

		this.elementos[0] = null;
		this.elementos[1] = null;
		this.elementos[2] = null;

		return tipo1.equals(tipo2);
	}

}
