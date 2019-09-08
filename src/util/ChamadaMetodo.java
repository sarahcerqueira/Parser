package util;

import java.util.ArrayList;

public class ChamadaMetodo { 
	private String id;
	private String escopo;
	private int linha;
	private ArrayList<String> parametros;
	

	public ChamadaMetodo(String id, String escopo, int linha) {
		this.id = id;
		this.escopo = escopo;
		this.linha = linha;
		parametros = new ArrayList<String>();
		
	}
	
	public void addParametro(String id) {
		parametros.add(id);
	}

	public String getId() {
		return id;
	}

	public String getEscopo() {
		return escopo;
	}

	public int getLinha() {
		return linha;
	}

	public ArrayList<String> getParametros() {
		return parametros;
	}
}
