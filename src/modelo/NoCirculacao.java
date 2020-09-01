package modelo;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jgrapht.traverse.ClosestFirstIterator;

import controle.RedeCirculacaoViaria;

public class NoCirculacao {

	private int idNoCirc;
	private String idString;
	private boolean caminhado;
	
	private Set<TrechoCirculacao> trechosProximos;
	private Set<TrechoCirculacao> trechosChegando;
	private Set<TrechoCirculacao> trechosSaindo;

	public NoCirculacao(String idString, boolean caminhado) {
		this.idString = idString;
		this.idNoCirc = Integer.parseInt(idString);
		this.caminhado = caminhado;
		
		trechosProximos = new HashSet<TrechoCirculacao>();
		trechosChegando = new HashSet<TrechoCirculacao>();
		trechosSaindo = new HashSet<TrechoCirculacao>();
	}
	
	public NoCirculacao(int idNoCirc) {
		this.idNoCirc = idNoCirc;
		this.idString = String.valueOf(idNoCirc);
		
		trechosProximos = new HashSet<TrechoCirculacao>();
		trechosChegando = new HashSet<TrechoCirculacao>();
		trechosSaindo = new HashSet<TrechoCirculacao>();
	}

	public Set<TrechoCirculacao> getNextClosestVertex(Integer distanciaLimiar) {
		ClosestFirstIterator<String, TrechoCirculacao> closestIterator = new ClosestFirstIterator<String, TrechoCirculacao>(
				RedeCirculacaoViaria.getWeightedGraph(), String.valueOf(getIdNoCirc()), distanciaLimiar);
		
		String noString = null;
		while (true) {
			try {
				noString = closestIterator.next();
			} catch(NoSuchElementException e) {
				break;
			}
			TrechoCirculacao trechoCirculacao = closestIterator.getSpanningTreeEdge(noString);
			if (trechoCirculacao != null) {
				trechosProximos.add(trechoCirculacao);
			}
			
			Integer noInt = Integer.parseInt(noString);
			if (RedeCirculacaoViaria.getTrechoIdToRestricaoIndex().containsKey(noInt) == false) {
				Integer novoId = RedeCirculacaoViaria.getTrechoIdToRestricaoIndex().size() + 1;
				RedeCirculacaoViaria.getTrechoIdToRestricaoIndex().put(noInt, novoId);
			}
		}
		
		setTrechosProximos(trechosProximos);
		return getTrechosProximos();
	}
	
	public int getIdNoCirc() {
		return idNoCirc;
	}

	public void setIdNoCirc(int idNoCirc) {
		this.idNoCirc = idNoCirc;
		this.idString = String.valueOf(idNoCirc);
	}
	
	public String getIdString() {
		return idString;
	}
	
	public void setIdString(String idString) {
		this.idString = idString;
		this.idNoCirc = Integer.parseInt(idString);
	}

	public boolean isCaminhado() {
		return caminhado;
	}

	public void setCaminhado(boolean caminhado) {
		this.caminhado = caminhado;
	}

	public Set<TrechoCirculacao> getTrechosProximos() {
		return trechosProximos;
	}

	public void setTrechosProximos(Set<TrechoCirculacao> trechosProximos) {
		this.trechosProximos = trechosProximos;
	}

	public Set<TrechoCirculacao> getTrechosChegando() {
		return trechosChegando;
	}
	
	public void setTrechosChegando(Set<TrechoCirculacao> trechosChegando) {
		this.trechosChegando = trechosChegando;
	}

	public Set<TrechoCirculacao> getTrechosSaindo() {
		return trechosSaindo;
	}

	public void setTrechosSaindo(Set<TrechoCirculacao> trechosSaindo) {
		this.trechosSaindo = trechosSaindo;
	}
	
}
