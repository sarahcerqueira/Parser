/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import model.AutomatoCadeiaCaractere;
import model.AutomatoComentario;
import model.AutomatoDelimitador;
import model.AutomatoIdentificador;
import model.AutomatoNumeros;
import model.AutomatoOperAritmetico;
import model.AutomatoOperLogico;
import model.AutomatoOperRelacionais;
import util.Classe;
import util.ManipuladorDeArquivo;
import util.Modo;
import util.ModoException;
import util.PalavraReservada;
import util.Token;

/**
 * Esta classe cumpre a tarefa de um analisador Lexico
 *
 * @author User-PC
 */
public class AnalisadorLexico {

	private AutomatoCadeiaCaractere cadeia;
	private AutomatoComentario comentario;
	private AutomatoDelimitador delimitador;
	private AutomatoIdentificador identificador;
	private AutomatoNumeros numero;
	private AutomatoOperAritmetico aritmetico;
	private AutomatoOperLogico logico;
	private AutomatoOperRelacionais relacional;
	private ArrayList<Token> listaDeTokens;
	private ArrayList<Token> listaDeErro;

	private String lexema;

	public void executar(String arquivo) throws FileNotFoundException, IOException, ModoException {

		this.inicializar();
		ManipuladorDeArquivo leitura = new ManipuladorDeArquivo(arquivo, Modo.LEITURA);

		Classe classe = Classe.NULL; // Para Operadores e delimitadores
		Classe classe1 = Classe.NULL; // Para comentarios, Numeros, identificadores, cadeias

		char c;
		char proximo = ' '; // Próximo caractere
		boolean aceito = false; // Aceito, automato em estado final
		boolean ajuste = true; // Se proximo for usado
		boolean ativo = false; // Auxilia na verificação de erros
		boolean fim = false; // Flag auxiliar apar determian o fim da classificação
		boolean loop = leitura.hasNextCaractere(); // verifica se há caractere há ser lido
		int linha = 1; // Conta as linhas

		// Este loop faz todo processo de classificação
		while (loop) {

			c = proximo;
			proximo = leitura.nextCaractere();

			// Se ajuste é falso significa que o próximo não foi utilizado em apenas um loop
			if (!ajuste) {

				if (c == '\r' || c == '\n' || c == '\t') {

					// Aceito significa que há um token salvar
					if (aceito) {
						this.classificao(classe1, linha);
						this.resetLexema();
						this.resetAutomatos();
						classe1 = Classe.NULL;
						aceito = false;
						ativo = true;

					} else if (!classe1.equals(Classe.NULL) && !classe1.equals(Classe.COMENTARIO)) {
						classe1 = Classe.ERRO;
						this.classificao(classe1, linha);
						this.resetLexema();
						classe1 = Classe.NULL;
						this.resetAutomatos();
					}

					if (c == '\n') {
						linha++;

					}

					if (!classe1.equals(Classe.COMENTARIO) && !classe1.equals(Classe.CADEIA_DE_CARACTERES)) {
						ativo = true;
					}

				} else if (c == ' ') {

					if (aceito) {
						this.classificao(classe1, linha);
						this.resetLexema();
						classe1 = Classe.NULL;
						this.resetAutomatos();
						aceito = false;

					} else if (classe1.equals(Classe.ERRO)) {
						this.classificao(classe1, linha);
						this.resetLexema();
						classe1 = Classe.NULL;
						this.resetAutomatos();

					} else if (!classe1.equals(Classe.NULL) && !classe1.equals(Classe.CADEIA_DE_CARACTERES)
							&& !classe1.equals(Classe.COMENTARIO) && !classe1.equals(Classe.NUMERO)) {
						classe = Classe.ERRO;
						this.classificao(classe1, linha);
						this.resetLexema();
						classe1 = Classe.NULL;
						this.resetAutomatos();
					}

					if (!classe1.equals(Classe.CADEIA_DE_CARACTERES) && !classe1.equals(Classe.COMENTARIO)
							&& !classe1.equals(Classe.NUMERO)) {
						ativo = true;
					}
				}

				// Checa se o caracter é um delimitador ou operador.
				classe = this.isdelimitacao(c);

				//Se classe é NULL c não é operador nem delimitador
				if (!classe.equals(Classe.NULL)) {

					switch (classe.getClasse()) {

					case ("DELIMITADOR"):
						
						// O ponto também pode fazer parte de um número
						if (classe1.equals(Classe.NUMERO) && c == '.') {
							
							classe = Classe.NULL;
							break;
						}
						
						//Se aceito é true, significa que antes do delimitador havia um token formado
						if (aceito) {
							this.classificao(classe1, linha);
							this.resetLexema();
							classe1 = Classe.NULL;
							this.resetAutomatos();
							aceito = false;
							
						/*Se Aceito é false e um delimitador foi encontrado o lexema anterior está
						 * incompleto logo é considerado erro. Entretanto se classe1 é um cometário
						 * ou cadeia de caracteres eles aceitam também delimitadores e operadores logicos*/
						} else if (!classe1.equals(Classe.NULL) && !classe1.equals(Classe.COMENTARIO)
								&& !classe1.equals(Classe.CADEIA_DE_CARACTERES)) {
							
							classe1 = Classe.ERRO;
							this.classificao(classe1, linha);
							this.resetLexema();
							classe1 = Classe.NULL;
							this.resetAutomatos();
						}

					
						if ((!classe1.equals(Classe.COMENTARIO) && !classe1.equals(Classe.CADEIA_DE_CARACTERES))
								|| (classe1.equals(Classe.COMENTARIO) && aceito)
								|| (classe1.equals(Classe.CADEIA_DE_CARACTERES) && aceito)) {

							this.concat(c);
							this.classificao(classe, linha);
							this.resetLexema();

						} else {
							classe = Classe.NULL;
						}

						break;

					case ("OPERADOR LOGICO"):
						if (aceito) {
							this.classificao(classe1, linha);
							this.resetLexema();
							classe1 = Classe.NULL;
							this.resetAutomatos();
							aceito = false;

						} else if (!classe1.equals(Classe.NULL) && !classe1.equals(Classe.COMENTARIO)
								&& !classe1.equals(Classe.CADEIA_DE_CARACTERES)) {
							classe1 = Classe.ERRO;
							this.classificao(classe1, linha);
							this.resetLexema();
							classe1 = Classe.NULL;
							this.resetAutomatos();
						}

						if ((!classe1.equals(Classe.COMENTARIO) && !classe1.equals(Classe.CADEIA_DE_CARACTERES))
								|| (classe1.equals(Classe.COMENTARIO) && aceito)
								|| (classe1.equals(Classe.CADEIA_DE_CARACTERES) && aceito)) {

							this.concat(c);
							this.classificao(classe, linha);

							if (this.logico.isOperLogico(proximo)) {
								ajuste = true;
								this.apagaTokenAnt();
								this.concat(proximo);
								this.classificao(classe, linha);
							}

							this.resetLexema();
						} else {
							classe = Classe.NULL;
						}

						break;

					case ("OPERADOR ARITMETICO"):

						if (c == '/' && (proximo == '/' || proximo == '*')) {
							classe1 = Classe.COMENTARIO;
							classe = Classe.NULL;
							break;
						} else if (classe1.equals(Classe.COMENTARIO)) {
							classe = Classe.NULL;
							break;
						}

						if (aceito) {
							this.classificao(classe1, linha);
							this.resetLexema();
							classe1 = Classe.NULL;
							this.resetAutomatos();
							aceito = false;

						} else if (!classe1.equals(Classe.NULL) && !classe1.equals(Classe.COMENTARIO)
								&& !classe1.equals(Classe.CADEIA_DE_CARACTERES)) {
							classe1 = Classe.ERRO;
							this.classificao(classe1, linha);
							this.resetLexema();
							classe1 = Classe.NULL;
							this.resetAutomatos();
						}

						if ((!classe1.equals(Classe.COMENTARIO) && !classe1.equals(Classe.CADEIA_DE_CARACTERES))
								|| (classe1.equals(Classe.COMENTARIO) && aceito)
								|| (classe1.equals(Classe.CADEIA_DE_CARACTERES) && aceito)) {
							this.concat(c);
							this.classificao(classe, linha);

							if (this.aritmetico.isOperAritmetico(proximo)) {
								ajuste = true;
								this.apagaTokenAnt();
								this.concat(proximo);
								this.classificao(classe, linha);
							}
							this.resetLexema();
						} else {
							classe = Classe.NULL;
						}

						break;

					case ("OPERADOR RELACIONAL"):
						if (aceito) {
							this.classificao(classe1, linha);
							this.resetLexema();
							this.resetAutomatos();
							classe1 = Classe.NULL;
							aceito = false;

						} else if (!classe1.equals(Classe.NULL) && !classe1.equals(Classe.COMENTARIO)
								&& !classe1.equals(Classe.CADEIA_DE_CARACTERES)) {
							classe1 = Classe.ERRO;
							this.classificao(classe1, linha);
							this.resetLexema();
							classe1 = Classe.NULL;
							this.resetAutomatos();
						}

						if ((!classe1.equals(Classe.COMENTARIO) && !classe1.equals(Classe.CADEIA_DE_CARACTERES))
								|| (classe1.equals(Classe.COMENTARIO) && aceito)
								|| (classe1.equals(Classe.CADEIA_DE_CARACTERES) && aceito)) {

							this.concat(c);

							this.classificao(classe, linha);

							if (this.relacional.isOperRelacional(proximo)) {
								ajuste = true;
								this.apagaTokenAnt();
								this.concat(proximo);
								this.classificao(classe, linha);
							} else {

								this.resetDelimitacao();

								if (this.relacional.isOperRelacional(proximo)) {
									ajuste = true;
									this.apagaTokenAnt();
									this.concat(proximo);
									classe = Classe.ERRO;
									this.classificao(classe, linha);
								}
							}

							this.resetLexema();
						} else {
							classe = Classe.NULL;
						}

						break;

					}// Fim switch

				/*Se c não é operador nem delimitado, e classe1 ainda é NULL, c pode ser numero
				 * comentario, identificado, ou uma cadeia*/
					
				} else {
					if (classe1.equals(Classe.NULL)) {
						classe1 = this.getClasse(c);
					}
					
					this.resetDelimitacao(); 

				}
				
				/*Se classe == NULL e ativo é falso significa que não houve classificação antes
				 * e então aqui os automatados devem continuar identificando*/
				if (classe.equals(Classe.NULL) && !ativo) {
					
					switch (classe1.getClasse()) {

					case ("COMENTARIO"):
						if (this.comentario.isComentario(c)) {
							aceito = this.comentario.isEstadoFinal();
						} else if (this.comentario.isEstadoErro()) {
							classe1 = Classe.ERRO;
						}

						this.concat(c);

						if (fim && aceito) {
							this.classificao(classe1, linha);

						} else if (fim && !aceito) {
							this.classificao(Classe.ERRO, linha);

						}

						break;
					case ("IDENTIFICADOR"):
						if (this.identificador.isIdentificador(c)) {
							aceito = this.identificador.isEstadoFinal();
						} else if (this.identificador.isEstadoErro()) {
							classe1 = Classe.ERRO;
						}

						this.concat(c);

						if (fim && aceito) {
							this.classificao(classe1, linha);

						} else if (fim && !aceito) {
							this.classificao(Classe.ERRO, linha);

						}

						break;
					case ("NUMERO"):
						if (this.numero.isNumero(c)) {
							aceito = this.numero.isEstadoFinal();
						} else if (this.numero.isEstadoErro()) {
							classe1 = Classe.ERRO;
						}

						this.concat(c);

						if (fim && aceito) {
							this.classificao(classe1, linha);

						} else if (fim && !aceito) {
							this.classificao(Classe.ERRO, linha);

						}

						break;
					case ("CADEIA DE CARACTERES"):
						if (this.cadeia.isCadeiaCaractere(c)) {
							aceito = this.cadeia.isEstadoFinal();
						} else if (this.cadeia.isEstadoErro()) {
							classe1 = Classe.ERRO;
						}

						this.concat(c);

						if (fim && aceito) {
							this.classificao(classe1, linha);

						} else if (fim && !aceito) {
							this.classificao(Classe.ERRO, linha);

						}

						break;
					case ("NULL"):
						classe1 = Classe.ERRO;
						aceito = true;
						this.concat(c);
						break;

					case ("ERRO"):
						this.concat(c);
					}

				}
				
			} else {
				ajuste = false;
			}

			if (ativo) {
				ativo = false;
			}

			loop = leitura.hasNextCaractere();

			if (!loop && !fim) {

				fim = true;
				loop = true;

			} else if (!loop && fim) {
				loop = false;
			}
		}
		
		leitura.fechaArquivo();
		ManipuladorDeArquivo escrita = new ManipuladorDeArquivo(arquivo + ".saida", Modo.ESCRITA);

		for (int i = 0; i < this.listaDeTokens.size(); i++) {
			if (i == 0) {
				System.out.println("Tokens Válidos\n");
				escrita.escreverArquivo("Tokens Válidos\r\n");
			}
			Token t = this.listaDeTokens.get(i);
			System.out.println(t.getLinha() + " " + t.getValor() + " " + t.getClasse().getClasse());
			escrita.escreverArquivo(t.getLinha() + " " + t.getValor() + " " + t.getClasse().getClasse() + "\r\n");
		}

		for (int i = 0; i < this.listaDeErro.size(); i++) {
			if (i == 0) {
				System.out.println("\nTokens Inválidos\n");
				escrita.escreverArquivo("\r\nTokens Inválidos\r\n");
			}
			Token t = this.listaDeErro.get(i);
			System.out.println(t.getLinha() + " " + t.getValor() + " " + t.getClasse().getClasse());
			escrita.escreverArquivo(t.getLinha() + " " + t.getValor() + " " + t.getClasse().getClasse() + "\r\n");

		}

		escrita.fechaArquivo();

	}

	private Classe isdelimitacao(char c) {

		if (this.delimitador.isdelimitado(c)) {
			return Classe.DELIMITADOR;

		} else if (this.aritmetico.isOperAritmetico(c)) {
			return Classe.OPERADOR_ARITMETICO;

		} else if (this.logico.isOperLogico(c)) {
			return Classe.OPERADOR_LOGICO;

		} else if (this.relacional.isOperRelacional(c)) {
			return Classe.OPERADOR_RELACIONAL;

		}

		return Classe.NULL;
	}

	private void apagaTokenAnt() {
		int size = this.listaDeTokens.size();

		if (size != 0) {
			this.listaDeTokens.remove(size - 1);
		}

	}

	private Classe getClasse(char c) {
		int ascii = (int) c;

		if (c == '"') {
			return Classe.CADEIA_DE_CARACTERES;

		} else if (c == '/') {
			return Classe.COMENTARIO;

		} else if (ascii > 47 && ascii < 58) {

			if (this.getToken(-1).getValor().equals("-")) {
				Classe cl = this.getToken(-2).getClasse();

				// Verifica se há - que oderia ser classificado como parte de um número
				if (!cl.equals(Classe.NUMERO) && !cl.equals(Classe.IDENTIFICADOR)) {
					this.listaDeTokens.remove(this.listaDeTokens.size() - 1);
					this.lexema = "-";
				}
			}
			return Classe.NUMERO;

		} else if ((ascii > 96 && ascii < 123) || (ascii > 64 && ascii < 91)) {
			return Classe.IDENTIFICADOR;
		}

		return Classe.NULL;
	}

	private void classificao(Classe classe, int linha) {

		if (!classe.equals(Classe.ERRO)) {

			if (classe.equals(Classe.COMENTARIO)) {

				this.lexema = this.lexema.replaceAll("\r", "");
			} else if (classe.equals(Classe.IDENTIFICADOR) && isPalavraReservada(lexema)) {
				classe = Classe.PALAVRA_RESERVADA;
			}

			this.listaDeTokens.add(this.createToken(this.lexema, classe, linha));

		} else {
			this.listaDeErro.add(this.createToken(this.lexema, classe, linha));
		}

	}

	public void inicializar() {
		this.cadeia = new AutomatoCadeiaCaractere();
		this.comentario = new AutomatoComentario();
		this.delimitador = new AutomatoDelimitador();
		this.identificador = new AutomatoIdentificador();
		this.numero = new AutomatoNumeros();
		this.aritmetico = new AutomatoOperAritmetico();
		this.logico = new AutomatoOperLogico();
		this.relacional = new AutomatoOperRelacionais();

		this.listaDeTokens = new ArrayList<>();
		this.listaDeErro = new ArrayList<>();
		this.lexema = "";
	}

	private void resetAutomatos() {
		this.cadeia.resetAutomato();
		this.comentario.resetAutomato();
		this.identificador.resetAutomato();
		this.numero.resetAutomato();
	}

	private void resetDelimitacao() {
		this.aritmetico.resetAutomato();
		this.logico.resetAutomato();
		this.relacional.resetAutomato();
	}

	private Token createToken(String valor, Classe classe, int linha) {
		return new Token(valor, classe, linha);
	}

	private boolean isPalavraReservada(String lexema) {

		return (lexema.equals(PalavraReservada.BOOLEANO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.CONSTANTES.getPalavraReservada())
				|| lexema.equals(PalavraReservada.ENQUANTO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.ENTAO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.ESCREVA.getPalavraReservada())
				|| lexema.equals(PalavraReservada.FALSO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.INTEIRO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.LEIA.getPalavraReservada())
				|| lexema.equals(PalavraReservada.METODO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.PRINCIPAL.getPalavraReservada())
				|| lexema.equals(PalavraReservada.PROGRAMA.getPalavraReservada())
				|| lexema.equals(PalavraReservada.REAL.getPalavraReservada())
				|| lexema.equals(PalavraReservada.RESULTADO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.SE.getPalavraReservada())
				|| lexema.equals(PalavraReservada.SENAO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.TEXTO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.VARIAVEIS.getPalavraReservada())
				|| lexema.equals(PalavraReservada.VAZIO.getPalavraReservada())
				|| lexema.equals(PalavraReservada.VERDADEIRO.getPalavraReservada()));

	}

	public void concat(char c) {
		this.lexema += Character.toString(c);
	}

	private void resetLexema() {
		this.lexema = "";
	}

	private Token getToken(int index) {
		Token t = this.listaDeTokens.get(this.listaDeTokens.size() + index);

		return t;

	}

}
