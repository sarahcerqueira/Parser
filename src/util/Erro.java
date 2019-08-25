package util;

public class Erro {
	
	private int linha;
	private String erro;
	
	
	public Erro(int l, String e) {
		this.linha = l;
		this.erro = e;
	}
	
	public int getLinha() {
		return linha;
	}
	public void setLinha(int linha) {
		this.linha = linha;
	}
	public String getErro() {
		return erro;
	}
	public void setErro(String erro) {
		this.erro = erro;
	}
	
	

}
