package modelo;

import java.util.ArrayList;
import java.util.HashSet;

public class PontoOnibusNovo extends PontoOnibus {

	private long idPontoNovo;

	private double beneficioPonto;
	
	private HashSet<TrechoCirculacao> trechosCaminhados;
	
	public PontoOnibusNovo(TrechoCirculacao trechoCirculacao, Long idPontoNovo, HashSet<TrechoCirculacao> trechosCaminhados) {
		super.setTrechoCirculacao(trechoCirculacao);
		super.setIdPontoOnibus(idPontoNovo);
		setIdPontoNovo(idPontoNovo);
		
		setTrechosCaminhados(trechosCaminhados);
	}

	public long getIdPontoNovo() {
		return idPontoNovo;
	}

	public void setIdPontoNovo(long idPontoNovo) {
		this.idPontoNovo = idPontoNovo;
	}

	public double getBeneficioPonto() {
		return beneficioPonto;
	}

	public void setBeneficioPonto(double beneficioPonto) {
		this.beneficioPonto = beneficioPonto;
	}

	public HashSet<TrechoCirculacao> getTrechosCaminhados() {
		return trechosCaminhados;
	}

	public void setTrechosCaminhados(HashSet<TrechoCirculacao> trechosCaminhados) {
		this.trechosCaminhados = trechosCaminhados;
	}
	
}
